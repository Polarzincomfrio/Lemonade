// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.citra.citra_emu.utils

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentActivity
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.key.Keyer
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.request.Options
import coil.transform.RoundedCornersTransformation
import org.citra.citra_emu.R
import org.citra.citra_emu.model.Game
import java.nio.IntBuffer

class GameIconFetcher(
    private val game: Game,
    private val options: Options
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        return DrawableResult(
            drawable = getGameIcon(game.icon)!!.toDrawable(options.context.resources),
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    private fun getGameIcon(vector: IntArray?): Bitmap? {
        // Check for null or empty vector
        if (vector == null || vector.isEmpty()) return null

        try {
            // Assuming the input vector represents a square image, calculate the dimension
            val dimension = Math.sqrt(vector.size.toDouble()).toInt()
        
            // Create a temporary bitmap with the original dimension for better quality
            val tempBitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888)
            tempBitmap.copyPixelsFromBuffer(IntBuffer.wrap(vector))

            // Create the final bitmap with desired dimensions and ARGB_8888 for better quality
            val finalBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        
            // Scale the tempBitmap to fit into the finalBitmap
            val canvas = Canvas(finalBitmap)
            val scale = 48f / dimension
            val matrix = Matrix().apply { postScale(scale, scale) }
            canvas.drawBitmap(tempBitmap, matrix, null)

            return finalBitmap
         } catch (e: Exception) {
             e.printStackTrace()
             return null
         }
     }

    class Factory : Fetcher.Factory<Game> {
        override fun create(data: Game, options: Options, imageLoader: ImageLoader): Fetcher =
            GameIconFetcher(data, options)
    }
}

class GameIconKeyer : Keyer<Game> {
    override fun key(data: Game, options: Options): String = data.path
}

object GameIconUtils {
    fun loadGameIcon(activity: FragmentActivity, game: Game, imageView: ImageView) {
        val imageLoader = ImageLoader.Builder(activity)
            .components {
                add(GameIconKeyer())
                add(GameIconFetcher.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(activity)
                    .maxSizePercent(0.25)
                    .build()
            }
            .build()

        val request = ImageRequest.Builder(activity)
            .data(game)
            .target(imageView)
            .error(R.drawable.no_icon)
            .build()
        imageLoader.enqueue(request)
    }
}
