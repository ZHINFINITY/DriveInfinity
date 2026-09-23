package com.infinity.drive.core.media

import coil3.ImageLoader
import coil3.memory.MemoryCache

class CoilThumbnailMemoryCache(
    private val imageLoader: ImageLoader
) : ThumbnailMemoryCache {

    override fun remove(fileId: String) {
        imageLoader.memoryCache?.remove(MemoryCache.Key(thumbnailCacheKey(fileId)))
    }

    override fun clear() {
        imageLoader.memoryCache?.clear()
    }
}
