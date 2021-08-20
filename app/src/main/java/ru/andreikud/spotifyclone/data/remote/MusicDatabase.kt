package ru.andreikud.spotifyclone.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ru.andreikud.spotifyclone.data.entities.Song
import ru.andreikud.spotifyclone.other.Constants.SONG_COLLECTION
import timber.log.Timber

class MusicDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs(): List<Song> {
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            Timber.e("Got an exception: ${e.localizedMessage}")
            return emptyList()
        }
    }
}