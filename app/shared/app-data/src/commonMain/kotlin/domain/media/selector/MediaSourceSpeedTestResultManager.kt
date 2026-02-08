/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.selector

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.him188.ani.app.domain.mediasource.codec.MediaSourceTier

/**
 * 管理媒体源速度测试结果的单例管理器
 *
 * 用于在不同组件之间共享速度测试结果，例如：
 * - MediaSelectorAutoSelectUseCase 执行速度测试并存储结果
 * - SwitchMediaOnPlayerErrorExtension 在重新选择时使用速度测试结果
 */
class MediaSourceSpeedTestResultManager {
    private val _speedTestResults = MutableStateFlow<Map<String, MediaSourceSpeedTester.SpeedTestResult>>(emptyMap())

    /**
     * 速度测试结果流
     * Key: mediaSourceId
     * Value: SpeedTestResult
     */
    val speedTestResults: StateFlow<Map<String, MediaSourceSpeedTester.SpeedTestResult>> = _speedTestResults.asStateFlow()

    /**
     * 更新速度测试结果
     */
    fun updateResults(results: List<MediaSourceSpeedTester.SpeedTestResult>) {
        val newResults = results.associate { it.mediaSourceId to it }
        _speedTestResults.value = _speedTestResults.value + newResults
    }

    /**
     * 清除所有速度测试结果
     */
    fun clearAll() {
        _speedTestResults.value = emptyMap()
    }

    /**
     * 清除特定源的速度测试结果
     */
    fun clearSource(mediaSourceId: String) {
        _speedTestResults.value = _speedTestResults.value - mediaSourceId
    }

    /**
     * 根据速度测试结果计算动态 tiers
     *
     * @return MediaSelectorSourceTiers，包含基于速度的动态优先级
     */
    fun calculateDynamicTiers(): MediaSelectorSourceTiers {
        val results = _speedTestResults.value.values.toList()
        val dynamicTiers = MediaSourceSpeedTester.calculateDynamicTiers(results)

        return MediaSelectorSourceTiers(
            tiers = dynamicTiers,
            fallback = { MediaSourceTier.Fallback },
        )
    }
}
