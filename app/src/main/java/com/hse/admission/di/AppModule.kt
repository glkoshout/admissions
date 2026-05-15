package com.hse.admission.di

import android.content.Context
import androidx.room.Room
import com.hse.admission.data.dao.*
import com.hse.admission.data.database.AppDatabase
import com.hse.admission.data.repositories.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun db(@ApplicationContext c: Context): AppDatabase = Room.databaseBuilder(c, AppDatabase::class.java, "hse_db").fallbackToDestructiveMigration().build()
    @Provides fun userDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun docDao(db: AppDatabase): DocumentDao = db.documentDao()
    @Provides fun appDao(db: AppDatabase): ApplicationDao = db.applicationDao()
    @Provides @Singleton fun repo(userDao: UserDao, docDao: DocumentDao, appDao: ApplicationDao, session: com.hse.admission.data.datastore.SessionDataStore): AppRepository = AppRepository(userDao, docDao, appDao, session)
}
