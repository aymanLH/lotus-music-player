package com.dn0ne.player.app.data

import android.content.Context
import android.util.Log
import com.dn0ne.player.app.domain.lyrics.Lyrics
import com.dn0ne.player.app.domain.result.DataError
import com.dn0ne.player.app.domain.result.Result
import com.dn0ne.player.app.domain.track.Track
import com.dn0ne.player.app.domain.track.format
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.io.FileOutputStream

class LyricsReaderImpl(private val context: Context) : LyricsReader {
    override fun readFromTag(track: Track): Result<Lyrics?, DataError.Local> {
        var file: File? = null

        return try {
            context.contentResolver.openInputStream(track.uri)?.use { input ->
                val temp = TempAudioCache.create(context, track.format)
                FileOutputStream(temp).use { output ->
                    input.copyTo(output)
                }
                file = temp
            } ?: return Result.Error(DataError.Local.NoReadPermission)

            val temp = file ?: return Result.Error(DataError.Local.FailedToRead)

            val lyricsText = if (track.format.equals("opus", ignoreCase = true)) {
                // jaudiotagger 3.0.1 has no .opus reader, so the OpusTags packet
                // is read at the Ogg page level instead.
                OpusTagEditor.readLyrics(temp)
            } else {
                val audioFile =
                    AudioFileIO.read(temp) ?: return Result.Error(DataError.Local.FailedToRead)
                val tag = audioFile.tagAndConvertOrCreateAndSetDefault
                    ?: return Result.Error(DataError.Local.FailedToRead)

                tag.getFirst(FieldKey.LYRICS)
            }

            if (lyricsText.isNullOrBlank()) return Result.Success(null)

            Result.Success(
                Lyrics(
                    uri = track.uri.toString(),
                    plain = lyricsText.split('\n'),
                    synced = null,
                    areFromRemote = false
                )
            )
        } catch (e: Exception) {
            // jaudiotagger throws a variety of checked exceptions for unsupported
            // or malformed files; report a failed read instead of crashing.
            Log.w(LOG_TAG, "Failed to read lyrics from tag for ${track.uri}", e)
            Result.Error(DataError.Local.FailedToRead)
        } finally {
            file?.delete()
        }
    }

    private companion object {
        const val LOG_TAG = "LyricsReaderImpl"
    }
}
