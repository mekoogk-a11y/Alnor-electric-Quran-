package com.example

import android.app.Application
import com.example.core.AppContainer

class AlNoorApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
