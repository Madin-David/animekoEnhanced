/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.torrent.qbittorrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.him188.ani.app.torrent.api.FetchTorrentTimeoutException
import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentLibInfo
import me.him188.ani.app.torrent.api.TorrentSession
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.torrent.qbittorrent.api.QBittorrentClient
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.toKtPath
import me.him188.ani.utils.logging.logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * qBittorrent 下载器实现
 */
class QBittorrentDownloader(
    private val baseUrl: String,
    private val username: String,
    private val password: String,
    private val saveDir: String,
    private val verifyCertificate: Boolean = true,
    private val httpDownloader: HttpFileDownloader,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : TorrentDownloader {
    private val logger = logger<QBittorrentDownloader>()
    private val scope = CoroutineScope(parentCoroutineContext + Dispatchers.Default + Job())

    internal val client = QBittorrentClient(baseUrl, username, password, verifyCertificate)

    private val _totalStats = MutableStateFlow(
        TorrentDownloader.Stats(
            totalSize = 0L,
            downloadedBytes = 0L,
            downloadSpeed = 0L,
            uploadedBytes = 0L,
            uploadSpeed = 0L,
            downloadProgress = 0f,
        )
    )

    override val totalStats: Flow<TorrentDownloader.Stats> = _totalStats.asStateFlow()

    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "qBittorrent",
        version = "4.1+",
        supportsStreaming = true,
    )

    private val sessions = mutableMapOf<String, QBittorrentSession>()

    init {
        // 启动统计信息更新任务
        scope.launch {
            while (isActive) {
                updateStats()
                delay(2000) // 每 2 秒更新一次
            }
        }
    }

    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentInfo {
        logger.info { "Fetching torrent: $uri" }

        return when {
            uri.startsWith("magnet:") -> {
                // 磁力链接直接返回
                EncodedTorrentInfo.createRaw(uri.toByteArray())
            }
            uri.startsWith("http://") || uri.startsWith("https://") -> {
                // HTTP 链接，下载种子文件
                try {
                    val data = httpDownloader.download(uri)
                    EncodedTorrentInfo.createRaw(data)
                } catch (e: Exception) {
                    logger.error { "Failed to download torrent file: ${e.message}" }
                    throw FetchTorrentTimeoutException("Failed to download torrent file", e)
                }
            }
            else -> {
                throw IllegalArgumentException("Unsupported URI format: $uri")
            }
        }
    }

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext
    ): TorrentSession {
        logger.info { "Starting download" }

        // 登录
        if (!client.login()) {
            throw IllegalStateException("Failed to login to qBittorrent")
        }

        // 添加种子
        val dataBytes = data.data
        val success = when {
            dataBytes.decodeToString().startsWith("magnet:") -> {
                client.addMagnet(dataBytes.decodeToString(), saveDir)
            }
            else -> {
                client.addTorrentFile(dataBytes, saveDir)
            }
        }

        if (!success) {
            throw IllegalStateException("Failed to add torrent to qBittorrent")
        }

        // 等待种子出现在列表中
        var hash: String? = null
        repeat(30) { // 最多等待 30 秒
            delay(1000)
            val torrents = client.getTorrents()
            // 找到最新添加的种子（简化实现，实际应该根据 info hash 匹配）
            hash = torrents.maxByOrNull { it.size }?.hash
            if (hash != null) return@repeat
        }

        if (hash == null) {
            throw IllegalStateException("Failed to find torrent in qBittorrent")
        }

        // 创建 session
        val session = QBittorrentSession(
            hash = hash!!,
            client = client,
            saveDir = saveDir,
            scope = scope,
        )

        sessions[hash!!] = session
        return session
    }

    override fun getSaveDirForTorrent(data: EncodedTorrentInfo): SystemPath {
        return saveDir.toKtPath()
    }

    override fun listSaves(): List<SystemPath> {
        return listOf(saveDir.toKtPath())
    }

    private suspend fun updateStats() {
        try {
            val transferInfo = client.getTransferInfo() ?: return

            _totalStats.value = TorrentDownloader.Stats(
                totalSize = 0L, // qBittorrent API 不直接提供总大小
                downloadedBytes = transferInfo.dl_info_data,
                downloadSpeed = transferInfo.dl_info_speed,
                uploadedBytes = transferInfo.up_info_data,
                uploadSpeed = transferInfo.up_info_speed,
                downloadProgress = 0f,
            )
        } catch (e: Exception) {
            logger.error { "Failed to update stats: ${e.message}" }
        }
    }

    override fun close() {
        sessions.values.forEach { scope.launch { it.close() } }
        sessions.clear()
        client.close()
    }
}
