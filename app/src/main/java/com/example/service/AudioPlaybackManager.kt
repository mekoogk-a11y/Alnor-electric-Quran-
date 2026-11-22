package com.example.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AudioRepeatMode {
    NONE,
    REPEAT_ONE,
    REPEAT_ALL
}

data class AudioPlaybackState(
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentSurahId: Int = 1,
    val currentSurahNameArabic: String = "الفاتحة",
    val currentSurahNameEnglish: String = "Al-Fatihah",
    val currentReciterName: String = "مشاري راشد العفاسي",
    val isOfflinePlayback: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: AudioRepeatMode = AudioRepeatMode.NONE,
    val errorMessage: String? = null
)

class AudioPlaybackManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    fun connect() {
        if (controllerFuture != null) return
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                try {
                    val mc = future.get()
                    controller = mc
                    attachPlayerListener(mc)
                    _state.update { it.copy(isConnected = true) }
                } catch (e: Exception) {
                    _state.update { it.copy(errorMessage = e.message) }
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun attachPlayerListener(mc: MediaController) {
        mc.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressPolling() else stopProgressPolling()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        durationMs = mc.duration.coerceAtLeast(0L)
                    )
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = error.localizedMessage ?: "Audio playback error"
                    )
                }
            }
        })
    }

    fun playUri(
        uri: String,
        surahId: Int,
        surahNameArabic: String,
        surahNameEnglish: String,
        reciterName: String,
        isOffline: Boolean
    ) {
        connect()
        val mc = controller
        _state.update {
            it.copy(
                currentSurahId = surahId,
                currentSurahNameArabic = surahNameArabic,
                currentSurahNameEnglish = surahNameEnglish,
                currentReciterName = reciterName,
                isOfflinePlayback = isOffline,
                errorMessage = null,
                isBuffering = true
            )
        }

        val metadata = MediaMetadata.Builder()
            .setTitle("سورة $surahNameArabic")
            .setArtist(reciterName)
            .setAlbumTitle("مصحف النور الإلكتروني")
            .build()

        val item = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()

        if (mc != null) {
            mc.setMediaItem(item)
            mc.setPlaybackSpeed(_state.value.playbackSpeed)
            applyRepeatMode(mc, _state.value.repeatMode)
            mc.prepare()
            mc.play()
        } else {
            scope.launch {
                delay(350)
                controller?.let { ready ->
                    ready.setMediaItem(item)
                    ready.setPlaybackSpeed(_state.value.playbackSpeed)
                    applyRepeatMode(ready, _state.value.repeatMode)
                    ready.prepare()
                    ready.play()
                }
            }
        }
    }

    fun togglePlayPause() {
        val mc = controller ?: return
        if (mc.isPlaying) mc.pause() else mc.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
        _state.update { it.copy(positionMs = positionMs.coerceAtLeast(0L)) }
    }

    fun setSpeed(speed: Float) {
        val safeSpeed = speed.coerceIn(0.5f, 2.0f)
        controller?.setPlaybackSpeed(safeSpeed)
        _state.update { it.copy(playbackSpeed = safeSpeed) }
    }

    fun cycleRepeatMode() {
        val next = when (_state.value.repeatMode) {
            AudioRepeatMode.NONE -> AudioRepeatMode.REPEAT_ONE
            AudioRepeatMode.REPEAT_ONE -> AudioRepeatMode.REPEAT_ALL
            AudioRepeatMode.REPEAT_ALL -> AudioRepeatMode.NONE
        }
        controller?.let { applyRepeatMode(it, next) }
        _state.update { it.copy(repeatMode = next) }
    }

    private fun applyRepeatMode(mc: MediaController, mode: AudioRepeatMode) {
        mc.repeatMode = when (mode) {
            AudioRepeatMode.NONE -> Player.REPEAT_MODE_OFF
            AudioRepeatMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
            AudioRepeatMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                val mc = controller
                if (mc != null && mc.isPlaying) {
                    _state.update {
                        it.copy(
                            positionMs = mc.currentPosition.coerceAtLeast(0L),
                            durationMs = mc.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(400)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressPolling()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        controller = null
    }
}
