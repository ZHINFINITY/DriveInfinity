package com.infinity.drive.data.local.dao

import com.infinity.drive.domain.model.FileCategory

/** Row of the storage-by-type rollup. */
data class CategoryUsage(
    val category: FileCategory,
    val fileCount: Int,
    val totalBytes: Long
)
