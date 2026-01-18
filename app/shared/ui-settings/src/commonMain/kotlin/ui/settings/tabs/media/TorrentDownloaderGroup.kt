/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.preference.QBittorrentSettings
import me.him188.ani.app.data.models.preference.TorrentDownloaderConfig
import me.him188.ani.app.data.models.preference.TorrentDownloaderType
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.framework.components.DropdownItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.TextButtonItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem

/**
 * BT 下载器选择设置组
 */
@Composable
internal fun SettingsScope.TorrentDownloaderGroup(
    state: SettingsState<TorrentDownloaderConfig>,
    modifier: Modifier = Modifier,
) {
    val config by state

    Group(
        title = { Text("BT 下载器") },
        description = { Text("选择使用内置下载器或连接到外部下载器") },
    ) {
        DropdownItem(
            selected = { config.type },
            values = { TorrentDownloaderType.entries },
            itemText = { Text(it.displayName) },
            onSelect = { state.update(config.copy(type = it)) },
            title = { Text("下载器类型") },
            description = { Text("选择要使用的 BT 下载器实现") },
        )

        AnimatedVisibility(config.type == TorrentDownloaderType.ANITORRENT) {
            Column {
                Text(
                    "使用内置 Anitorrent 下载器",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        AnimatedVisibility(config.type == TorrentDownloaderType.QBITTORRENT) {
            QBittorrentSettingsGroup(
                settings = config.qbittorrent,
                onUpdate = { state.update(config.copy(qbittorrent = it)) },
            )
        }

        AnimatedVisibility(config.type == TorrentDownloaderType.ARIA2) {
            Column {
                Text(
                    "Aria2 支持即将推出",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        AnimatedVisibility(config.type == TorrentDownloaderType.TRANSMISSION) {
            Column {
                Text(
                    "Transmission 支持即将推出",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        AnimatedVisibility(config.type == TorrentDownloaderType.COMMAND_LINE) {
            Column {
                Text(
                    "命令行下载器支持即将推出（仅桌面平台）",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * qBittorrent 设置组
 */
@Composable
private fun SettingsScope.QBittorrentSettingsGroup(
    settings: QBittorrentSettings,
    onUpdate: (QBittorrentSettings) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Group(
        title = { Text("qBittorrent 设置") },
        useThinHeader = true,
    ) {
        TextFieldItem(
            value = settings.url,
            title = { Text("Web UI 地址") },
            placeholder = { Text("http://localhost:8080") },
            description = { Text("qBittorrent Web UI 的访问地址") },
            onValueChangeCompleted = { onUpdate(settings.copy(url = it)) },
        )

        TextFieldItem(
            value = settings.username,
            title = { Text("用户名") },
            placeholder = { Text("admin") },
            description = { Text("qBittorrent Web UI 登录用户名") },
            onValueChangeCompleted = { onUpdate(settings.copy(username = it)) },
        )

        TextFieldItem(
            value = settings.password,
            title = { Text("密码") },
            placeholder = { Text("") },
            description = { Text("qBittorrent Web UI 登录密码") },
            isPassword = true,
            onValueChangeCompleted = { onUpdate(settings.copy(password = it)) },
        )

        TextFieldItem(
            value = settings.saveDir,
            title = { Text("保存目录") },
            placeholder = { Text("/downloads") },
            description = { Text("种子文件的保存位置") },
            onValueChangeCompleted = { onUpdate(settings.copy(saveDir = it)) },
        )

        TextButtonItem(
            onClick = {
                scope.launch {
                    // TODO: 实现连接测试
                    // 需要创建一个临时的 QBittorrentClient 并测试登录
                }
            },
            title = { Text("测试连接") },
            description = { Text("测试是否能够成功连接到 qBittorrent") },
        )
    }
}

/**
 * 下载器类型的显示名称
 */
private val TorrentDownloaderType.displayName: String
    get() = when (this) {
        TorrentDownloaderType.ANITORRENT -> "内置 Anitorrent"
        TorrentDownloaderType.QBITTORRENT -> "qBittorrent"
        TorrentDownloaderType.ARIA2 -> "Aria2"
        TorrentDownloaderType.TRANSMISSION -> "Transmission"
        TorrentDownloaderType.COMMAND_LINE -> "命令行工具"
    }
