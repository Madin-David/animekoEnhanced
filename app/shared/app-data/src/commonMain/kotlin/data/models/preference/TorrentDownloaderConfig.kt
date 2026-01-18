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
 * BT 下载器配置，支持内置 Anitorrent 和外部下载器
 */
@Serializable
data class TorrentDownloaderConfig(
    val type: TorrentDownloaderType = TorrentDownloaderType.ANITORRENT,
    val qbittorrent: QBittorrentSettings = QBittorrentSettings.Default,
    val aria2: Aria2Settings = Aria2Settings.Default,
    val transmission: TransmissionSettings = TransmissionSettings.Default,
    val commandLine: CommandLineSettings = CommandLineSettings.Default,
    @Transient private val _placeholder: Int = 0,
) {
    companion object {
        val Default = TorrentDownloaderConfig()
    }
}

/**
 * BT 下载器类型
 */
@Serializable
enum class TorrentDownloaderType {
    /**
     * 内置 Anitorrent 下载器
     */
    ANITORRENT,

    /**
     * qBittorrent Web API
     */
    QBITTORRENT,

    /**
     * Aria2 JSON-RPC
     */
    ARIA2,

    /**
     * Transmission RPC
     */
    TRANSMISSION,

    /**
     * 自定义命令行工具（仅桌面平台）
     */
    COMMAND_LINE,
}

/**
 * qBittorrent 设置
 */
@Serializable
data class QBittorrentSettings(
    /**
     * Web UI 地址，例如 http://localhost:8080
     */
    val url: String = "http://localhost:8080",

    /**
     * 用户名
     */
    val username: String = "admin",

    /**
     * 密码
     */
    val password: String = "",

    /**
     * 保存目录
     */
    val saveDir: String = "",

    /**
     * 是否启用 HTTPS 证书验证
     */
    val verifyCertificate: Boolean = true,

    @Transient private val _placeholder: Int = 0,
) {
    companion object {
        val Default = QBittorrentSettings()
    }
}

/**
 * Aria2 设置
 */
@Serializable
data class Aria2Settings(
    /**
     * JSON-RPC 地址，例如 http://localhost:6800/jsonrpc
     */
    val url: String = "http://localhost:6800/jsonrpc",

    /**
     * RPC Secret Token
     */
    val secret: String = "",

    /**
     * 保存目录
     */
    val saveDir: String = "",

    /**
     * 是否启用 HTTPS 证书验证
     */
    val verifyCertificate: Boolean = true,

    @Transient private val _placeholder: Int = 0,
) {
    companion object {
        val Default = Aria2Settings()
    }
}

/**
 * Transmission 设置
 */
@Serializable
data class TransmissionSettings(
    /**
     * RPC 地址，例如 http://localhost:9091/transmission/rpc
     */
    val url: String = "http://localhost:9091/transmission/rpc",

    /**
     * 用户名
     */
    val username: String = "",

    /**
     * 密码
     */
    val password: String = "",

    /**
     * 保存目录
     */
    val saveDir: String = "",

    /**
     * 是否启用 HTTPS 证书验证
     */
    val verifyCertificate: Boolean = true,

    @Transient private val _placeholder: Int = 0,
) {
    companion object {
        val Default = TransmissionSettings()
    }
}

/**
 * 命令行下载器设置（仅桌面平台）
 */
@Serializable
data class CommandLineSettings(
    /**
     * 可执行文件路径
     */
    val executable: String = "",

    /**
     * 命令行参数模板
     * 支持的占位符：
     * - {magnetLink}: 磁力链接
     * - {torrentFile}: 种子文件路径
     * - {saveDir}: 保存目录
     */
    val args: String = "--save-dir={saveDir} {magnetLink}",

    /**
     * 保存目录
     */
    val saveDir: String = "",

    @Transient private val _placeholder: Int = 0,
) {
    companion object {
        val Default = CommandLineSettings()
    }
}
