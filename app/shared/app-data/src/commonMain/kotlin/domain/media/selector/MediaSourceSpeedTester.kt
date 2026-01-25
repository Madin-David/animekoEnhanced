/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.selector

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.domain.mediasource.codec.MediaSourceTier
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.ktor.ScopedHttpClient
import me.him188.ani.utils.logging.logger
import kotlin.time.Duration
import kotlin.time.measureTime

/**
 * 视频源速度测试器, 用于在播放前测试各个源的下载速度
 */
class MediaSourceSpeedTester(
    private val httpClient: ScopedHttpClient,
) {
    private val logger = logger<MediaSourceSpeedTester>()

    /**
     * 测试结果, 包含下载速度 (字节/秒) 和延迟 (毫秒)
     */
    data class SpeedTestResult(
        val mediaSourceId: String,
        val speedBytesPerSecond: Long,
        val latencyMs: Long,
        val success: Boolean,
    )

    /**
     * 测试多个媒体源的速度
     *
     * @param mediaList 要测试的媒体列表
     * @param settings 媒体选择器设置
     * @return 测试结果列表, 按速度从快到慢排序
     */
    suspend fun testSources(
        mediaList: List<Media>,
        settings: MediaSelectorSettings,
    ): List<SpeedTestResult> = coroutineScope {
        if (!settings.enableSourceSpeedTest) {
            logger.info { "Source speed test is disabled" }
            return@coroutineScope emptyList()
        }

        logger.info { "Starting speed test for ${mediaList.size} sources" }

        // 按 mediaSourceId 分组, 每个源只测试一次
        val mediaBySource = mediaList.groupBy { it.mediaSourceId }

        // 并发测试所有源
        val results = mediaBySource.map { (mediaSourceId, medias) ->
            async {
                // 对每个源, 选择第一个可用的 media 进行测试
                val media = medias.firstOrNull() ?: return@async SpeedTestResult(
                    mediaSourceId = mediaSourceId,
                    speedBytesPerSecond = 0,
                    latencyMs = Long.MAX_VALUE,
                    success = false,
                )

                testSingleSource(
                    media = media,
                    timeout = settings.sourceSpeedTestTimeout,
                    segmentSize = settings.sourceSpeedTestSegmentSize,
                )
            }
        }.awaitAll()

        // 按速度排序 (速度快的在前)
        results
            .filter { it.success }
            .sortedByDescending { it.speedBytesPerSecond }
            .also { successfulResults ->
                logger.info {
                    "Speed test completed: ${successfulResults.size}/${mediaBySource.size} sources tested successfully"
                }
                successfulResults.forEach { result ->
                    logger.debug {
                        "Source ${result.mediaSourceId}: ${result.speedBytesPerSecond / 1024} KB/s, latency: ${result.latencyMs}ms"
                    }
                }
            }
    }

    /**
     * 测试单个媒体源的速度
     */
    private suspend fun testSingleSource(
        media: Media,
        timeout: Duration,
        segmentSize: Long,
    ): SpeedTestResult {
        return try {
            withTimeout(timeout) {
                val url = extractTestUrl(media.download) ?: run {
                    logger.debug { "Cannot extract test URL from media ${media.mediaId}" }
                    return@withTimeout SpeedTestResult(
                        mediaSourceId = media.mediaSourceId,
                        speedBytesPerSecond = 0,
                        latencyMs = Long.MAX_VALUE,
                        success = false,
                    )
                }

                var bytesDownloaded = 0L
                val duration = measureTime {
                    try {
                        httpClient.use { client ->
                            client.get(url) {
                                // 只下载指定大小的片段
                                headers.append("Range", "bytes=0-${segmentSize - 1}")
                            }.bodyAsChannel().let { channel ->
                                val buffer = ByteArray(8192)
                                while (bytesDownloaded < segmentSize) {
                                    val read = channel.readAvailable(buffer)
                                    if (read <= 0) break
                                    bytesDownloaded += read
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.debug(e) { "Failed to download from ${media.mediaId}" }
                        throw e
                    }
                }

                val speedBytesPerSecond = if (duration.inWholeMilliseconds > 0) {
                    (bytesDownloaded * 1000) / duration.inWholeMilliseconds
                } else {
                    Long.MAX_VALUE // 极快的速度
                }

                SpeedTestResult(
                    mediaSourceId = media.mediaSourceId,
                    speedBytesPerSecond = speedBytesPerSecond,
                    latencyMs = duration.inWholeMilliseconds,
                    success = bytesDownloaded > 0,
                )
            }
        } catch (e: TimeoutCancellationException) {
            logger.debug(e) { "Speed test timeout for media ${media.mediaId}" }
            SpeedTestResult(
                mediaSourceId = media.mediaSourceId,
                speedBytesPerSecond = 0,
                latencyMs = Long.MAX_VALUE,
                success = false,
            )
        } catch (e: Exception) {
            logger.debug(e) { "Speed test failed for media ${media.mediaId}" }
            SpeedTestResult(
                mediaSourceId = media.mediaSourceId,
                speedBytesPerSecond = 0,
                latencyMs = Long.MAX_VALUE,
                success = false,
            )
        }
    }

    /**
     * 从 ResourceLocation 提取可用于测试的 URL
     */
    private fun extractTestUrl(download: ResourceLocation): String? {
        return when (download) {
            is ResourceLocation.HttpStreamingFile -> download.uri
            is ResourceLocation.HttpTorrentFile -> download.uri
            is ResourceLocation.MagnetLink -> null // 磁力链接无法直接测速
            is ResourceLocation.LocalFile -> null // 本地文件不需要测速
            is ResourceLocation.WebVideo -> download.uri
        }
    }

    companion object {
        /**
         * 根据速度测试结果计算动态的源优先级
         *
         * @param results 速度测试结果
         * @return 媒体源 ID 到动态优先级的映射
         */
        fun calculateDynamicTiers(results: List<SpeedTestResult>): Map<String, MediaSourceTier> {
            if (results.isEmpty()) return emptyMap()

            // 将速度测试结果转换为优先级
            // 速度最快的源获得最高优先级 (tier = 0)
            // 其他源根据速度相对排名获得优先级
            return results
                .filter { it.success }
                .sortedByDescending { it.speedBytesPerSecond }
                .mapIndexed { index, result ->
                    result.mediaSourceId to MediaSourceTier(index.toUInt())
                }
                .toMap()
        }
    }
}
