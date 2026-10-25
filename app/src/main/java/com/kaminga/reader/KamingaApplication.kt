package com.kaminga.reader

import android.app.Application
import com.kaminga.reader.di.AppContainer

class KamingaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
