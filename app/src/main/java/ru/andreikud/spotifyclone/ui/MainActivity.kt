package ru.andreikud.spotifyclone.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.andreikud.spotifyclone.BuildConfig
import ru.andreikud.spotifyclone.R
import ru.andreikud.spotifyclone.adapters.SongSwipeAdapter
import ru.andreikud.spotifyclone.data.entities.Song
import ru.andreikud.spotifyclone.exoplayer.isPlaying
import ru.andreikud.spotifyclone.exoplayer.toSong
import ru.andreikud.spotifyclone.other.Status
import ru.andreikud.spotifyclone.ui.viewmodels.MainViewModel
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var vpSong: ViewPager2
    private lateinit var ivCurrentSongImage: ImageView
    private lateinit var clRoot: ConstraintLayout
    private lateinit var ivPlayPause: ImageView
    private lateinit var fcvNavHostFragment: NavHostFragment

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeAdapter: SongSwipeAdapter

    @Inject
    lateinit var glide: RequestManager

    private var currentlyPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        fcvNavHostFragment = supportFragmentManager.findFragmentById(R.id.fcvNavHostFragment) as NavHostFragment
        ivPlayPause = findViewById(R.id.ivPlayPause)
        clRoot = findViewById(R.id.rootLayout)
        ivCurrentSongImage = findViewById(R.id.ivCurSongImage)
        vpSong = findViewById<ViewPager2>(R.id.vpSong).apply {
            adapter = swipeAdapter
        }

        subscribeToObservers()
        ivPlayPause.setOnClickListener {
            currentlyPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeAdapter.songs[position])
                } else {
                    currentlyPlayingSong = swipeAdapter.songs[position]
                }
            }
        })

        swipeAdapter.setItemClickListener {
            fcvNavHostFragment.findNavController().navigate(
                R.id.actToSongFragment
            )
        }

        fcvNavHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.songFragment -> hideSongSwiper()
                    else -> showSongSwiper()
                }
            }
    }

    private fun hideSongSwiper() {
        ivPlayPause.isVisible = false
        vpSong.isVisible = false
        ivCurrentSongImage.isVisible = false
    }

    private fun showSongSwiper() {
        ivPlayPause.isVisible = true
        vpSong.isVisible = true
        ivCurrentSongImage.isVisible = true
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

        mainViewModel.playbackState.observe(this) {
            playbackState = it
            ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) {
                    R.drawable.ic_pause
                } else {
                    R.drawable.ic_play
                }
            )
        }

        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        Snackbar.make(
                            clRoot,
                            result.message ?: "Unknown error occurred",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        Snackbar.make(
                            clRoot,
                            result.message ?: "Unknown error occurred",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}