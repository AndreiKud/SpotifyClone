package ru.andreikud.spotifyclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import ru.andreikud.spotifyclone.R
import ru.andreikud.spotifyclone.data.entities.Song
import ru.andreikud.spotifyclone.exoplayer.isPlaying
import ru.andreikud.spotifyclone.exoplayer.toSong
import ru.andreikud.spotifyclone.other.Status
import ru.andreikud.spotifyclone.ui.viewmodels.MainViewModel
import ru.andreikud.spotifyclone.ui.viewmodels.SongViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var ivSongImage: ImageView
    private lateinit var tvSongName: TextView
    private lateinit var ivPlayPauseDetails: ImageView
    private lateinit var ivSkipToPrevious: ImageView
    private lateinit var ivSkipToNext: ImageView
    private lateinit var sbSongPlaytime: SeekBar
    private lateinit var tvCurrPlaytime: TextView
    private lateinit var tvSongDuration: TextView

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()

    private var playbackState: PlaybackStateCompat? = null
    private var shouldUpdateSeekBar = false

    private var currentlyPlayingSong: Song? = null
        set(value) {
            field = value
            value?.let(this::updateTitleAndImage)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()

        ivSongImage = view.findViewById(R.id.ivSongImage)
        tvSongName = view.findViewById(R.id.tvSongName)
        ivPlayPauseDetails = view.findViewById(R.id.ivPlayPauseDetail)
        ivSkipToPrevious = view.findViewById(R.id.ivSkipPrevious)
        ivSkipToNext = view.findViewById(R.id.ivSkip)
        sbSongPlaytime = view.findViewById(R.id.sbSongPlaytime)
        tvCurrPlaytime = view.findViewById(R.id.tvCurrPlaytime)
        tvSongDuration = view.findViewById(R.id.tvSongDuration)

        ivPlayPauseDetails.setOnClickListener {
            currentlyPlayingSong?.let { song ->
                mainViewModel.playOrToggleSong(song, true)
            }
        }

        ivSkipToPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        ivSkipToNext.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        sbSongPlaytime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setPlaytimeToTextView(tvCurrPlaytime, progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = true
                seekBar?.let { nnSeekBar ->
                    mainViewModel.seekTo(nnSeekBar.progress.toLong())
                }
            }
        })
    }

    private fun updateTitleAndImage(song: Song) {
        val title = "${song.title} - ${song.subtitle}"
        tvSongName.text = title
        glide.load(song.logoUrl).into(ivSongImage)
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
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

        mainViewModel.currentlyPlayingSong.observe(viewLifecycleOwner) {
            currentlyPlayingSong = it?.toSong() ?: return@observe
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            ivPlayPauseDetails.setImageResource(
                if (playbackState?.isPlaying == true) {
                    R.drawable.ic_pause
                } else {
                    R.drawable.ic_play
                }
            )
            sbSongPlaytime.progress = playbackState?.position?.toInt() ?: 0
        }

        songViewModel.currentPlayerPosition.observe(viewLifecycleOwner) { playtime ->
            if (shouldUpdateSeekBar) {
                sbSongPlaytime.progress = playtime.toInt()
                setPlaytimeToTextView(tvCurrPlaytime, playtime)
            }
        }

        songViewModel.currentSongDuration.observe(viewLifecycleOwner) { duration ->
            sbSongPlaytime.max = duration.toInt()
            setPlaytimeToTextView(tvSongDuration, duration)
        }
    }

    private fun setPlaytimeToTextView(textView: TextView, timeInMs: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        textView.text = dateFormat.format(timeInMs)
    }
}