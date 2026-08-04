package com.example.medisync.di

import android.app.Application
import androidx.room.Room
import com.example.medisync.data.SettingsManager
import com.example.medisync.data.local.room.MediSyncDatabase
import com.example.medisync.repo.AuthRepository
import com.example.medisync.repo.AuthRepositoryImpl
import com.example.medisync.repo.DocumentRepository
import com.example.medisync.repo.DocumentRepositoryImpl
import com.example.medisync.repo.UserRepository
import com.example.medisync.repo.UserRepositoryImpl
import com.example.medisync.ui.screens.admin.AdminUserProfileViewModel
import com.example.medisync.ui.screens.admin.UserDetailViewModel
import com.example.medisync.ui.screens.admin.UserListViewModel
import com.example.medisync.ui.screens.auth.AuthViewModel
import com.example.medisync.ui.screens.common.ProfileViewModel
import com.example.medisync.ui.screens.user.ReportsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Firebase Instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            MediSyncDatabase::class.java,
            "medisync_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<MediSyncDatabase>().userDao() }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get(), get()) }
    single<DocumentRepository> { DocumentRepositoryImpl(get()) }

    // Settings
    single { SettingsManager(androidContext()) }

    // ViewModels
    viewModel { AuthViewModel(get(), get(), androidContext() as Application) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { UserListViewModel(get(), get(), get()) }
    viewModel { UserDetailViewModel(get(), get()) }
    viewModel { AdminUserProfileViewModel(get()) }
    viewModel { ReportsViewModel(get(), get()) }
}
