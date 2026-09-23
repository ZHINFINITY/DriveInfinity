package com.infinity.drive.presentation.theme

import com.google.android.material.color.MaterialColors

internal actual fun harmonizeColor(argb: Int, primaryArgb: Int): Int =
    MaterialColors.harmonize(argb, primaryArgb)
