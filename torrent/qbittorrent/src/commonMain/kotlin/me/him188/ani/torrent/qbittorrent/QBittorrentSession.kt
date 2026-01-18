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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.him188.ani.app.torrent.api.TorrentSession
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.app.torrent.api.peer.PeerInfo
import me.him188.ani.torrent.qbittorrent.api.QBittorrentClient
import me.him188.ani.utils.io.toKtPath
import me.him188.ani.utils.logging.logger

/**
 * qBittorrent 种子会话实现
 */
class QBittorrentSession(
    private val hash: String,
    private val client: QBittorrentClient,
    private val saveDir: String,
    private val scope: CoroutineScope,
) : TorrentSession {
    private val logger = logger<QBittorrentSession>()

    private val _sessionStats = MutableStateFlow<TorrentSession.Stats?>(null)
    override val sessionStats: Flow<TorrentSession.Stats?> = _sessionStats.asStateFlow()

    private var cachedName: String? = null
    private var cachedFiles: List<TorrentFileEntry>? = null

    init {
        // 启动统计信息更新任务
        scope.launch {
            while (isActive) {
                updateStats()
                delay(2000) // 每 2 秒更新一次
            }
        }
    }

    override suspend fun getName(): String {
        if (cachedName != null) return cachedName!!

        val torrents = client.getTorrents(hashes = hash)
        if (torrents.isEmpty()) {
            throw IllegalStateException("Torrent not found: $hash")
        }

        cachedName = torrents[0].name
        return cachedName!!
    }

    override suspend fun getFiles(): List<TorrentFileEntry> {
        if (cachedFiles != null) return cachedFiles!!

        val files = client.getTorrentFiles(hash)
        val torrentInfo = client.getTorrents(hashes = hash).firstOrNull()
            ?: throw IllegalStateException("Torrent not found: $hash")

        cachedFiles = files.map { file ->
            QBittorrentFileEntry(
                path = file.name,
                length = file.size,
                pathInTorrent = file.name,
                saveDir = saveDir.toKtPath(),
                index = file.index,
            )
        }

        return cachedFiles!!
    }

    override fun getPeers(): List<PeerInfo> {
        // qBittorrent API 需要额外的调用来获取 peer 信息
        // 这里简化实现，返回空列表
        return emptyList()
    }

    private suspend fun updateStats() {
        try {
            val torrents = client.getTorrents(hashes = hash)
            if (torrents.isEmpty()) {
                logger.warn { "Torrent not found: $hash" }
                return
            }

            val torrent = torrents[0]
            _sessionStats.value = TorrentSession.Stats(
                totalSizeRequested = torrent.size,
                downloadedBytes = torrent.downloaded,
                downloadSpeed = torrent.dlspeed,
                uploadedBytes = torrent.uploaded,
                uploadSpeed = torrent.upspeed,
                downloadProgress = torrent.progress,
            )
        } catch (e: Exception) {
            logger.error { "Failed to update session stats: ${e.message}" }
        }
    }

    override suspend fun close() {
        // 不删除种子，只是停止跟踪
        logger.info { "Closing session for torrent: $hash" }
    }

    override suspend fun closeIfNotInUse() {
        // 简化实现，直接关闭
        close()
    }
}
