/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 姝ゆ簮浠ｇ爜鐨勪娇鐢ㄥ彈 GNU AFFERO GENERAL PUBLIC LICENSE version 3 璁稿彲璇佺殑绾︽潫, 鍙互鍦ㄤ互涓嬮摼鎺ユ壘鍒拌璁稿彲璇?
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.weight
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import me.him188.ani.app.ui.comment.CommentState

@Composable
fun EpisodeCommentsDialog(
    episodeTitle: String,
    state: CommentState,
    onDismissRequest: () -> Unit,
    onClickUrl: (url: String) -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large,
        ) {
            Column {
                TopAppBar(
                    title = { Text("第 $episodeTitle 集评论") },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Rounded.Close, contentDescription = "关闭")
                        }
                    },
                )

                EpisodeCommentColumn(
                    state = state,
                    onClickReply = { },
                    onNewCommentClick = { },
                    onClickUrl = onClickUrl,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
    }
}
