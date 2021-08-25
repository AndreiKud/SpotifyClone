package ru.andreikud.spotifyclone.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.andreikud.spotifyclone.exoplayer.MusicService
import ru.andreikud.spotifyclone.exoplayer.MusicServiceConnection
import ru.andreikud.spotifyclone.exoplayer.currentPlaybackPosition
import ru.andreikud.spotifyclone.other.Constants.UPDATE_PLAYBACK_POSITION_DELAY
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _songDuration = MutableLiveData<Long>()
    val currentSongDuration: LiveData<Long> = _songDuration

    private val _currentPlayerPosition = MutableLiveData<Long>()
    val currentPlayerPosition: LiveData<Long> = _currentPlayerPosition

    init {
        updateCurrentPLayerPosition()
    }

    private fun updateCurrentPLayerPosition() {
        viewModelScope.launch {
            while(true) {
                val position = playbackState.value?.currentPlaybackPosition
                if (currentPlayerPosition.value != position && position != null) {
                    _currentPlayerPosition.postValue(position)
                    _songDuration.postValue(MusicService.songDuration)
                }
                delay(UPDATE_PLAYBACK_POSITION_DELAY)
            }
        }
    }
}