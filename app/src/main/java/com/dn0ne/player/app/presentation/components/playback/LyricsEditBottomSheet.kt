package com.dn0ne.player.app.presentation.components.playback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dn0ne.player.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsEditBottomSheet(
    onPasteLyricsClick: () -> Unit,
    onImportFromFileClick: () -> Unit,
    onFetchFromRemoteClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .safeDrawingPadding()
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .padding(top = 8.dp, bottom = 28.dp)
        ) {
            Text(
                text = context.resources.getString(R.string.lyrics_actions),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LyricsEditAction(
                icon = Icons.Rounded.ContentPaste,
                text = context.resources.getString(R.string.paste_lyrics),
                onClick = onPasteLyricsClick
            )

            LyricsEditAction(
                icon = Icons.Rounded.Description,
                text = context.resources.getString(R.string.import_from_file),
                onClick = onImportFromFileClick
            )

            LyricsEditAction(
                icon = Icons.Rounded.Download,
                text = context.resources.getString(R.string.fetch_from_lrclib),
                onClick = onFetchFromRemoteClick
            )
        }
    }
}

@Composable
private fun LyricsEditAction(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeDefaults.Medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
