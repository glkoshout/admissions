package com.hse.admission.data.repositories

import com.hse.admission.data.dao.*
import com.hse.admission.data.datastore.SessionDataStore
import com.hse.admission.data.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class AppRepository @Inject constructor(private val userDao: UserDao, private val docDao: DocumentDao, private val appDao: ApplicationDao, val session: SessionDataStore) {
    val programs = listOf("Программная инженерия","Компьютерные науки и технологии","Фундаментальная и прикладная математика","Экономика","Менеджмент","Дизайн","Юриспруденция","Психология в бизнесе","Филология")
    suspend fun register(user: UserEntity) = userDao.insert(user)
    suspend fun login(email: String, password: String) = userDao.login(email, password)
    suspend fun getUser(id: Long) = userDao.getById(id)
    suspend fun getUserByEmail(email: String) = userDao.getByEmail(email)
    suspend fun updateUser(user: UserEntity) = userDao.update(user)
    suspend fun deleteUser(user: UserEntity) = userDao.delete(user)
    fun users(): Flow<List<UserEntity>> = userDao.getAll()
    suspend fun addDocument(document: DocumentEntity) = docDao.insert(document)
    fun userDocs(userId: Long) = docDao.getByUser(userId)
    fun userDocsFlow(userIdFlow: Flow<Long>) = userIdFlow.flatMapLatest { id -> docDao.getByUser(id) }
    fun allDocs() = docDao.getAll()
    suspend fun updateDoc(doc: DocumentEntity) = docDao.update(doc)
    suspend fun addApplication(app: ApplicationEntity) = appDao.insert(app)
    fun userApplications(userId: Long) = appDao.getByUser(userId)
    fun userApplicationsFlow(userIdFlow: Flow<Long>) = userIdFlow.flatMapLatest { id -> appDao.getByUser(id) }
    fun allApplications() = appDao.getAll()
    suspend fun updateApplication(app: ApplicationEntity) = appDao.update(app)
    suspend fun deleteApplication(app: ApplicationEntity) = appDao.delete(app)
}
