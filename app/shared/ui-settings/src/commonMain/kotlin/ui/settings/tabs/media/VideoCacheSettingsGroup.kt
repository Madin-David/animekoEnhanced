/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import me.him188.ani.app.data.models.preference.CacheCleanupStrategy
import me.him188.ani.app.data.models.preference.VideoCacheSettings
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.framework.components.DropdownItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SliderItem
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import kotlin.math.roundToInt

/**
 * 视频缓存设置组
 *
 * 提供视频播放缓存、下载缓存等相关设置
 */
@Composable
fun SettingsScope.VideoCacheSettingsGroup(
    videoCacheSettingsState: SettingsState<VideoCacheSettings>,
) {
    val settings by videoCacheSettingsState

    Group(title = { Text("视频缓存设置") }) {
        // 播放缓存设置
        SubGroup(title = { Text("播放缓存") }) {
            SliderItem(
                title = { Text("预缓存时长") },
                description = { Text("播放时提前缓存 ${settings.playbackPreCacheDuration} 秒") },
            ) {
                Slider(
                    value = settings.playbackPreCacheDuration.toFloat(),
                    onValueChange = { value ->
                        videoCacheSettingsState.update(
                            settings.copy(playbackPreCacheDuration = value.roundToInt()),
                        )
                    },
                    valueRange = VideoCacheSettings.PLAYBACK_PRE_CACHE_DURATION_RANGE.first.toFloat()..
                        VideoCacheSettings.PLAYBACK_PRE_CACHE_DURATION_RANGE.last.toFloat(),
                    steps = 28, // (300-10)/10 - 1
                )
            }

            HorizontalDividerItem()

            SliderItem(
                title = { Text("播放缓冲区大小") },
                description = { Text("${settings.playbackBufferSize} MB") },
            ) {
                Slider(
                    value = settings.playbackBufferSize.toFloat(),
                    onValueChange = { value ->
                        videoCacheSettingsState.update(
                            settings.copy(playbackBufferSize = value.roundToInt()),
                        )
                    },
                    valueRange = VideoCacheSettings.PLAYBACK_BUFFER_SIZE_RANGE.first.toFloat()..
                        VideoCacheSettings.PLAYBACK_BUFFER_SIZE_RANGE.last.toFloat(),
                    steps = 30, // (512-16)/16 - 1
                )
            }

            HorizontalDividerItem()

            SwitchItem(
                checked = settings.lowSpeedNetworkMode,
                onCheckedChange = { checked ->
                    videoCacheSettingsState.update(settings.copy(lowSpeedNetworkMode = checked))
                },
                title = { Text("低速网络模式") },
                description = { Text("在低速网络下降低缓存质量以提高播放流畅度") },
            )

            if (settings.lowSpeedNetworkMode) {
                HorizontalDividerItem()

                SliderItem(
                    title = { Text("低速网络阈值") },
                    description = { Text("低于 ${settings.lowSpeedNetworkThreshold} KB/s 时启用") },
                ) {
                    Slider(
                        value = settings.lowSpeedNetworkThreshold.toFloat(),
                        onValueChange = { value ->
                            videoCacheSettingsState.update(
                                settings.copy(lowSpeedNetworkThreshold = value.roundToInt()),
                            )
                        },
                        valueRange = VideoCacheSettings.LOW_SPEED_NETWORK_THRESHOLD_RANGE.first.toFloat()..
                            VideoCacheSettings.LOW_SPEED_NETWORK_THRESHOLD_RANGE.last.toFloat(),
                        steps = 48, // (5000-100)/100 - 1
                    )
                }
            }
        }

        HorizontalDividerItem()

        // HTTP/HLS 下载设置
        SubGroup(title = { Text("HTTP/HLS 下载") }) {
            SliderItem(
                title = { Text("下载并发数") },
                description = { Text("同时下载 ${settings.httpDownloadConcurrency} 个片段") },
            ) {
                Slider(
                    value = settings.httpDownloadConcurrency.toFloat(),
                    onValueChange = { value ->
                        videoCacheSettingsState.update(
                            settings.copy(httpDownloadConcurrency = value.roundToInt()),
                        )
                    },
                    valueRange = VideoCacheSettings.HTTP_DOWNLOAD_CONCURRENCY_RANGE.first.toFloat()..
                        VideoCacheSettings.HTTP_DOWNLOAD_CONCURRENCY_RANGE.last.toFloat(),
                    steps = 14, // 16-1-1
                )
            }

            HorizontalDividerItem()

            SliderItem(
                title = { Text("分片大小") },
                description = { Text("${settings.httpChunkSize} MB") },
            ) {
                Slider(
                    value = settings.httpChunkSize.toFloat(),
                    onValueChange = { value ->
                        videoCacheSettingsState.update(
                            settings.copy(httpChunkSize = value.roundToInt()),
                        )
                    },
                    valueRange = VideoCacheSettings.HTTP_CHUNK_SIZE_RANGE.first.toFloat()..
                        VideoCacheSettings.HTTP_CHUNK_SIZE_RANGE.last.toFloat(),
                    steps = 62, // 64-1-1
                )
            }
        }

        HorizontalDividerItem()

        // BT 下载设置
        SubGroup(title = { Text("BitTorrent 下载") }) {
            SliderItem(
                title = { Text("下载速度限制") },
                description = {
                    Text(
                        if (settings.torrentDownloadSpeedLimit == 0) {
                            "不限制"
                        } else {
                            "${settings.torrentDownloadSpeedLimit} KB/s"
                        },
                    )
                },
            ) {
                Slider(
                    value = settings.torrentDownloadSpeedLimit.toFloat(),
                    onValueChange = { value ->
                        videoCacheSettingsState.update(
                            settings.copy(torrentDownloadSpeedLimit = value.roundToInt()),
                        )
                    },
                    valueRange = 0f..10240f, // 0-10 MB/s for UI
                    steps = 101, // 10240/100 - 1
                )
            }

            HorizontalDividerItem()

            SliderItem(
                title = { Text("上传速度限制") },
                description = {
                    Text(
                        if (settings.torrentUploadSpeedLimit == 0) {
                            "不限制"
                        } else {
                            "${settings.torrentUploadSpeedLimit} KB/s"
                        },
                    )
                },
            ) {
                Slider(
                    value = settings.torrentUploadSpeedLimit.toFloat(),
                    onValueChange = { value ->
                        videoCacheSettingsState.update(
                            settings.copy(torrentUploadSpeedLimit = value.roundToInt()),
                        )
                    },
                    valueRange = 0f..10240f, // 0-10 MB/s for UI
                    steps = 101, // 10240/100 - 1
                )
            }

            HorizontalDividerItem()

            SliderItem(
                title = { Text("最大连接数") },
                description = { Text("${settings.torrentMaxConnections} 个连接") },
            ) {
                Slider(
                    value = settings.torrentMaxConnections.toFloat(),
                    onValueChange = { value ->
                        videoCacheSettingsState.update(
                            settings.copy(torrentMaxConnections = value.roundToInt()),
                        )
                    },
                    valueRange = VideoCacheSettings.TORRENT_MAX_CONNECTIONS_RANGE.first.toFloat()..
                        VideoCacheSettings.TORRENT_MAX_CONNECTIONS_RANGE.last.toFloat(),
                    steps = 48, // (500-10)/10 - 1
                )
            }
        }

        HorizontalDividerItem()

        // 缓存管理设置
        SubGroup(title = { Text("缓存管理") }) {
            SliderItem(
                title = { Text("最大缓存大小") },
                description = {
                    Text(
                        if (settings.maxCacheSizeGB == 0) {
                            "不限制"
                        } else {
                            "${settings.maxCacheSizeGB} GB"
                        },
                    )
                },
            ) {
                Slider(
                    value = settings.maxCacheSizeGB.toFloat(),
                    onValueChange = { value ->
                        videoCacheSettingsState.update(
                            settings.copy(maxCacheSizeGB = value.roundToInt()),
                        )
                    },
                    valueRange = 0f..200f, // 0-200 GB for UI
                    steps = 39, // 200/5 - 1
                )
            }

            HorizontalDividerItem()

            SwitchItem(
                checked = settings.autoCleanupCache,
                onCheckedChange = { checked ->
                    videoCacheSettingsState.update(settings.copy(autoCleanupCache = checked))
                },
                title = { Text("自动清理缓存") },
                description = { Text("当缓存超过最大大小时自动清理") },
            )

            if (settings.autoCleanupCache) {
                HorizontalDividerItem()

                DropdownItem(
                    title = { Text("清理策略") },
                    selected = { settings.cacheCleanupStrategy },
                    values = { CacheCleanupStrategy.entries },
                    itemText = { strategy ->
                        when (strategy) {
                            CacheCleanupStrategy.LEAST_RECENTLY_USED -> "最近最少使用"
                            CacheCleanupStrategy.OLDEST_FIRST -> "最早创建"
                            CacheCleanupStrategy.LARGEST_FIRST -> "最大文件"
                        }
                    },
                    onSelect = { strategy ->
                        videoCacheSettingsState.update(settings.copy(cacheCleanupStrategy = strategy))
                    },
                    description = {
                        Text(
                            when (settings.cacheCleanupStrategy) {
                                CacheCleanupStrategy.LEAST_RECENTLY_USED -> "优先删除最久未使用的缓存"
                                CacheCleanupStrategy.OLDEST_FIRST -> "优先删除最早创建的缓存"
                                CacheCleanupStrategy.LARGEST_FIRST -> "优先删除占用空间最大的缓存"
                            },
                        )
                    },
                )

                HorizontalDividerItem()

                SliderItem(
                    title = { Text("保留最近播放") },
                    description = { Text("保留最近 ${settings.keepRecentCacheDays} 天播放的缓存") },
                ) {
                    Slider(
                        value = settings.keepRecentCacheDays.toFloat(),
                        onValueChange = { value ->
                            videoCacheSettingsState.update(
                                settings.copy(keepRecentCacheDays = value.roundToInt()),
                            )
                        },
                        valueRange = VideoCacheSettings.KEEP_RECENT_CACHE_DAYS_RANGE.first.toFloat()..
                            30f, // Limit to 30 days for UI
                        steps = 28, // 30-1-1
                    )
                }
            }
        }
    }
}
