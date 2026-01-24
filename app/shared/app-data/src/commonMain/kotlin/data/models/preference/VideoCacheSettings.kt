/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.models.preference

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 视频缓存设置
 *
 * 控制视频播放时的缓存行为，包括预缓存、播放缓存等
 * @since 5.4.0
 */
@Serializable
data class VideoCacheSettings(
    /**
     * 播放时预缓存时长（秒）
     *
     * 在播放视频时，提前缓存多少秒的内容
     * 范围: 10-300 秒
     * 默认: 30 秒
     */
    val playbackPreCacheDuration: Int = 30,

    /**
     * 播放缓冲区大小（MB）
     *
     * 播放器内部缓冲区的大小
     * 范围: 16-512 MB
     * 默认: 64 MB
     */
    val playbackBufferSize: Int = 64,

    /**
     * HTTP/HLS 下载并发数
     *
     * 同时下载的视频片段数量
     * 范围: 1-16
     * 默认: 4
     */
    val httpDownloadConcurrency: Int = 4,

    /**
     * HTTP/HLS 分片大小（MB）
     *
     * 每个下载分片的大小
     * 范围: 1-64 MB
     * 默认: 8 MB
     */
    val httpChunkSize: Int = 8,

    /**
     * BT 下载速度限制（KB/s）
     *
     * BitTorrent 下载的最大速度，0 表示不限制
     * 范围: 0-102400 KB/s (0-100 MB/s)
     * 默认: 0 (不限制)
     */
    val torrentDownloadSpeedLimit: Int = 0,

    /**
     * BT 上传速度限制（KB/s）
     *
     * BitTorrent 上传的最大速度，0 表示不限制
     * 范围: 0-102400 KB/s (0-100 MB/s)
     * 默认: 0 (不限制)
     */
    val torrentUploadSpeedLimit: Int = 0,

    /**
     * BT 最大连接数
     *
     * BitTorrent 的最大对等连接数
     * 范围: 10-500
     * 默认: 200
     */
    val torrentMaxConnections: Int = 200,

    /**
     * 缓存清理策略
     *
     * 当缓存空间不足时的清理策略
     */
    val cacheCleanupStrategy: CacheCleanupStrategy = CacheCleanupStrategy.LEAST_RECENTLY_USED,

    /**
     * 最大缓存大小（GB）
     *
     * 视频缓存的最大总大小，0 表示不限制
     * 范围: 0-1000 GB
     * 默认: 0 (不限制)
     */
    val maxCacheSizeGB: Int = 0,

    /**
     * 自动清理缓存
     *
     * 当缓存超过最大大小时自动清理
     * 默认: true
     */
    val autoCleanupCache: Boolean = true,

    /**
     * 保留最近播放的缓存天数
     *
     * 自动清理时保留最近播放的缓存
     * 范围: 1-365 天
     * 默认: 7 天
     */
    val keepRecentCacheDays: Int = 7,

    /**
     * 低速网络模式
     *
     * 在低速网络下降低缓存质量以提高播放流畅度
     * 默认: false
     */
    val lowSpeedNetworkMode: Boolean = false,

    /**
     * 低速网络阈值（KB/s）
     *
     * 低于此速度时启用低速网络模式
     * 范围: 100-5000 KB/s
     * 默认: 500 KB/s
     */
    val lowSpeedNetworkThreshold: Int = 500,

    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    companion object {
        val Default = VideoCacheSettings()

        /**
         * 播放预缓存时长的有效范围
         */
        val PLAYBACK_PRE_CACHE_DURATION_RANGE = 10..300

        /**
         * 播放缓冲区大小的有效范围（MB）
         */
        val PLAYBACK_BUFFER_SIZE_RANGE = 16..512

        /**
         * HTTP 下载并发数的有效范围
         */
        val HTTP_DOWNLOAD_CONCURRENCY_RANGE = 1..16

        /**
         * HTTP 分片大小的有效范围（MB）
         */
        val HTTP_CHUNK_SIZE_RANGE = 1..64

        /**
         * BT 速度限制的有效范围（KB/s）
         */
        val TORRENT_SPEED_LIMIT_RANGE = 0..102400

        /**
         * BT 最大连接数的有效范围
         */
        val TORRENT_MAX_CONNECTIONS_RANGE = 10..500

        /**
         * 最大缓存大小的有效范围（GB）
         */
        val MAX_CACHE_SIZE_RANGE = 0..1000

        /**
         * 保留缓存天数的有效范围
         */
        val KEEP_RECENT_CACHE_DAYS_RANGE = 1..365

        /**
         * 低速网络阈值的有效范围（KB/s）
         */
        val LOW_SPEED_NETWORK_THRESHOLD_RANGE = 100..5000
    }

    /**
     * 验证设置值是否在有效范围内
     */
    fun validate(): VideoCacheSettings {
        return copy(
            playbackPreCacheDuration = playbackPreCacheDuration.coerceIn(PLAYBACK_PRE_CACHE_DURATION_RANGE),
            playbackBufferSize = playbackBufferSize.coerceIn(PLAYBACK_BUFFER_SIZE_RANGE),
            httpDownloadConcurrency = httpDownloadConcurrency.coerceIn(HTTP_DOWNLOAD_CONCURRENCY_RANGE),
            httpChunkSize = httpChunkSize.coerceIn(HTTP_CHUNK_SIZE_RANGE),
            torrentDownloadSpeedLimit = torrentDownloadSpeedLimit.coerceIn(TORRENT_SPEED_LIMIT_RANGE),
            torrentUploadSpeedLimit = torrentUploadSpeedLimit.coerceIn(TORRENT_SPEED_LIMIT_RANGE),
            torrentMaxConnections = torrentMaxConnections.coerceIn(TORRENT_MAX_CONNECTIONS_RANGE),
            maxCacheSizeGB = maxCacheSizeGB.coerceIn(MAX_CACHE_SIZE_RANGE),
            keepRecentCacheDays = keepRecentCacheDays.coerceIn(KEEP_RECENT_CACHE_DAYS_RANGE),
            lowSpeedNetworkThreshold = lowSpeedNetworkThreshold.coerceIn(LOW_SPEED_NETWORK_THRESHOLD_RANGE),
        )
    }
}

/**
 * 缓存清理策略
 */
enum class CacheCleanupStrategy {
    /**
     * 最近最少使用（LRU）
     *
     * 优先删除最久未使用的缓存
     */
    LEAST_RECENTLY_USED,

    /**
     * 最早创建
     *
     * 优先删除最早创建的缓存
     */
    OLDEST_FIRST,

    /**
     * 最大文件
     *
     * 优先删除占用空间最大的缓存
     */
    LARGEST_FIRST,
}
