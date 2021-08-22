package ru.andreikud.spotifyclone.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.andreikud.spotifyclone.data.remote.MusicDatabase
import javax.inject.Inject

enum class State {
    CREATED,
    INITIALIZING,
    INITIALIZED,
    ERROR
}

class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
) {

    var songs = listOf<MediaMetadataCompat>()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state = State.CREATED
        set(value) {
            field = value
            if (value == State.INITIALIZED || value == State.ERROR) {
                synchronized(onReadyListeners) {
                    onReadyListeners.forEach { it(value == State.INITIALIZED) }
                }
            }
        }

    suspend fun fetchSongs() = withContext(Dispatchers.IO) {
        state = State.INITIALIZING
        val allSongs = musicDatabase.getAllSongs()
        songs = allSongs.map { song ->
            Builder()
                .putString(METADATA_KEY_ARTIST, song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.logoUrl)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.logoUrl)
                .build()
        }
        state = State.INITIALIZED
    }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if (state == State.CREATED || state == State.INITIALIZING) {
            onReadyListeners += action
            return false
        }

        action(state == State.INITIALIZED)
        return true
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(
                    MediaItem.Builder()
                        .setUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
                        .setMediaId(song.getString(METADATA_KEY_MEDIA_ID))
                        .build()
                )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()

        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()
}