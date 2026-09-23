package com.infinity.drive.presentation.platform

/** Filesystem paths of the platform's well-known media folders, empty where absent. */
interface StandardFolderPaths {

    val camera: String?

    val pictures: String?

    val movies: String?
}
