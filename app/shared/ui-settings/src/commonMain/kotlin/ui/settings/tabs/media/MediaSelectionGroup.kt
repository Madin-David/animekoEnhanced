/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.fastAll
import me.him188.ani.app.data.models.preference.MediaPreference
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.ui.foundation.animation.AniAnimatedVisibility
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.settings_media_advanced_settings
import me.him188.ani.app.ui.lang.settings_media_advanced_settings_description
import me.him188.ani.app.ui.lang.settings_media_alliance
import me.him188.ani.app.ui.lang.settings_media_alliance_description
import me.him188.ani.app.ui.lang.settings_media_any
import me.him188.ani.app.ui.lang.settings_media_auto_cache_all
import me.him188.ani.app.ui.lang.settings_media_auto_cache_none
import me.him188.ani.app.ui.lang.settings_media_auto_cache_partial
import me.him188.ani.app.ui.lang.settings_media_auto_cache_space
import me.him188.ani.app.ui.lang.settings_media_auto_enable_last
import me.him188.ani.app.ui.lang.settings_media_auto_enable_last_description
import me.him188.ani.app.ui.lang.settings_media_fast_select_web
import me.him188.ani.app.ui.lang.settings_media_fast_select_web_description
import me.him188.ani.app.ui.lang.settings_media_hide_no_subtitle
import me.him188.ani.app.ui.lang.settings_media_hide_no_subtitle_description
import me.him188.ani.app.ui.lang.settings_media_hide_single_episode
import me.him188.ani.app.ui.lang.settings_media_hide_single_episode_description
import me.him188.ani.app.ui.lang.settings_media_max_wait_time
import me.him188.ani.app.ui.lang.settings_media_max_wait_time_description
import me.him188.ani.app.ui.lang.settings_media_none
import me.him188.ani.app.ui.lang.settings_media_prefer_seasons
import me.him188.ani.app.ui.lang.settings_media_prefer_seasons_description
import me.him188.ani.app.ui.lang.settings_media_prefer_source_type
import me.him188.ani.app.ui.lang.settings_media_prefer_source_type_description
import me.him188.ani.app.ui.lang.settings_media_preference_description
import me.him188.ani.app.ui.lang.settings_media_preference_override_notice
import me.him188.ani.app.ui.lang.settings_media_preference_title
import me.him188.ani.app.ui.lang.settings_media_resolution
import me.him188.ani.app.ui.lang.settings_media_resolution_description
import me.him188.ani.app.ui.lang.settings_media_show_disabled
import me.him188.ani.app.ui.lang.settings_media_show_disabled_description
import me.him188.ani.app.ui.lang.settings_media_sort_tip
import me.him188.ani.app.ui.lang.settings_media_source_bt
import me.him188.ani.app.ui.lang.settings_media_source_no_preference
import me.him188.ani.app.ui.lang.settings_media_source_web
import me.him188.ani.app.ui.lang.settings_media_speed_test
import me.him188.ani.app.ui.lang.settings_media_speed_test_description
import me.him188.ani.app.ui.lang.settings_media_speed_test_segment_size
import me.him188.ani.app.ui.lang.settings_media_speed_test_segment_size_1mb
import me.him188.ani.app.ui.lang.settings_media_speed_test_segment_size_256kb
import me.him188.ani.app.ui.lang.settings_media_speed_test_segment_size_2mb
import me.him188.ani.app.ui.lang.settings_media_speed_test_segment_size_512kb
import me.him188.ani.app.ui.lang.settings_media_speed_test_segment_size_description
import me.him188.ani.app.ui.lang.settings_media_speed_test_timeout
import me.him188.ani.app.ui.lang.settings_media_speed_test_timeout_description
import me.him188.ani.app.ui.lang.settings_media_pre_test_delay_for_new
import me.him188.ani.app.ui.lang.settings_media_pre_test_delay_for_new_description
import me.him188.ani.app.ui.lang.settings_media_pre_test_on_details
import me.him188.ani.app.ui.lang.settings_media_pre_test_on_details_description
import me.him188.ani.app.ui.lang.settings_media_subtitle_language
import me.him188.ani.app.ui.lang.settings_media_wait_time_10s
import me.him188.ani.app.ui.lang.settings_media_wait_time_15s
import me.him188.ani.app.ui.lang.settings_media_wait_time_1s
import me.him188.ani.app.ui.lang.settings_media_wait_time_2s
import me.him188.ani.app.ui.lang.settings_media_wait_time_3s
import me.him188.ani.app.ui.lang.settings_media_wait_time_5s
import me.him188.ani.app.ui.lang.settings_media_wait_time_8s
import me.him188.ani.app.ui.lang.settings_media_wait_time_infinite
import me.him188.ani.app.ui.lang.settings_media_wait_time_none
import me.him188.ani.app.ui.media.renderResolution
import me.him188.ani.app.ui.media.renderSubtitleLanguage
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.framework.components.DropdownItem
import me.him188.ani.app.ui.settings.framework.components.SelectableItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SorterItem
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem
import me.him188.ani.app.ui.settings.rendering.MediaSourceIcons
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Stable
class MediaSelectionGroupState(
    val defaultMediaPreferenceState: SettingsState<MediaPreference>,
    val mediaSelectorSettingsState: SettingsState<MediaSelectorSettings>,
) {
    val defaultMediaPreference by defaultMediaPreferenceState

    private val allSubtitleLanguageIds = SubtitleLanguage.matchableEntries.map { it.id }
    val sortedLanguages by derivedStateOf {
        defaultMediaPreference.fallbackSubtitleLanguageIds.extendTo(allSubtitleLanguageIds)
    }

    private val allResolutionIds = Resolution.entries.map { it.id }
    val sortedResolutions by derivedStateOf {
        defaultMediaPreference.fallbackResolutions.extendTo(allResolutionIds)
    }

    /**
     * 将 [this] 扩展到 [all]，并保持顺序.
     */
    private fun List<String>?.extendTo(
        all: List<String>
    ): List<SelectableItem<String>> {
        val fallback = this ?: return all.map { SelectableItem(it, selected = true) }

        return fallback.map {
            SelectableItem(it, selected = true)
        } + (all - fallback.toSet()).map {
            SelectableItem(it, selected = false)
        }
    }
}

