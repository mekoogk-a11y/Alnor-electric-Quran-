package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "al_noor_prefs")

data class UserPreferences(
    val isArabic: Boolean = true,
    val isDarkTheme: Boolean = false,
    val mushafFontSizeSp: Float = 26f,
    val isFlipMode: Boolean = true,
    val showTranslationInScroll: Boolean = false,
    val lastReadPage: Int = 1,
    val lastReadSurahId: Int = 1,
    val lastReadVerseKey: String = "1:1",
    val selectedReciterId: Int = 7, // Mishary Rashid Alafasy default
    val selectedTafsirId: Int = 16, // Tafsir Al-Muyassar (Arabic)
    val selectedTranslationId: Int = 131, // Dr. Mustafa Khattab (English)
    val selectedCityId: String = "makkah",
    val selectedCalculationMethodId: String = "umm_al_qura",
    val customLatitude: Double? = null,
    val customLongitude: Double? = null,
    val customCityName: String? = null,
    val memorizationReminderEnabled: Boolean = false
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_ARABIC = booleanPreferencesKey("is_arabic")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val MUSHAF_FONT_SIZE = floatPreferencesKey("mushaf_font_size")
        val IS_FLIP_MODE = booleanPreferencesKey("is_flip_mode")
        val SHOW_TRANSLATION_IN_SCROLL = booleanPreferencesKey("show_translation_scroll")
        val LAST_READ_PAGE = intPreferencesKey("last_read_page")
        val LAST_READ_SURAH_ID = intPreferencesKey("last_read_surah_id")
        val LAST_READ_VERSE_KEY = stringPreferencesKey("last_read_verse_key")
        val SELECTED_RECITER_ID = intPreferencesKey("selected_reciter_id")
        val SELECTED_TAFSIR_ID = intPreferencesKey("selected_tafsir_id")
        val SELECTED_TRANSLATION_ID = intPreferencesKey("selected_translation_id")
        val SELECTED_CITY_ID = stringPreferencesKey("selected_city_id")
        val SELECTED_METHOD_ID = stringPreferencesKey("selected_method_id")
        val CUSTOM_LAT = doublePreferencesKey("custom_lat")
        val CUSTOM_LON = doublePreferencesKey("custom_lon")
        val CUSTOM_CITY_NAME = stringPreferencesKey("custom_city_name")
        val MEMORIZATION_REMINDER = booleanPreferencesKey("memorization_reminder")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            isArabic = prefs[Keys.IS_ARABIC] ?: true,
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: false,
            mushafFontSizeSp = prefs[Keys.MUSHAF_FONT_SIZE] ?: 26f,
            isFlipMode = prefs[Keys.IS_FLIP_MODE] ?: true,
            showTranslationInScroll = prefs[Keys.SHOW_TRANSLATION_IN_SCROLL] ?: false,
            lastReadPage = prefs[Keys.LAST_READ_PAGE] ?: 1,
            lastReadSurahId = prefs[Keys.LAST_READ_SURAH_ID] ?: 1,
            lastReadVerseKey = prefs[Keys.LAST_READ_VERSE_KEY] ?: "1:1",
            selectedReciterId = prefs[Keys.SELECTED_RECITER_ID] ?: 7,
            selectedTafsirId = prefs[Keys.SELECTED_TAFSIR_ID] ?: 16,
            selectedTranslationId = prefs[Keys.SELECTED_TRANSLATION_ID] ?: 131,
            selectedCityId = prefs[Keys.SELECTED_CITY_ID] ?: "makkah",
            selectedCalculationMethodId = prefs[Keys.SELECTED_METHOD_ID] ?: "umm_al_qura",
            customLatitude = prefs[Keys.CUSTOM_LAT],
            customLongitude = prefs[Keys.CUSTOM_LON],
            customCityName = prefs[Keys.CUSTOM_CITY_NAME],
            memorizationReminderEnabled = prefs[Keys.MEMORIZATION_REMINDER] ?: false
        )
    }

    suspend fun setArabic(isArabic: Boolean) {
        context.dataStore.edit { it[Keys.IS_ARABIC] = isArabic }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_THEME] = isDark }
    }

    suspend fun setMushafFontSize(sizeSp: Float) {
        context.dataStore.edit { it[Keys.MUSHAF_FONT_SIZE] = sizeSp.coerceIn(18f, 40f) }
    }

    suspend fun setFlipMode(isFlip: Boolean) {
        context.dataStore.edit { it[Keys.IS_FLIP_MODE] = isFlip }
    }

    suspend fun setShowTranslationInScroll(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_TRANSLATION_IN_SCROLL] = show }
    }

    suspend fun saveReadingPosition(pageNumber: Int, surahId: Int, verseKey: String) {
        context.dataStore.edit {
            it[Keys.LAST_READ_PAGE] = pageNumber.coerceIn(1, 604)
            it[Keys.LAST_READ_SURAH_ID] = surahId.coerceIn(1, 114)
            it[Keys.LAST_READ_VERSE_KEY] = verseKey
        }
    }

    suspend fun setSelectedReciter(reciterId: Int) {
        context.dataStore.edit { it[Keys.SELECTED_RECITER_ID] = reciterId }
    }

    suspend fun setSelectedTafsir(tafsirId: Int) {
        context.dataStore.edit { it[Keys.SELECTED_TAFSIR_ID] = tafsirId }
    }

    suspend fun setSelectedTranslation(translationId: Int) {
        context.dataStore.edit { it[Keys.SELECTED_TRANSLATION_ID] = translationId }
    }

    suspend fun setPrayerLocation(cityId: String, customLat: Double? = null, customLon: Double? = null, customName: String? = null) {
        context.dataStore.edit {
            it[Keys.SELECTED_CITY_ID] = cityId
            if (customLat != null && customLon != null && customName != null) {
                it[Keys.CUSTOM_LAT] = customLat
                it[Keys.CUSTOM_LON] = customLon
                it[Keys.CUSTOM_CITY_NAME] = customName
            } else {
                it.remove(Keys.CUSTOM_LAT)
                it.remove(Keys.CUSTOM_LON)
                it.remove(Keys.CUSTOM_CITY_NAME)
            }
        }
    }

    suspend fun setCalculationMethod(methodId: String) {
        context.dataStore.edit { it[Keys.SELECTED_METHOD_ID] = methodId }
    }

    suspend fun setMemorizationReminder(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MEMORIZATION_REMINDER] = enabled }
    }
}
