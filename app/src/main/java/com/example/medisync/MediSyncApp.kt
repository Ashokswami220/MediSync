package com.example.medisync

import android.app.Application
import com.cloudinary.android.MediaManager
import com.example.medisync.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import java.io.File

class MediSyncApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(File(filesDir, "image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .build()
    }

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
