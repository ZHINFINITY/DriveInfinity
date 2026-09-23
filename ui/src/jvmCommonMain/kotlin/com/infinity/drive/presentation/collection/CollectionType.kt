package com.infinity.drive.presentation.collection

import org.jetbrains.compose.resources.StringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.vector.ImageVector
import com.infinity.drive.resources.Res
import com.infinity.drive.resources.collection_archived_empty
import com.infinity.drive.resources.collection_archived_subtitle
import com.infinity.drive.resources.collection_archived_title
import com.infinity.drive.resources.collection_favorites_empty
import com.infinity.drive.resources.collection_favorites_subtitle
import com.infinity.drive.resources.collection_favorites_title
import com.infinity.drive.resources.collection_hidden_empty
import com.infinity.drive.resources.collection_hidden_subtitle
import com.infinity.drive.resources.collection_hidden_title

enum class CollectionType(
    val titleRes: StringResource,
    val subtitleRes: StringResource,
    val icon: ImageVector,
    val emptyMessageRes: StringResource
) {
    FAVORITES(
        titleRes = Res.string.collection_favorites_title,
        subtitleRes = Res.string.collection_favorites_subtitle,
        icon = Icons.Filled.Star,
        emptyMessageRes = Res.string.collection_favorites_empty
    ),
    ARCHIVED(
        titleRes = Res.string.collection_archived_title,
        subtitleRes = Res.string.collection_archived_subtitle,
        icon = Icons.Filled.Archive,
        emptyMessageRes = Res.string.collection_archived_empty
    ),
    HIDDEN(
        titleRes = Res.string.collection_hidden_title,
        subtitleRes = Res.string.collection_hidden_subtitle,
        icon = Icons.Filled.VisibilityOff,
        emptyMessageRes = Res.string.collection_hidden_empty
    )
}
