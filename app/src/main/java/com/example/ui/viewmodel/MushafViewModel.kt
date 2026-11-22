package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.AppContainer
import com.example.data.local.AyahEntity
import com.example.data.local.PreferencesRepository
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MushafPageUiState(
    val isLoading: Boolean = false,
    val verses: List<AyahEntity> = emptyList(),
    val translations: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
)

class MushafViewModel(
    private val repository: QuranRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _pageStates = MutableStateFlow<Map<Int, MushafPageUiState>>(emptyMap())
    val pageStates: StateFlow<Map<Int, MushafPageUiState>> = _pageStates.asStateFlow()

    private val _chapterVerses = MutableStateFlow<List<AyahEntity>>(emptyList())
    val chapterVerses: StateFlow<List<AyahEntity>> = _chapterVerses.asStateFlow()

    private val _chapterTranslations = MutableStateFlow<Map<String, String>>(emptyMap())
    val chapterTranslations: StateFlow<Map<String, String>> = _chapterTranslations.asStateFlow()

    private val _isChapterLoading = MutableStateFlow(false)
    val isChapterLoading: StateFlow<Boolean> = _isChapterLoading.asStateFlow()

    private val _chapterError = MutableStateFlow<String?>(null)
    val chapterError: StateFlow<String?> = _chapterError.asStateFlow()

    fun loadPage(pageNumber: Int, translationId: Int? = null) {
        val safePage = pageNumber.coerceIn(1, 604)
        val existing = _pageStates.value[safePage]
        if (existing != null && (existing.isLoading || (existing.verses.isNotEmpty() && translationId == null))) {
            return
        }
        viewModelScope.launch {
            _pageStates.update { it + (safePage to MushafPageUiState(isLoading = true)) }
            val result = repository.fetchAndCachePage(safePage, translationId)
            result.fold(
                onSuccess = { verses ->
                    val trMap = if (translationId != null) {
                        repository.getCachedTranslations(translationId, verses.map { it.verseKey })
                    } else emptyMap()
                    _pageStates.update {
                        it + (safePage to MushafPageUiState(
                            isLoading = false,
                            verses = verses,
                            translations = trMap
                        ))
                    }
                    // Preload adjacent pages silently
                    preloadPageIfNeeded(safePage + 1)
                },
                onFailure = { err ->
                    _pageStates.update {
                        it + (safePage to MushafPageUiState(
                            isLoading = false,
                            errorMessage = err.message ?: "Network unavailable"
                        ))
                    }
                }
            )
        }
    }

    private fun preloadPageIfNeeded(pageNumber: Int) {
        if (pageNumber !in 1..604) return
        if (_pageStates.value[pageNumber]?.verses?.isNotEmpty() == true) return
        viewModelScope.launch {
            val res = repository.fetchAndCachePage(pageNumber)
            res.onSuccess { verses ->
                _pageStates.update {
                    it + (pageNumber to MushafPageUiState(isLoading = false, verses = verses))
                }
            }
        }
    }

    fun loadChapter(chapterId: Int, translationId: Int?) {
        viewModelScope.launch {
            _isChapterLoading.value = true
            _chapterError.value = null
            val res = repository.fetchAndCacheChapter(chapterId, translationId)
            res.fold(
                onSuccess = { verses ->
                    _chapterVerses.value = verses
                    if (translationId != null) {
                        _chapterTranslations.value = repository.getCachedTranslations(
                            translationId,
                            verses.map { it.verseKey }
                        )
                    }
                    _isChapterLoading.value = false
                },
                onFailure = { err ->
                    _isChapterLoading.value = false
                    _chapterError.value = err.message ?: "Unable to load surah"
                }
            )
        }
    }

    fun saveReadingPosition(pageNumber: Int, surahId: Int, verseKey: String) {
        viewModelScope.launch {
            preferencesRepository.saveReadingPosition(pageNumber, surahId, verseKey)
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MushafViewModel(
                        container.quranRepository,
                        container.preferencesRepository
                    ) as T
                }
            }
    }
}