@Composable
internal fun SettingsScope.MediaSelectionGroup(
    state: MediaSelectionGroupState
) {
    Group(
        title = {
            Text(stringResource(Lang.settings_media_preference_title))
        },
        description = {
            Column {
                Text(stringResource(Lang.settings_media_preference_description))
                Text(stringResource(Lang.settings_media_preference_override_notice))
            }
        },
    ) {
        val textAny = stringResource(Lang.settings_media_any)
        val textNone = stringResource(Lang.settings_media_none)

        SorterItem(
            values = { state.sortedLanguages },
            onSort = { list ->
                state.defaultMediaPreferenceState.update(
                    state.defaultMediaPreference.copy(
                        fallbackSubtitleLanguageIds = list.filter { it.selected }
                            .map { it.item },
                    ),
                )
            },
            exposed = { list ->
                Text(
                    remember(list) {
                        if (list.fastAll { it.selected }) {
                            textAny
                        } else if (list.fastAll { !it.selected }) {
                            textNone
                        } else
                            list.asSequence().filter { it.selected }
                                .joinToString { renderSubtitleLanguage(it.item) }
                    },
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            item = { Text(renderSubtitleLanguage(it)) },
            key = { it },
            dialogDescription = {
                Text(
                    stringResource(Lang.settings_media_sort_tip),
                )
            },
            icon = { Icon(Icons.Outlined.Language, null) },
            title = { Text(stringResource(Lang.settings_media_subtitle_language)) },
        )

        HorizontalDividerItem()

        SorterItem(
            values = { state.sortedResolutions },
            onSort = { list ->
                state.defaultMediaPreferenceState.update(
                    state.defaultMediaPreference.copy(
                        fallbackResolutions = list.filter { it.selected }
                            .map { it.item },
                    ),
                )
            },
            exposed = { list ->
                Text(
                    remember(list) {
                        if (list.fastAll { it.selected }) {
                            textAny
                        } else if (list.fastAll { !it.selected }) {
                            textNone
                        } else
                            list.asSequence().filter { it.selected }
                                .joinToString { renderResolution(it.item) }
                    },
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            item = { Text(renderResolution(it)) },
            key = { it },
            dialogDescription = { Text(stringResource(Lang.settings_media_sort_tip)) },
            icon = { Icon(Icons.Outlined.Hd, null) },
            title = { Text(stringResource(Lang.settings_media_resolution)) },
            description = { Text(stringResource(Lang.settings_media_resolution_description)) },
        )

        HorizontalDividerItem()

        val allianceRegexes by remember(state) {
            derivedStateOf { state.defaultMediaPreference.alliancePatterns?.joinToString() ?: "" }
        }
        TextFieldItem(
            value = allianceRegexes,
            title = { Text(stringResource(Lang.settings_media_alliance)) },
            description = {
                Text(stringResource(Lang.settings_media_alliance_description))
            },
            icon = { Icon(Icons.Outlined.Subtitles, null) },
            placeholder = { Text(textAny) },
            onValueChangeCompleted = { new ->
                state.defaultMediaPreferenceState.update(
                    state.defaultMediaPreference.copy(
                        alliancePatterns = new.split(",", "，").map { it.trim() },
                    ),
                )
            },
            sanitizeValue = { it.replace("，", ",") },
        )

        Group(
            title = { Text(stringResource(Lang.settings_media_advanced_settings)) },
            description = { Text(stringResource(Lang.settings_media_advanced_settings_description)) },
            useThinHeader = true,
        ) {
            val mediaSelectorSettings by state.mediaSelectorSettingsState

            kotlin.run {
                val values = remember {
                    MediaSourceKind.selectableEntries + null
                }
                DropdownItem(
                    selected = { mediaSelectorSettings.preferKind },
                    values = { values },
                    itemText = {
                        Text(
                            when (it) {
                                MediaSourceKind.WEB -> stringResource(Lang.settings_media_source_web)
                                MediaSourceKind.BitTorrent -> stringResource(Lang.settings_media_source_bt)
                                null -> stringResource(Lang.settings_media_source_no_preference)
                                MediaSourceKind.LocalCache -> "" // not possible
                            },
                        )
                    },
                    onSelect = {
                        state.mediaSelectorSettingsState.update(mediaSelectorSettings.copy(preferKind = it))
                    },
                    itemIcon = {
                        it?.let {
                            Icon(MediaSourceIcons.kind(it), null)
                        }
                    },
                    title = { Text(stringResource(Lang.settings_media_prefer_source_type)) },
                    description = { Text(stringResource(Lang.settings_media_prefer_source_type_description)) },
                )
            }

            HorizontalDividerItem()

            AniAnimatedVisibility(mediaSelectorSettings.preferKind == MediaSourceKind.WEB) {
                SubGroup {
                    SwitchItem(
                        checked = mediaSelectorSettings.fastSelectWebKind,
                        onCheckedChange = {
                            state.mediaSelectorSettingsState.update(
                                mediaSelectorSettings.copy(fastSelectWebKind = it),
                            )
                        },
                        title = { Text(stringResource(Lang.settings_media_fast_select_web)) },
                        description = { Text(stringResource(Lang.settings_media_fast_select_web_description)) },
                    )

                    HorizontalDividerItem()

                    DropdownItem(
                        selected = { mediaSelectorSettings.fastSelectWebKindAllowNonPreferredDelay },
                        values = {
                            listOf(
                                0.seconds,
                                3.seconds,
                                5.seconds,
                                8.seconds,
                                10.seconds,
                                15.seconds,
                                Duration.INFINITE,
                            )
                        },
                        itemText = { duration ->
                            Text(
                                when (duration) {
                                    0.seconds -> stringResource(Lang.settings_media_wait_time_none)
                                    3.seconds -> stringResource(Lang.settings_media_wait_time_3s)
                                    5.seconds -> stringResource(Lang.settings_media_wait_time_5s)
                                    8.seconds -> stringResource(Lang.settings_media_wait_time_8s)
                                    10.seconds -> stringResource(Lang.settings_media_wait_time_10s)
                                    15.seconds -> stringResource(Lang.settings_media_wait_time_15s)
                                    Duration.INFINITE -> stringResource(Lang.settings_media_wait_time_infinite)
                                    else -> duration.toString() // non-reachable
                                },
                            )
                        },
                        onSelect = {
                            state.mediaSelectorSettingsState.update(
                                mediaSelectorSettings.copy(
                                    fastSelectWebKindAllowNonPreferredDelay = it,
                                ),
                            )
                        },
                        title = { Text(stringResource(Lang.settings_media_max_wait_time)) },
                        description = { Text(stringResource(Lang.settings_media_max_wait_time_description)) },
                        enabled = mediaSelectorSettings.fastSelectWebKind,
                    )

                    HorizontalDividerItem()
                }
            }

            SwitchItem(
                checked = mediaSelectorSettings.showDisabled,
                onCheckedChange = {
                    state.mediaSelectorSettingsState.update(
                        mediaSelectorSettings.copy(showDisabled = it),
                    )
                },
                title = { Text(stringResource(Lang.settings_media_show_disabled)) },
                description = { Text(stringResource(Lang.settings_media_show_disabled_description)) },
            )

            HorizontalDividerItem()

            SwitchItem(
                checked = !state.defaultMediaPreference.showWithoutSubtitle,
                onCheckedChange = {
                    state.defaultMediaPreferenceState.update(
                        state.defaultMediaPreference.copy(showWithoutSubtitle = !it),
                    )
                },
                title = { Text(stringResource(Lang.settings_media_hide_no_subtitle)) },
                description = { Text(stringResource(Lang.settings_media_hide_no_subtitle_description)) },
            )

            HorizontalDividerItem()

            SwitchItem(
                checked = mediaSelectorSettings.hideSingleEpisodeForCompleted,
                onCheckedChange = {
                    state.mediaSelectorSettingsState.update(
                        mediaSelectorSettings.copy(hideSingleEpisodeForCompleted = it),
                    )
                },
                title = { Text(stringResource(Lang.settings_media_hide_single_episode)) },
                description = { Text(stringResource(Lang.settings_media_hide_single_episode_description)) },
            )

            HorizontalDividerItem()

            SwitchItem(
                checked = mediaSelectorSettings.preferSeasons,
                onCheckedChange = {
                    state.mediaSelectorSettingsState.update(
                        mediaSelectorSettings.copy(preferSeasons = it),
                    )
                },
                title = { Text(stringResource(Lang.settings_media_prefer_seasons)) },
                description = { Text(stringResource(Lang.settings_media_prefer_seasons_description)) },
            )

            HorizontalDividerItem()

            SwitchItem(
                checked = mediaSelectorSettings.autoEnableLastSelected,
                onCheckedChange = {
                    state.mediaSelectorSettingsState.update(
                        mediaSelectorSettings.copy(autoEnableLastSelected = it),
                    )
                },
                title = { Text(stringResource(Lang.settings_media_auto_enable_last)) },
                description = { Text(stringResource(Lang.settings_media_auto_enable_last_description)) },
            )

            HorizontalDividerItem()

            SwitchItem(
                checked = mediaSelectorSettings.enableSourceSpeedTest,
                onCheckedChange = {
                    state.mediaSelectorSettingsState.update(
                        mediaSelectorSettings.copy(enableSourceSpeedTest = it),
                    )
                },
                title = { Text(stringResource(Lang.settings_media_speed_test)) },
                description = { Text(stringResource(Lang.settings_media_speed_test_description)) },
            )

            AniAnimatedVisibility(mediaSelectorSettings.enableSourceSpeedTest) {
                SubGroup {
                    DropdownItem(
                        selected = { mediaSelectorSettings.sourceSpeedTestTimeout },
                        values = {
                            listOf(
                                1.seconds,
                                2.seconds,
                                3.seconds,
                                5.seconds,
                                8.seconds,
                                10.seconds,
                            )
                        },
                        itemText = { duration ->
                            Text(
                                when (duration) {
                                    1.seconds -> stringResource(Lang.settings_media_wait_time_1s).replace("After ", "")
                                    2.seconds -> stringResource(Lang.settings_media_wait_time_2s).replace("After ", "")
                                    3.seconds -> stringResource(Lang.settings_media_wait_time_3s).replace("After ", "")
                                    5.seconds -> stringResource(Lang.settings_media_wait_time_5s).replace("After ", "")
                                    8.seconds -> stringResource(Lang.settings_media_wait_time_8s).replace("After ", "")
                                    10.seconds -> stringResource(Lang.settings_media_wait_time_10s).replace("After ", "")
                                    else -> duration.toString()
                                },
                            )
                        },
                        onSelect = {
                            state.mediaSelectorSettingsState.update(
                                mediaSelectorSettings.copy(sourceSpeedTestTimeout = it),
                            )
                        },
                        title = { Text(stringResource(Lang.settings_media_speed_test_timeout)) },
                        description = { Text(stringResource(Lang.settings_media_speed_test_timeout_description)) },
                    )

                    HorizontalDividerItem()

                    val segmentSizeValues = remember {
                        listOf(
                            256L * 1024,
                            512L * 1024,
                            1024L * 1024,
                            2L * 1024 * 1024,
                        )
                    }
                    DropdownItem(
                        selected = { mediaSelectorSettings.sourceSpeedTestSegmentSize },
                        values = { segmentSizeValues },
                        itemText = { size ->
                            Text(
                                when (size) {
                                    256L * 1024 -> stringResource(Lang.settings_media_speed_test_segment_size_256kb)
                                    512L * 1024 -> stringResource(Lang.settings_media_speed_test_segment_size_512kb)
                                    1024L * 1024 -> stringResource(Lang.settings_media_speed_test_segment_size_1mb)
                                    2L * 1024 * 1024 -> stringResource(Lang.settings_media_speed_test_segment_size_2mb)
                                    else -> "${size / 1024} KB"
                                },
                            )
                        },
                        onSelect = {
                            state.mediaSelectorSettingsState.update(
                                mediaSelectorSettings.copy(sourceSpeedTestSegmentSize = it),
                            )
                        },
                        title = { Text(stringResource(Lang.settings_media_speed_test_segment_size)) },
                        description = { Text(stringResource(Lang.settings_media_speed_test_segment_size_description)) },
                    )

                    HorizontalDividerItem()

                    SwitchItem(
                        checked = mediaSelectorSettings.preTestSpeedOnSubjectDetails,
                        onCheckedChange = {
                            state.mediaSelectorSettingsState.update(
                                mediaSelectorSettings.copy(preTestSpeedOnSubjectDetails = it),
                            )
                        },
                        title = { Text(stringResource(Lang.settings_media_pre_test_on_details)) },
                        description = { Text(stringResource(Lang.settings_media_pre_test_on_details_description)) },
                    )

                    HorizontalDividerItem()

                    DropdownItem(
                        selected = { mediaSelectorSettings.preTestSpeedDelayForNewSubject },
                        values = {
                            listOf(
                                0.seconds,
                                1.seconds,
                                2.seconds,
                                3.seconds,
                                5.seconds,
                                8.seconds,
                                10.seconds,
                            )
                        },
                        itemText = { duration ->
                            Text(
                                when (duration) {
                                    0.seconds -> stringResource(Lang.settings_media_wait_time_none).replace("Don't wait", "Immediately").replace("不等待", "立即")
                                    1.seconds -> stringResource(Lang.settings_media_wait_time_1s).replace("After ", "").replace("等待 ", "")
                                    2.seconds -> stringResource(Lang.settings_media_wait_time_2s).replace("After ", "").replace("等待 ", "")
                                    3.seconds -> stringResource(Lang.settings_media_wait_time_3s).replace("After ", "").replace("等待 ", "")
                                    5.seconds -> stringResource(Lang.settings_media_wait_time_5s).replace("After ", "").replace("等待 ", "")
                                    8.seconds -> stringResource(Lang.settings_media_wait_time_8s).replace("After ", "").replace("等待 ", "")
                                    10.seconds -> stringResource(Lang.settings_media_wait_time_10s).replace("After ", "").replace("等待 ", "")
                                    else -> duration.toString()
                                },
                            )
                        },
                        onSelect = {
                            state.mediaSelectorSettingsState.update(
                                mediaSelectorSettings.copy(preTestSpeedDelayForNewSubject = it),
                            )
                        },
                        title = { Text(stringResource(Lang.settings_media_pre_test_delay_for_new)) },
                        description = { Text(stringResource(Lang.settings_media_pre_test_delay_for_new_description)) },
                    )

                    HorizontalDividerItem()
                }
            }
        }
    }
}

@Composable
fun autoCacheDescription(sliderValue: Float) = when (sliderValue) {
    0f -> stringResource(Lang.settings_media_auto_cache_none)
    10f -> stringResource(Lang.settings_media_auto_cache_all)
    else -> stringResource(Lang.settings_media_auto_cache_partial, sliderValue.toInt()) +
            stringResource(Lang.settings_media_auto_cache_space, 600.megaBytes * sliderValue)
}

