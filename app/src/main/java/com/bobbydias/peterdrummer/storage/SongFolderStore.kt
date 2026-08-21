package com.bobbydias.peterdrummer.storage

import android.content.Context
import android.net.Uri

class SongFolderStore(context: Context) {
    private val preferences = context.getSharedPreferences("peter_drummer_settings", Context.MODE_PRIVATE)

    var folderUri: Uri?
        get() = preferences.getString(KEY_FOLDER_URI, null)?.let(Uri::parse)
        set(value) {
            preferences.edit().putString(KEY_FOLDER_URI, value?.toString()).apply()
        }

    var songVolume: Float
        get() = preferences.getFloat(KEY_SONG_VOLUME, 0.82f)
        set(value) = preferences.edit().putFloat(KEY_SONG_VOLUME, value.coerceIn(0f, 1f)).apply()

    var drumVolume: Float
        get() = preferences.getFloat(KEY_DRUM_VOLUME, 0.88f)
        set(value) = preferences.edit().putFloat(KEY_DRUM_VOLUME, value.coerceIn(0f, 1f)).apply()

    companion object {
        private const val KEY_FOLDER_URI = "song_folder_uri"
        private const val KEY_SONG_VOLUME = "song_volume"
        private const val KEY_DRUM_VOLUME = "drum_volume"
    }
}
