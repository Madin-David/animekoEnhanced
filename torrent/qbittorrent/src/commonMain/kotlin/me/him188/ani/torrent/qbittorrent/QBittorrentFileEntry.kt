/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.torrent.qbittorrent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.torrent.api.files.FilePriority
import me.him188.ani.app.torrent.api.files.PieceState
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.app.torrent.api.files.TorrentFileHandle
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.absolutePath
import me.him188.ani.utils.io.resolve

/**
 * qBittorrent 文件条目实现
 */
class QBittorrentFileEntry(
    private val path: String,
    override val length: Long,
    override val pathInTorrent: String,
    private val saveDir: SystemPath,
    private val index: Int,
) : TorrentFileEntry {

    private val _supportsStreaming = MutableStateFlow(true)
    override val supportsStreaming: Flow<Boolean> = _supportsStreaming

    override suspend fun createHandle(): TorrentFileHandle {
        return QBittorrentFileHandle(
            resolvedFile = saveDir.resolve(pathInTorrent),
            length = length,
        )
    }

    override suspend fun resolveFile(): SystemPath {
        return saveDir.resolve(pathInTorrent)
    }

    override suspend fun resolveFileMaybeEmptyOrNull(): SystemPath? {
        return resolveFile()
    }

    override fun toString(): String {
        return "QBittorrentFileEntry(path='$path', length=$length)"
    }
}

/**
 * qBittorrent 文件句柄实现
 */
class QBittorrentFileHandle(
    private val resolvedFile: SystemPath,
    private val length: Long,
) : TorrentFileHandle {

    override val file: SystemPath = resolvedFile

    override suspend fun resume() {
        // qBittorrent 通过 API 控制，这里不需要实现
    }

    override fun createInput(): SeekableInput {
        // 返回文件的输入流
        // 注意：这里需要使用平台特定的文件访问方式
        // 简化实现，实际应该使用 SystemPath 的 API
        throw UnsupportedOperationException("Direct file access not implemented for qBittorrent")
    }

    override val fileStats: Flow<TorrentFileHandle.Stats>
        get() = MutableStateFlow(
            TorrentFileHandle.Stats(
                downloadedBytes = length, // 简化实现，假设已下载
                downloadProgress = 1f,
            )
        )

    override val pieces: Flow<List<PieceState>>
        get() = MutableStateFlow(emptyList())

    override val isDownloaded: Flow<Boolean>
        get() = MutableStateFlow(true)

    override suspend fun closeAndDelete() {
        // 不删除文件，由 qBittorrent 管理
    }

    override suspend fun close() {
        // 无需操作
    }

    override suspend fun priority(): FilePriority {
        return FilePriority.NORMAL
    }

    override suspend fun prioritize() {
        // qBittorrent 通过 API 控制优先级
    }
}
