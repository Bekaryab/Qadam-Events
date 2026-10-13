package com.bekaryab.qadamevents.ui.events.components

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decodes a content:// URI to an [ImageBitmap] off the main thread, or null
 * when there is no URI or decoding fails. Callers always keep a tinted
 * placeholder fallback, so images degrade gracefully. No image-loading
 * dependency required.
 */
@Composable
fun rememberContentImageBitmap(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uriString) {
        bitmap = if (uriString == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                decodeContentBitmap(context, Uri.parse(uriString))
            }
        }
    }

    return bitmap
}

private fun decodeContentBitmap(context: Context, uri: Uri): ImageBitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val longest = maxOf(info.size.width, info.size.height)
                if (longest > 1024) {
                    val ratio = 1024.0 / longest
                    decoder.setTargetSize(
                        (info.size.width * ratio).toInt(),
                        (info.size.height * ratio).toInt()
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }.asImageBitmap()
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media
                .getBitmap(context.contentResolver, uri)
                ?.asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
}
