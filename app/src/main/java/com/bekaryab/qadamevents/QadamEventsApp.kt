package com.bekaryab.qadamevents

import android.app.Application
import com.bekaryab.qadamevents.di.appModule
import com.bekaryab.qadamevents.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class QadamEventsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@QadamEventsApp)
            modules(networkModule, appModule)
        }
    }
}
