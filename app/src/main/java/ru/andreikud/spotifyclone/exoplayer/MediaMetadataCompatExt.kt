package ru.andreikud.spotifyclone.exoplayer

import android.support.v4.media.MediaMetadataCompat
import ru.andreikud.spotifyclone.data.entities.Song

fun MediaMetadataCompat.toSong(): Song = with(description) {
    Song(
        title.toString(),
        subtitle.toString(),
        mediaUri.toString(),
        iconUri.toString(),
        mediaId.toString()
    )
}