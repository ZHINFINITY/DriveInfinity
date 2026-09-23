package com.infinity.drive.core.files

/** Removes local copies, asking for platform consent where required. */
interface LocalCopyDeleter {

    fun delete(paths: List<String>): LocalCleanup

    fun isGone(path: String): Boolean
}
