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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.data.repository.episode.EpisodeCollectionRepository
import me.him188.ani.app.domain.media.fetch.MediaFetchSession
import me.him188.ani.app.domain.media.fetch.MediaSourceMediaFetcher
import me.him188.ani.app.domain.usecase.UseCase
import me.him188.ani.datasources.api.topic.EpisodeCollectionType
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
    private val mediaFetcher: MediaSourceMediaFetcher by inject()
    private val speedTester: MediaSourceSpeedTester by inject()
    private val speedTestResultManager: MediaSourceSpeedTestResultManager by inject()
    private val getMediaSelectorSettingsFlowUseCase: GetMediaSelectorSettingsFlowUseCase by inject()

    override suspend fun invoke(subjectId: Int) = withContext(Dispatchers.Default) {
        // 检查设置是否启用
        val settings = getMediaSelectorSettingsFlowUseCase().first()
        if (!settings.preTestSpeedOnSubjectDetails || !settings.enableSourceSpeedTest) {
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
            .filter { it.collectionType == EpisodeCollectionType.DONE }
            .maxByOrNull { it.episode.sort }

        // 确定要测速的剧集：最后观看的下一集，或第一集
        val targetEpisode = if (lastWatchedEpisode != null) {
            // 找到下一集
            episodeCollections
                .filter { it.episode.sort > lastWatchedEpisode.episode.sort }
                .minByOrNull { it.episode.sort }
                ?: episodeCollections.first() // 如果没有下一集，使用第一集
        } else {
            // 没有观看历史，使用第一集
            episodeCollections.first()
        }

        // 创建媒体获取会话
        val session = MediaFetchSession(
            subjectId = subjectId,
            episodeId = targetEpisode.episode.id,
        )

        // 获取媒体源列表
        val mediaList = mediaFetcher.awaitCompletedAndSelectCached(session, null)

        if (mediaList.isEmpty()) {
            return@withContext
        }

        // 执行速度测试
        val speedTestResults = speedTester.testSources(mediaList, settings)

        // 存储结果到管理器
        speedTestResultManager.updateResults(speedTestResults)
    }
}
