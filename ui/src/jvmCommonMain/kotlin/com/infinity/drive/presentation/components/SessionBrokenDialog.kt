package com.infinity.drive.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.window.DialogProperties
import com.infinity.drive.resources.Res
import com.infinity.drive.resources.session_broken_action
import com.infinity.drive.resources.session_broken_message
import com.infinity.drive.resources.session_broken_title

/**
 * Shown when the stored session cannot be read, which leaves the app unable to
 * reach Telegram at all. There is nothing to go back to, so the dialog has no
 * way out other than signing in again.
 */
@Composable
fun SessionBrokenDialog(onSignInAgain: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = { Text(stringResource(Res.string.session_broken_title)) },
        text = { Text(stringResource(Res.string.session_broken_message)) },
        confirmButton = {
            Button(onClick = onSignInAgain, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(Res.string.session_broken_action))
            }
        }
    )
}
