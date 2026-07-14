package com.example.medisync

import android.app.Application
import org.koin.core.context.startKoin
import org.koin.android.ext.koin.androidContext
import com.example.medisync.di.appModule
import com.cloudinary.android.MediaManager

class MediSyncApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@MediSyncApp)
            modules(appModule)
        }

        // Initialize Cloudinary with only the public cloud_name
        MediaManager.init(this, mapOf("cloud_name" to "era9rd7s"))
    }
}
