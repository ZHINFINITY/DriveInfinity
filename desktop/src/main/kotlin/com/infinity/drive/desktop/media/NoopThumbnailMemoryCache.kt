package com.infinity.drive.desktop.media

import com.infinity.drive.core.media.ThumbnailMemoryCache

class NoopThumbnailMemoryCache : ThumbnailMemoryCache {

    override fun remove(fileId: String) {
    }

    override fun clear() {
    }
}
