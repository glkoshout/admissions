package com.hse.admission.data.dao

import androidx.room.*
import com.hse.admission.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert suspend fun insert(user: UserEntity): Long
    @Update suspend fun update(user: UserEntity)
    @Delete suspend fun delete(user: UserEntity)
    @Query("SELECT * FROM users") fun getAll(): Flow<List<UserEntity>>
    @Query("SELECT * FROM users WHERE id=:id") suspend fun getById(id: Long): UserEntity?
    @Query("SELECT * FROM users WHERE email=:email LIMIT 1") suspend fun getByEmail(email: String): UserEntity?
    @Query("SELECT * FROM users WHERE email=:email AND password=:password LIMIT 1") suspend fun login(email: String, password: String): UserEntity?
}

@Dao
interface DocumentDao {
    @Insert suspend fun insert(document: DocumentEntity)
    @Update suspend fun update(document: DocumentEntity)
    @Delete suspend fun delete(document: DocumentEntity)
    @Query("SELECT * FROM documents WHERE id=:id") suspend fun getById(id: Long): DocumentEntity?
    @Query("SELECT * FROM documents WHERE userId=:userId ORDER BY uploadDate DESC") fun getByUser(userId: Long): Flow<List<DocumentEntity>>
    @Query("SELECT * FROM documents") fun getAll(): Flow<List<DocumentEntity>>
}

@Dao
interface ApplicationDao {
    @Insert suspend fun insert(application: ApplicationEntity)
    @Update suspend fun update(application: ApplicationEntity)
    @Delete suspend fun delete(application: ApplicationEntity)
    @Query("SELECT * FROM applications WHERE id=:id") suspend fun getById(id: Long): ApplicationEntity?
    @Query("SELECT * FROM applications WHERE userId=:userId ORDER BY createdAt DESC") fun getByUser(userId: Long): Flow<List<ApplicationEntity>>
    @Query("SELECT * FROM applications") fun getAll(): Flow<List<ApplicationEntity>>
}
