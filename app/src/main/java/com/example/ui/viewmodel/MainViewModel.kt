package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.AppContainer
import com.example.core.QuranVerificationReport
import com.example.data.local.BookmarkEntity
import com.example.data.local.DownloadedAudioEntity
import com.example.data.local.MemorizationEntity
import com.example.data.local.PreferencesRepository
import com.example.data.local.SurahEntity
import com.example.data.local.TasbihSessionEntity
import com.example.data.local.UserPreferences
import com.example.data.repository.OfflineDownloadProgress
import com.example.data.repository.QuranRepository
import com.example.service.AudioPlaybackManager
import com.example.service.AudioPlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: QuranRepository,
    private val preferencesRepository: PreferencesRepository,
    private val audioManager: AudioPlaybackManager
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val surahs: StateFlow<List<SurahEntity>> = repository.surahsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memorizationList: StateFlow<List<MemorizationEntity>> = repository.memorizationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasbihSessions: StateFlow<List<TasbihSessionEntity>> = repository.tasbihFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedAudio: StateFlow<List<DownloadedAudioEntity>> = repository.downloadedAudioFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedVersesCount: StateFlow<Int> = repository.cachedVersesCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cachedPagesCount: StateFlow<Int> = repository.cachedPagesCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val downloadProgress: StateFlow<OfflineDownloadProgress> = repository.downloadProgress

    val audioState: StateFlow<AudioPlaybackState> = audioManager.state

    private val _verificationReport = MutableStateFlow<QuranVerificationReport?>(null)
    val verificationReport: StateFlow<QuranVerificationReport?> = _verificationReport.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            audioManager.connect()
            _verificationReport.value = repository.runIntegrityVerification()
        }
    }

    fun setArabic(isArabic: Boolean) = viewModelScope.launch { preferencesRepository.setArabic(isArabic) }
    fun setDarkTheme(isDark: Boolean) = viewModelScope.launch { preferencesRepository.setDarkTheme(isDark) }
    fun setFontSize(sizeSp: Float) = viewModelScope.launch { preferencesRepository.setMushafFontSize(sizeSp) }
    fun setFlipMode(isFlip: Boolean) = viewModelScope.launch { preferencesRepository.setFlipMode(isFlip) }
    fun setShowTranslationInScroll(show: Boolean) = viewModelScope.launch { preferencesRepository.setShowTranslationInScroll(show) }
    fun setMemorizationReminder(enabled: Boolean) = viewModelScope.launch { preferencesRepository.setMemorizationReminder(enabled) }

    fun setPrayerCity(cityId: String) = viewModelScope.launch {
        preferencesRepository.setPrayerLocation(cityId)
    }

    fun setCustomLocation(lat: Double, lon: Double, label: String) = viewModelScope.launch {
        preferencesRepository.setPrayerLocation("custom", lat, lon, label)
    }

    fun setCalculationMethod(methodId: String) = viewModelScope.launch {
        preferencesRepository.setCalculationMethod(methodId)
    }

    fun addBookmark(pageNumber: Int, surahId: Int, surahNameArabic: String, verseKey: String?) = viewModelScope.launch {
        repository.addBookmark(pageNumber, surahId, surahNameArabic, verseKey)
    }

    fun removeBookmarkByPage(pageNumber: Int) = viewModelScope.launch {
        repository.removeBookmarkByPage(pageNumber)
    }

    fun removeBookmarkById(id: Int) = viewModelScope.launch {
        repository.removeBookmarkById(id)
    }

    fun updateMemorization(entry: MemorizationEntity) = viewModelScope.launch {
        repository.updateMemorization(entry)
    }

    fun incrementTasbih(session: TasbihSessionEntity) = viewModelScope.launch {
        repository.updateTasbihSession(
            session.copy(
                currentCount = session.currentCount + 1,
                totalLifetimeCount = session.totalLifetimeCount + 1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun decrementTasbih(session: TasbihSessionEntity) = viewModelScope.launch {
        if (session.currentCount > 0) {
            repository.updateTasbihSession(
                session.copy(
                    currentCount = session.currentCount - 1,
                    totalLifetimeCount = (session.totalLifetimeCount - 1).coerceAtLeast(0),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun resetTasbih(session: TasbihSessionEntity) = viewModelScope.launch {
        repository.updateTasbihSession(
            session.copy(
                currentCount = 0,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun updateTasbihTarget(session: TasbihSessionEntity, target: Int) = viewModelScope.launch {
        repository.updateTasbihSession(
            session.copy(targetCount = target, updatedAt = System.currentTimeMillis())
        )
    }

    fun startFullQuranDownload() = viewModelScope.launch {
        repository.downloadFullQuranOffline()
        _verificationReport.value = repository.runIntegrityVerification()
    }

    fun runVerification() = viewModelScope.launch {
        _verificationReport.value = repository.runIntegrityVerification()
    }

    fun toggleAudioPlayPause() = audioManager.togglePlayPause()
    fun seekAudio(positionMs: Long) = audioManager.seekTo(positionMs)
    fun setAudioSpeed(speed: Float) = audioManager.setSpeed(speed)
    fun cycleAudioRepeat() = audioManager.cycleRepeatMode()

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(
                        container.quranRepository,
                        container.preferencesRepository,
                        container.audioPlaybackManager
                    ) as T
                }
            }
    }
}
