package ru.andreikud.spotifyclone.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ru.andreikud.spotifyclone.R
import ru.andreikud.spotifyclone.data.SongAdapter
import ru.andreikud.spotifyclone.other.Status
import ru.andreikud.spotifyclone.ui.viewmodels.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel
    lateinit var pbAllSongs: ProgressBar
    lateinit var rvAllSongs: RecyclerView

    @Inject
    lateinit var songAdapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        pbAllSongs = view.findViewById(R.id.pbAllSongs)
        rvAllSongs = view.findViewById(R.id.rvAllSongs)

        setupRecyclerView()
        subscribeToObservers()

        songAdapter.setOnItemClickListener { song ->
            mainViewModel.playOrToggleSong(song)
        }
    }

    private fun setupRecyclerView() {
        rvAllSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    pbAllSongs.isVisible = false
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                }
                Status.LOADING -> pbAllSongs.isVisible = true
                Status.ERROR -> Unit
            }
        }
    }
}