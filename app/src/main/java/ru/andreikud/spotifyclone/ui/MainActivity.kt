package ru.andreikud.spotifyclone.ui

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import ru.andreikud.spotifyclone.BuildConfig
import ru.andreikud.spotifyclone.R
import ru.andreikud.spotifyclone.adapters.SongSwipeAdapter
import ru.andreikud.spotifyclone.data.entities.Song
import ru.andreikud.spotifyclone.exoplayer.toSong
import ru.andreikud.spotifyclone.other.Status
import ru.andreikud.spotifyclone.ui.viewmodels.MainViewModel
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var vpSong: ViewPager2
    private lateinit var ivCurrentSongImage: ImageView

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeAdapter: SongSwipeAdapter

    @Inject
    lateinit var glide: RequestManager

    private var currentlyPlayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        ivCurrentSongImage = findViewById(R.id.ivCurSongImage)
        vpSong = findViewById<ViewPager2>(R.id.vpSong).apply {
            adapter = swipeAdapter
        }

        subscribeToObservers()
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIdx = swipeAdapter.songs.indexOf(song)
        if (newItemIdx != -1) {
            vpSong.currentItem = newItemIdx
            currentlyPlayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide.load((currentlyPlayingSong ?: songs[0]).logoUrl)
                                    .into(ivCurrentSongImage)
                            }
                            switchViewPagerToCurrentSong(currentlyPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        mainViewModel.currentlyPlayingSong.observe(this) {
            if (it == null) {
                return@observe
            }
            currentlyPlayingSong = it.toSong()
            glide.load(currentlyPlayingSong?.logoUrl).into(ivCurrentSongImage)
            switchViewPagerToCurrentSong(currentlyPlayingSong ?: return@observe)
        }
    }
}