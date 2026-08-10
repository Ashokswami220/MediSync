package com.example.medisync

import android.app.Application
import com.cloudinary.android.MediaManager
import com.example.medisync.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MediSyncApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MediSyncApp)
            modules(appModule)
        }

        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )
        MediaManager.init(this, config)
    }
}
