package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.AppContainer
import com.example.data.local.PreferencesRepository
import com.example.data.remote.RecitationResourceDto
import com.example.data.remote.TafsirResourceDto
import com.example.data.repository.QuranRepository
import com.example.data.repository.SearchVerseResult
import com.example.data.repository.TafsirResult
import com.example.service.AudioPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val repository: QuranRepository,
    private val preferencesRepository: PreferencesRepository,
    private val audioManager: AudioPlaybackManager
) : ViewModel() {

    private val _tafsirResources = MutableStateFlow<List<TafsirResourceDto>>(emptyList())
    val tafsirResources: StateFlow<List<TafsirResourceDto>> = _tafsirResources.asStateFlow()

    private val _tafsirResult = MutableStateFlow<TafsirResult?>(null)
    val tafsirResult: StateFlow<TafsirResult?> = _tafsirResult.asStateFlow()

    private val _isTafsirLoading = MutableStateFlow(false)
    val isTafsirLoading: StateFlow<Boolean> = _isTafsirLoading.asStateFlow()

    private val _tafsirError = MutableStateFlow<String?>(null)
    val tafsirError: StateFlow<String?> = _tafsirError.asStateFlow()

    private val _reciters = MutableStateFlow<List<RecitationResourceDto>>(emptyList())
    val reciters: StateFlow<List<RecitationResourceDto>> = _reciters.asStateFlow()

    private val _downloadingAudioKeys = MutableStateFlow<Set<String>>(emptySet())
    val downloadingAudioKeys: StateFlow<Set<String>> = _downloadingAudioKeys.asStateFlow()

    private val _audioActionError = MutableStateFlow<String?>(null)
    val audioActionError: StateFlow<String?> = _audioActionError.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchVerseResult>>(emptyList())
    val searchResults: StateFlow<List<SearchVerseResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun loadTafsirResources(language: String) {
        viewModelScope.launch {
            _tafsirResources.value = repository.getTafsirResources(language)
        }
    }

    fun loadTafsirForVerse(tafsirId: Int, verseKey: String) {
        viewModelScope.launch {
            _isTafsirLoading.value = true
            _tafsirError.value = null
            preferencesRepository.setSelectedTafsir(tafsirId)
            val result = repository.getTafsirForVerse(tafsirId, verseKey)
            result.fold(
                onSuccess = {
                    _tafsirResult.value = it
                    _isTafsirLoading.value = false
                },
                onFailure = { err ->
                    _isTafsirLoading.value = false
                    _tafsirError.value = err.message ?: "Failed to load Tafsir"
                }
            )
        }
    }

    fun loadReciters(language: String) {
        viewModelScope.launch {
            _reciters.value = repository.getRecitations(language)
        }
    }

    fun selectReciter(reciterId: Int) {
        viewModelScope.launch {
            preferencesRepository.setSelectedReciter(reciterId)
        }
    }

    fun playSurah(
        reciterId: Int,
        reciterName: String,
        surahId: Int,
        surahNameArabic: String,
        surahNameEnglish: String
    ) {
        viewModelScope.launch {
            _audioActionError.value = null
            val res = repository.resolveChapterAudioUri(reciterId, surahId)
            res.fold(
                onSuccess = { (uri, isOffline) ->
                    audioManager.playUri(
                        uri = uri,
                        surahId = surahId,
                        surahNameArabic = surahNameArabic,
                        surahNameEnglish = surahNameEnglish,
                        reciterName = reciterName,
                        isOffline = isOffline
                    )
                },
                onFailure = { err ->
                    _audioActionError.value = err.message ?: "Audio stream unavailable"
                }
            )
        }
    }

    fun downloadChapterAudio(reciterId: Int, chapterId: Int) {
        val key = "$reciterId:$chapterId"
        if (_downloadingAudioKeys.value.contains(key)) return
        viewModelScope.launch {
            _downloadingAudioKeys.value = _downloadingAudioKeys.value + key
            _audioActionError.value = null
            val res = repository.downloadChapterAudio(reciterId, chapterId)
            res.onFailure { err ->
                _audioActionError.value = err.message ?: "Download failed"
            }
            _downloadingAudioKeys.value = _downloadingAudioKeys.value - key
        }
    }

    fun deleteChapterAudio(reciterId: Int, chapterId: Int) {
        viewModelScope.launch {
            repository.deleteDownloadedAudio(reciterId, chapterId)
        }
    }

    fun search(query: String, language: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }
            _isSearching.value = true
            _searchResults.value = repository.searchQuran(query, language)
            _isSearching.value = false
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExploreViewModel(
                        container.quranRepository,
                        container.preferencesRepository,
                        container.audioPlaybackManager
                    ) as T
                }
            }
    }
}
