/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.torrent.engines

import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.him188.ani.app.data.models.preference.QBittorrentSettings
import me.him188.ani.app.domain.torrent.AbstractTorrentEngine
import me.him188.ani.app.domain.torrent.TorrentEngineType
import me.him188.ani.app.domain.torrent.peer.PeerFilterSettings
import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.peer.PeerFilter
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.torrent.qbittorrent.QBittorrentDownloader
import me.him188.ani.utils.ktor.ScopedHttpClient
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import kotlin.coroutines.CoroutineContext

/**
 * qBittorrent 引擎实现
 */
class QBittorrentEngine(
    config: Flow<QBittorrentSettings>,
    client: ScopedHttpClient,
    peerFilterSettings: Flow<PeerFilterSettings>,
    parentCoroutineContext: CoroutineContext,
) : AbstractTorrentEngine<QBittorrentDownloader, QBittorrentSettings>(
    type = TorrentEngineType.QBittorrent,
    config = config,
    client = client,
    peerFilterSettings = peerFilterSettings,
    parentCoroutineContext = parentCoroutineContext,
) {
    override val location: MediaSourceLocation get() = MediaSourceLocation.Local

    override val isSupported: Boolean = true

    init {
        initialized.complete(Unit)
    }

    override suspend fun testConnection(): Boolean {
        return try {
            val settings = config.first()
            val downloader = QBittorrentDownloader(
                baseUrl = settings.url,
                username = settings.username,
                password = settings.password,
                saveDir = settings.saveDir,
                verifyCertificate = settings.verifyCertificate,
                httpDownloader = client.asHttpFileDownloader(),
                parentCoroutineContext = scope.coroutineContext,
            )
            val result = downloader.client.login()
            downloader.close()
            result
        } catch (e: Exception) {
            logger.error { "Failed to test qBittorrent connection: ${e.message}" }
            false
        }
    }

    override suspend fun newInstance(config: QBittorrentSettings): QBittorrentDownloader {
        logger.info { "Creating QBittorrentDownloader instance" }
        return QBittorrentDownloader(
            baseUrl = config.url,
            username = config.username,
            password = config.password,
            saveDir = config.saveDir,
            verifyCertificate = config.verifyCertificate,
            httpDownloader = client.asHttpFileDownloader(),
            parentCoroutineContext = scope.coroutineContext,
        )
    }

    override suspend fun QBittorrentDownloader.applyConfig(config: QBittorrentSettings) {
        // qBittorrent 配置变更需要重新创建实例
        // 这里暂时不实现动态配置更新
        logger.info { "QBittorrent config changed, restart may be required" }
    }

    override suspend fun QBittorrentDownloader.applyPeerFilter(filter: PeerFilter) {
        // qBittorrent 不支持客户端 peer 过滤
        // 需要在 qBittorrent 服务端配置
        logger.info { "Peer filter is not supported by qBittorrent downloader" }
    }
}

private fun ScopedHttpClient.asHttpFileDownloader(): HttpFileDownloader = object : HttpFileDownloader {
    override suspend fun download(url: String): ByteArray = this@asHttpFileDownloader.use { get(url).readRawBytes() }
    override fun close() {}

    override fun toString(): String {
        return "HttpClientAsHttpFileDownloader(client=$this@asHttpFileDownloader)"
    }
}
