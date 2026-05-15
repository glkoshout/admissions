package com.hse.admission.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hse.admission.data.dao.*
import com.hse.admission.data.entities.*

@Database(entities = [UserEntity::class, DocumentEntity::class, ApplicationEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun documentDao(): DocumentDao
    abstract fun applicationDao(): ApplicationDao
}
