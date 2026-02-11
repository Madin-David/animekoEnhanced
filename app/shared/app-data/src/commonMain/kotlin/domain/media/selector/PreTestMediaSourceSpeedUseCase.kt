/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.selector

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
import me.him188.ani.app.domain.media.fetch.createFetchFetchSessionFlow
import me.him188.ani.app.domain.settings.GetMediaSelectorSettingsFlowUseCase
import me.him188.ani.app.domain.usecase.UseCase
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
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
    private val episodeCollectionRepository: EpisodeCollectionRepository by inject()
    private val subjectCollectionRepository: SubjectCollectionRepository by inject()
    private val mediaSourceManager: MediaSourceManager by inject()
    private val speedTester: MediaSourceSpeedTester by inject()
    private val speedTestResultManager: MediaSourceSpeedTestResultManager by inject()
    private val getMediaSelectorSettingsFlowUseCase: GetMediaSelectorSettingsFlowUseCase by inject()

    override suspend fun invoke(subjectId: Int) = withContext(Dispatchers.Default) {
        // 检查设置是否启用
        val settings = getMediaSelectorSettingsFlowUseCase().first()
        if (!settings.preTestSpeedOnSubjectDetails || !settings.enableSourceSpeedTest) {
            return@withContext
        }

        // 检查是否已有测速结果（去重）
        val existingResults = speedTestResultManager.speedTestResults.first()
        if (existingResults.isNotEmpty()) {
            // 已有测速结果，跳过
            return@withContext
        }

        // 获取所有剧集的收藏信息
        val episodeCollections = episodeCollectionRepository.subjectEpisodeCollectionInfosFlow(subjectId)
            .first()

        if (episodeCollections.isEmpty()) {
            return@withContext
        }

        // 找到最后一个已观看的剧集
        val lastWatchedEpisode = episodeCollections
            .filter { it.collectionType == UnifiedCollectionType.DONE }
            .maxByOrNull { it.episodeInfo.sort }

        // 智能延迟策略：如果用户从未观看过此番剧，延迟一段时间再测速
        if (lastWatchedEpisode == null) {
            // 用户未观看过，延迟后再测速
            delay(settings.preTestSpeedDelayForNewSubject)
        }
        // 如果用户已观看过，立即开始测速

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

        // 获取番剧信息
        val subjectInfo = try {
            subjectCollectionRepository.subjectCollectionFlow(subjectId).first().subjectInfo
        } catch (e: Exception) {
            // 获取失败，忽略
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
        } catch (e: Exception) {
            // 创建会话失败，忽略
            return@withContext
        }

        val mediaList = try {
            session.awaitCompletedResults()
        } catch (e: Exception) {
            // 获取失败，忽略
            return@withContext
        }

        if (mediaList.isEmpty()) {
            return@withContext
        }

        // 执行速度测试
        val speedTestResults = speedTester.testSources(
            mediaList = mediaList,
            settings = settings,
        )

        // 存储结果到管理器
        speedTestResultManager.updateResults(speedTestResults)
    }
}
