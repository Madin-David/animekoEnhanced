/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 姝ゆ簮浠ｇ爜鐨勪娇鐢ㄥ彈 GNU AFFERO GENERAL PUBLIC LICENSE version 3 璁稿彲璇佺殑绾︽潫, 鍙互鍦ㄤ互涓嬮摼鎺ユ壘鍒拌璁稿彲璇?
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import me.him188.ani.app.ui.comment.CommentColumn
import me.him188.ani.app.ui.comment.CommentState
import me.him188.ani.app.ui.foundation.LocalImageViewerHandler
import me.him188.ani.app.ui.subject.details.components.SubjectComment

@Composable
fun EpisodeCommentColumn(
    state: CommentState,
    onClickReply: (commentId: Long) -> Unit,
    onNewCommentClick: () -> Unit,
    onClickUrl: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    onClickReply
    onNewCommentClick

    val imageViewer = LocalImageViewerHandler.current

    CommentColumn(
        items = state.list.collectAsLazyPagingItemsWithLifecycle(),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp),
        state = gridState,
    ) { _, comment ->
        SubjectComment(
            comment = comment,
            onClickUrl = onClickUrl,
            onClickImage = { imageViewer.viewImage(it) },
            onClickReaction = { commentId, reactionId ->
                state.submitReaction(commentId, reactionId)
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
