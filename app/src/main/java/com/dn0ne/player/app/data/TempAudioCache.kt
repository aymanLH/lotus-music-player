package com.dn0ne.player.app.data

import android.content.Context
import java.io.File

/**
 * Temporary copies of audio files created by the tag editor and the lyrics reader.
 *
 * These must never be cleaned up by wiping the whole cache directory, because that also
 * deletes Coil's image cache and forces every cover art to be downloaded again.
 */
internal object TempAudioCache {
    private const val FILE_PREFIX = "temp_audio"

    fun create(context: Context, extension: String): File =
        File.createTempFile(FILE_PREFIX, ".$extension", context.cacheDir)

    fun deleteAll(context: Context) {
        context.cacheDir
            ?.listFiles { file -> file.name.startsWith(FILE_PREFIX) }
            ?.forEach { it.delete() }
    }
}
