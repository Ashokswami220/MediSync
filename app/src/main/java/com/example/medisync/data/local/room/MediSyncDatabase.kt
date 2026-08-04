package com.example.medisync.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UserEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MediSyncDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
