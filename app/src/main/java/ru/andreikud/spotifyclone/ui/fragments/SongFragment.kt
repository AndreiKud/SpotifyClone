package ru.andreikud.spotifyclone.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import ru.andreikud.spotifyclone.R
import ru.andreikud.spotifyclone.data.entities.Song
import ru.andreikud.spotifyclone.exoplayer.toSong
import ru.andreikud.spotifyclone.other.Status
import ru.andreikud.spotifyclone.ui.viewmodels.MainViewModel
import ru.andreikud.spotifyclone.ui.viewmodels.SongViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()

    private var currentlyPlayingSong: Song? = null
        set(value) {
            field = value
            value?.let(this::updateTitleAndImage)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()
    }

    private fun updateTitleAndImage(song: Song) {
        val title = "${song.title} - ${song.subtitle}"
        view?.findViewById<TextView>(R.id.tvSongName)?.apply {
            text = title
        }
        view?.findViewById<ImageView>(R.id.ivSongImage)?.also {
            glide.load(song.logoUrl).into(it)
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if (currentlyPlayingSong == null && songs.isNotEmpty()) {
                                currentlyPlayingSong = songs.first()
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.currentlyPlayingSong.observe(this) {
            if (it == null) {
                return@observe
            }
            currentlyPlayingSong = it.toSong()
        }
    }
}