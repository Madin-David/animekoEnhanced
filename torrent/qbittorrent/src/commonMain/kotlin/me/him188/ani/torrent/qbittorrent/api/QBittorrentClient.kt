/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.torrent.qbittorrent.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.him188.ani.utils.logging.logger

/**
 * qBittorrent Web API 客户端
 * API 文档: https://github.com/qbittorrent/qBittorrent/wiki/WebUI-API-(qBittorrent-4.1)
 */
class QBittorrentClient(
    private val baseUrl: String,
    private val username: String,
    private val password: String,
    private val verifyCertificate: Boolean = true,
) : AutoCloseable {
    private val logger = logger<QBittorrentClient>()

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private var cookie: String? = null

    /**
     * 登录到 qBittorrent
     */
    suspend fun login(): Boolean {
        return try {
            val response = client.submitForm(
                url = "$baseUrl/api/v2/auth/login",
                formParameters = Parameters.build {
                    append("username", username)
                    append("password", password)
                }
            )

            val setCookie = response.headers[HttpHeaders.SetCookie]
            if (setCookie != null) {
                cookie = setCookie.substringBefore(";")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.error { "Failed to login to qBittorrent: ${e.message}" }
            false
        }
    }

    /**
     * 添加磁力链接
     */
    suspend fun addMagnet(magnetUri: String, savePath: String? = null): Boolean {
        ensureLoggedIn()
        return try {
            val response = client.submitForm(
                url = "$baseUrl/api/v2/torrents/add",
                formParameters = Parameters.build {
                    append("urls", magnetUri)
                    if (savePath != null) {
                        append("savepath", savePath)
                    }
                }
            ) {
                headers.append(HttpHeaders.Cookie, cookie!!)
            }
            response.bodyAsText() == "Ok."
        } catch (e: Exception) {
            logger.error { "Failed to add magnet: ${e.message}" }
            false
        }
    }

    /**
     * 添加种子文件
     */
    suspend fun addTorrentFile(torrentData: ByteArray, savePath: String? = null): Boolean {
        ensureLoggedIn()
        return try {
            val response = client.submitFormWithBinaryData(
                url = "$baseUrl/api/v2/torrents/add",
                formData = formData {
                    append("torrents", torrentData, Headers.build {
                        append(HttpHeaders.ContentType, "application/x-bittorrent")
                        append(HttpHeaders.ContentDisposition, "filename=\"torrent.torrent\"")
                    })
                    if (savePath != null) {
                        append("savepath", savePath)
                    }
                }
            ) {
                headers.append(HttpHeaders.Cookie, cookie!!)
            }
            response.bodyAsText() == "Ok."
        } catch (e: Exception) {
            logger.error { "Failed to add torrent file: ${e.message}" }
            false
        }
    }

    /**
     * 获取所有种子信息
     */
    suspend fun getTorrents(filter: String? = null, hashes: String? = null): List<TorrentInfo> {
        ensureLoggedIn()
        return try {
            client.get("$baseUrl/api/v2/torrents/info") {
                headers.append(HttpHeaders.Cookie, cookie!!)
                if (filter != null) parameter("filter", filter)
                if (hashes != null) parameter("hashes", hashes)
            }.body()
        } catch (e: Exception) {
            logger.error { "Failed to get torrents: ${e.message}" }
            emptyList()
        }
    }

    /**
     * 获取种子文件列表
     */
    suspend fun getTorrentFiles(hash: String): List<TorrentFileInfo> {
        ensureLoggedIn()
        return try {
            client.get("$baseUrl/api/v2/torrents/files") {
                headers.append(HttpHeaders.Cookie, cookie!!)
                parameter("hash", hash)
            }.body()
        } catch (e: Exception) {
            logger.error { "Failed to get torrent files: ${e.message}" }
            emptyList()
        }
    }

    /**
     * 暂停种子
     */
    suspend fun pauseTorrent(hash: String): Boolean {
        ensureLoggedIn()
        return try {
            client.post("$baseUrl/api/v2/torrents/pause") {
                headers.append(HttpHeaders.Cookie, cookie!!)
                parameter("hashes", hash)
            }
            true
        } catch (e: Exception) {
            logger.error { "Failed to pause torrent: ${e.message}" }
            false
        }
    }

    /**
     * 恢复种子
     */
    suspend fun resumeTorrent(hash: String): Boolean {
        ensureLoggedIn()
        return try {
            client.post("$baseUrl/api/v2/torrents/resume") {
                headers.append(HttpHeaders.Cookie, cookie!!)
                parameter("hashes", hash)
            }
            true
        } catch (e: Exception) {
            logger.error { "Failed to resume torrent: ${e.message}" }
            false
        }
    }

    /**
     * 删除种子
     */
    suspend fun deleteTorrent(hash: String, deleteFiles: Boolean = false): Boolean {
        ensureLoggedIn()
        return try {
            client.post("$baseUrl/api/v2/torrents/delete") {
                headers.append(HttpHeaders.Cookie, cookie!!)
                parameter("hashes", hash)
                parameter("deleteFiles", deleteFiles.toString())
            }
            true
        } catch (e: Exception) {
            logger.error { "Failed to delete torrent: ${e.message}" }
            false
        }
    }

    /**
     * 获取全局传输信息
     */
    suspend fun getTransferInfo(): TransferInfo? {
        ensureLoggedIn()
        return try {
            client.get("$baseUrl/api/v2/transfer/info") {
                headers.append(HttpHeaders.Cookie, cookie!!)
            }.body()
        } catch (e: Exception) {
            logger.error { "Failed to get transfer info: ${e.message}" }
            null
        }
    }

    private suspend fun ensureLoggedIn() {
        if (cookie == null) {
            if (!login()) {
                throw IllegalStateException("Failed to login to qBittorrent")
            }
        }
    }

    override fun close() {
        client.close()
    }
}

@Serializable
data class TorrentInfo(
    val hash: String,
    val name: String,
    val size: Long,
    val progress: Float,
    val dlspeed: Long,
    val upspeed: Long,
    val downloaded: Long,
    val uploaded: Long,
    val state: String,
    val save_path: String,
)

@Serializable
data class TorrentFileInfo(
    val index: Int,
    val name: String,
    val size: Long,
    val progress: Float,
    val priority: Int,
)

@Serializable
data class TransferInfo(
    val dl_info_speed: Long,
    val dl_info_data: Long,
    val up_info_speed: Long,
    val up_info_data: Long,
)
