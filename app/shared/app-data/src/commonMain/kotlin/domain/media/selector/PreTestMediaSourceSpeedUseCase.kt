/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.selector

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.repository.episode.EpisodeCollectionRepository
import me.him188.ani.app.data.repository.subject.SubjectCollectionRepository
import me.him188.ani.app.domain.media.fetch.MediaSourceManager
import me.him188.ani.app.domain.media.fetch.awaitCompletedResults
import me.him188.ani.app.domain.media.fetch.create
import me.him188.ani.app.domain.media.fetch.createFetchFetchSessionFlow
import me.him188.ani.app.domain.settings.GetMediaSelectorSettingsFlowUseCase
import me.him188.ani.app.domain.usecase.UseCase
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 在番剧详情页预先测速，基于历史观看集数或第一集
 */
interface PreTestMediaSourceSpeedUseCase : UseCase {
    /**
     * @param subjectId 番剧 ID
     */
    suspend operator fun invoke(subjectId: Int)
}

class PreTestMediaSourceSpeedUseCaseImpl : PreTestMediaSourceSpeedUseCase, KoinComponent {
    private val logger = logger<PreTestMediaSourceSpeedUseCaseImpl>()

    private val episodeCollectionRepository: EpisodeCollectionRepository by inject()
    private val subjectCollectionRepository: SubjectCollectionRepository by inject()
    private val mediaSourceManager: MediaSourceManager by inject()
    private val speedTester: MediaSourceSpeedTester by inject()
    private val speedTestResultManager: MediaSourceSpeedTestResultManager by inject()
    private val getMediaSelectorSettingsFlowUseCase: GetMediaSelectorSettingsFlowUseCase by inject()

    override suspend fun invoke(subjectId: Int) = withContext(Dispatchers.Default) {
        logger.info { "PreTestMediaSourceSpeed invoked for subjectId=$subjectId" }

        // 检查设置是否启用
        val settings = getMediaSelectorSettingsFlowUseCase().first()
        if (!settings.preTestSpeedOnSubjectDetails || !settings.enableSourceSpeedTest) {
            logger.info { "Pre-test speed is disabled in settings" }
            return@withContext
        }

        // 检查是否已有测速结果（去重）
        val existingResults = speedTestResultManager.speedTestResults.first()
        if (existingResults.isNotEmpty()) {
            logger.info { "Speed test results already exist, skipping pre-test for subjectId=$subjectId" }
            return@withContext
        }

        // 获取所有剧集的收藏信息
        val episodeCollections = try {
            episodeCollectionRepository.subjectEpisodeCollectionInfosFlow(subjectId).first()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Failed to get episode collections for subjectId=$subjectId" }
            return@withContext
        }

        if (episodeCollections.isEmpty()) {
            logger.info { "No episodes found for subjectId=$subjectId" }
            return@withContext
        }

        // 找到最后一个已观看的剧集
        val lastWatchedEpisode = episodeCollections
            .filter { it.collectionType == UnifiedCollectionType.DONE }
            .maxByOrNull { it.episodeInfo.sort }

        // 智能延迟策略：如果用户从未观看过此番剧，延迟一段时间再测速
        if (lastWatchedEpisode == null) {
            logger.info { "User has not watched subjectId=$subjectId, delaying pre-test by ${settings.preTestSpeedDelayForNewSubject}" }
            delay(settings.preTestSpeedDelayForNewSubject)
        } else {
            logger.info { "User has watched subjectId=$subjectId, starting pre-test immediately" }
        }

        // 确定要测速的剧集：最后观看的下一集，或第一集
        val targetEpisode: EpisodeInfo = if (lastWatchedEpisode != null) {
            // 找到下一集
            episodeCollections
                .filter { it.episodeInfo.sort > lastWatchedEpisode.episodeInfo.sort }
                .minByOrNull { it.episodeInfo.sort }
                ?.episodeInfo
                ?: episodeCollections.first().episodeInfo // 如果没有下一集，使用第一集
        } else {
            // 没有观看历史，使用第一集
            episodeCollections.first().episodeInfo
        }

        logger.info { "Target episode for pre-test: episodeId=${targetEpisode.episodeId}, sort=${targetEpisode.sort}" }

        // 获取番剧信息
        val subjectInfo = try {
            subjectCollectionRepository.subjectCollectionFlow(subjectId).first().subjectInfo
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Failed to get subject info for subjectId=$subjectId" }
            return@withContext
        }

        // 创建媒体获取请求
        val request = MediaFetchRequest.create(
            subject = subjectInfo,
            episode = targetEpisode,
        )

        // 创建媒体获取会话并获取结果
        val session = try {
            mediaSourceManager.createFetchFetchSessionFlow(flowOf(request)).first()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Failed to create media fetch session for subjectId=$subjectId, episodeId=${targetEpisode.episodeId}" }
            return@withContext
        }

        val mediaList = try {
            session.awaitCompletedResults()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Failed to fetch media list for subjectId=$subjectId, episodeId=${targetEpisode.episodeId}" }
            return@withContext
        }

        if (mediaList.isEmpty()) {
            logger.info { "No media sources found for subjectId=$subjectId, episodeId=${targetEpisode.episodeId}" }
            return@withContext
        }

        logger.info { "Starting speed test for ${mediaList.size} media sources" }

        // 执行速度测试
        val speedTestResults = try {
            speedTester.testSources(
                mediaList = mediaList,
                settings = settings,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Speed test failed for subjectId=$subjectId" }
            return@withContext
        }

        logger.info { "Speed test completed with ${speedTestResults.size} results" }

        // 存储结果到管理器
        speedTestResultManager.updateResults(speedTestResults)
    }
}
