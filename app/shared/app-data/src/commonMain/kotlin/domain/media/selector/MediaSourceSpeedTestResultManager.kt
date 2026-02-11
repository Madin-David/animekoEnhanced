/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.selector

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.him188.ani.app.domain.mediasource.codec.MediaSourceTier
import kotlin.time.Duration.Companion.hours

/**
 * 管理媒体源速度测试结果的单例管理器
 *
 * 用于在不同组件之间共享速度测试结果，例如：
 * - MediaSelectorAutoSelectUseCase 执行速度测试并存储结果
 * - SwitchMediaOnPlayerErrorExtension 在重新选择时使用速度测试结果
 *
 * 实现了 LRU 缓存策略和自动过期机制，防止内存泄漏。
 */
class MediaSourceSpeedTestResultManager {
    /**
     * 带时间戳的速度测试结果
     */
    private data class TimestampedResult(
        val result: MediaSourceSpeedTester.SpeedTestResult,
        val timestamp: Instant,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _speedTestResults = MutableStateFlow<Map<String, TimestampedResult>>(emptyMap())

    /**
     * 速度测试结果流
     * Key: mediaSourceId
     * Value: SpeedTestResult
     */
    val speedTestResults: StateFlow<Map<String, MediaSourceSpeedTester.SpeedTestResult>> by lazy {
        _speedTestResults
            .map { timestampedMap ->
                timestampedMap.mapValues { it.value.result }
            }
            .stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyMap())
    }

    companion object {
        /**
         * 最大缓存数量（LRU 策略）
         */
        private const val MAX_CACHE_SIZE = 50

        /**
         * 结果过期时间（1 小时）
         */
        private val EXPIRATION_TIME = 1.hours
    }

    /**
     * 更新速度测试结果（线程安全，带 LRU 和过期清理）
     */
    fun updateResults(results: List<MediaSourceSpeedTester.SpeedTestResult>) {
        val now = Clock.System.now()
        val newResults = results.associate {
            it.mediaSourceId to TimestampedResult(it, now)
        }

        _speedTestResults.update { current ->
            // 1. 移除过期的结果
            val nonExpired = current.filterValues {
                now - it.timestamp < EXPIRATION_TIME
            }

            // 2. 合并新结果
            val merged = nonExpired + newResults

            // 3. 如果超过最大缓存数量，移除最旧的结果（LRU）
            if (merged.size > MAX_CACHE_SIZE) {
                merged.entries
                    .sortedByDescending { it.value.timestamp }
                    .take(MAX_CACHE_SIZE)
                    .associate { it.key to it.value }
            } else {
                merged
            }
        }
    }

    /**
     * 清除所有速度测试结果
     */
    fun clearAll() {
        _speedTestResults.value = emptyMap()
    }

    /**
     * 清除特定源的速度测试结果（线程安全）
     */
    fun clearSource(mediaSourceId: String) {
        _speedTestResults.update { current -> current - mediaSourceId }
    }

    /**
     * 清除过期的结果
     */
    fun clearExpired() {
        val now = Clock.System.now()
        _speedTestResults.update { current ->
            current.filterValues { now - it.timestamp < EXPIRATION_TIME }
        }
    }

    /**
     * 根据速度测试结果计算动态 tiers
     *
     * @return MediaSelectorSourceTiers，包含基于速度的动态优先级
     */
    fun calculateDynamicTiers(): MediaSelectorSourceTiers {
        val results = _speedTestResults.value.values.map { it.result }
        val dynamicTiers = MediaSourceSpeedTester.calculateDynamicTiers(results)

        return MediaSelectorSourceTiers(
            tiers = dynamicTiers,
            fallback = { MediaSourceTier.Fallback },
        )
    }
}
