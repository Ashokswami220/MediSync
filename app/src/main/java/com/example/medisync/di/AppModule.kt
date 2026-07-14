package com.example.medisync.di

import com.example.medisync.repo.AuthRepository
import com.example.medisync.repo.AuthRepositoryImpl
import com.example.medisync.repo.UserRepository
import com.example.medisync.repo.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import com.example.medisync.ui.screens.auth.AuthViewModel
import com.example.medisync.ui.screens.common.ProfileViewModel
import com.example.medisync.ui.screens.upload.UploadViewModel
import com.example.medisync.ui.screens.admin.UserListViewModel
import com.example.medisync.data.local.room.MediSyncDatabase
import androidx.room.Room
import org.koin.android.ext.koin.androidContext

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
        ).build() 
    }
    single { get<MediSyncDatabase>().userDao() }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    // ViewModels
    viewModel { AuthViewModel(get(), get(), androidContext()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { UploadViewModel() }
    viewModel { UserListViewModel(get()) }
}
