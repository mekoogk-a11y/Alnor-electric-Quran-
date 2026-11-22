package com.example.core

import android.content.Context
import com.example.data.local.AlNoorDatabase
import com.example.data.local.PreferencesRepository
import com.example.data.repository.QuranRepository
import com.example.service.AudioPlaybackManager

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AlNoorDatabase by lazy {
        AlNoorDatabase.getInstance(appContext)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(appContext)
    }

    val quranRepository: QuranRepository by lazy {
        QuranRepository(appContext, database.quranDao())
    }

    val audioPlaybackManager: AudioPlaybackManager by lazy {
        AudioPlaybackManager(appContext)
    }
}
