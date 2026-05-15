package com.hse.admission.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hse.admission.data.entities.ApplicationEntity
import com.hse.admission.data.entities.DocumentEntity
import com.hse.admission.data.entities.UserEntity
import com.hse.admission.data.repositories.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState<out T> { object Loading: UiState<Nothing>(); data class Success<T>(val data: T): UiState<T>(); data class Error(val msg: String): UiState<Nothing>(); object Empty: UiState<Nothing>() }

@HiltViewModel
class AuthViewModel @Inject constructor(private val repo: AppRepository): ViewModel() {
    val state = MutableStateFlow<UiState<String>>(UiState.Empty)
    val avatarUri = MutableStateFlow<String?>(null)
    val currentRole = repo.session.session.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null to null)
    fun register(fullName:String,email:String,phone:String,password:String)=viewModelScope.launch{
        val normalizedName = fullName.trim(); val normalizedEmail = email.trim(); val normalizedPhone = phone.trim()
        when {
            normalizedName.isBlank() -> { state.value = UiState.Error("Введите ФИО"); return@launch }
            normalizedEmail.isBlank() -> { state.value = UiState.Error("Введите email"); return@launch }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> { state.value = UiState.Error("Некорректный email"); return@launch }
            normalizedPhone.isBlank() -> { state.value = UiState.Error("Введите телефон"); return@launch }
            normalizedPhone.length < 10 -> { state.value = UiState.Error("Некорректный телефон"); return@launch }
            password.isBlank() -> { state.value = UiState.Error("Введите пароль"); return@launch }
            password.length < 6 -> { state.value = UiState.Error("Пароль должен быть не короче 6 символов"); return@launch }
        }
        if (repo.getUserByEmail(normalizedEmail) != null) { state.value = UiState.Error("Аккаунт с таким email уже существует"); return@launch }
        val id=repo.register(UserEntity(fullName=normalizedName,email=normalizedEmail,phone=normalizedPhone,password=password))
        repo.session.save(id,"applicant"); repo.session.saveAvatar(""); state.value=UiState.Success("ok")
    }
    fun login(email:String,password:String,isAdminTab:Boolean)=viewModelScope.launch{
        val user=repo.login(email,password) ?: if(email=="admin@mail.ru"&&password=="123456") UserEntity(id=-1,fullName="Admin",email=email,phone="",password=password,role="admin") else null
        if(user==null) { state.value=UiState.Error("Неверные данные"); return@launch }
        if (isAdminTab && user.role != "admin") { state.value = UiState.Error("Во вкладке админ могут входить только админы"); return@launch }
        if (!isAdminTab && user.role == "admin") { state.value = UiState.Error("Админ может входить только через вкладку админ"); return@launch }
        repo.session.save(user.id,user.role)
        repo.session.saveAvatar(user.avatarUri ?: "")
        avatarUri.value = user.avatarUri
        state.value=UiState.Success(user.role)
    }
    fun logout() = viewModelScope.launch { repo.session.clear(); state.value = UiState.Empty }
    fun loadCurrentUserAvatar() = viewModelScope.launch {
        val userId = repo.session.session.first().first ?: return@launch
        avatarUri.value = repo.getUser(userId)?.avatarUri ?: repo.session.avatarUri.first()
    }
    fun saveAvatar(uri: String) = viewModelScope.launch {
        val userId = repo.session.session.first().first ?: return@launch
        val user = repo.getUser(userId) ?: return@launch
        repo.updateUser(user.copy(avatarUri = uri))
        repo.session.saveAvatar(uri)
        avatarUri.value = uri
    }

    fun changePassword(oldPassword:String,newPassword:String,onResult:(String)->Unit)=viewModelScope.launch {
        if (newPassword.length < 6) { onResult("Новый пароль должен быть не короче 6 символов"); return@launch }
        val userId = repo.session.session.first().first
        if (userId == null || userId <= 0) { onResult("Пользователь не найден"); return@launch }
        val user = repo.getUser(userId) ?: run { onResult("Пользователь не найден"); return@launch }
        if (user.password != oldPassword) { onResult("Старый пароль введен неверно"); return@launch }
        repo.updateUser(user.copy(password = newPassword))
        onResult("Пароль успешно изменен")
    }
}

@HiltViewModel
class DocumentsViewModel @Inject constructor(private val repo: AppRepository): ViewModel() {
    private val userId = repo.session.session.map { it.first ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
    val sortAsc = MutableStateFlow(false)
    val title = MutableStateFlow("")
    val filePath = MutableStateFlow("")

    val uiState: StateFlow<UiState<List<DocumentEntity>>> = combine(repo.userDocsFlow(userId), sortAsc) { docs, asc ->
        val sorted = if (asc) docs.sortedBy { it.uploadDate } else docs.sortedByDescending { it.uploadDate }
        if (sorted.isEmpty()) UiState.Empty else UiState.Success(sorted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun addDocument(onSuccess: (String) -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        if (title.value.isBlank() || filePath.value.isBlank()) { onError("Заполните все поля документа"); return@launch }
        if (userId.value <= 0L) { onError("Пользователь не найден"); return@launch }
        repo.addDocument(DocumentEntity(userId = userId.value, title = title.value.trim(), filePath = filePath.value.trim()))
        title.value = ""; filePath.value = ""; onSuccess("Документ добавлен")
    }
}

@HiltViewModel
class ApplicationViewModel @Inject constructor(private val repo: AppRepository): ViewModel() {
    private val userId = repo.session.session.map { it.first ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
    val program = MutableStateFlow("")
    val educationForm = MutableStateFlow("")
    val search = MutableStateFlow("")
    val pdfUri = MutableStateFlow("")

    val uiState: StateFlow<UiState<List<ApplicationEntity>>> = combine(repo.userApplicationsFlow(userId), search) { list, q ->
        val filtered = if (q.isBlank()) list else list.filter { it.programName.contains(q, ignoreCase = true) }
        if (filtered.isEmpty()) UiState.Empty else UiState.Success(filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun submit(onSuccess: (String) -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        if (userId.value <= 0L) { onError("Пользователь не найден"); return@launch }
        if (program.value.isBlank() || educationForm.value.isBlank()) { onError("Выберите программу и форму обучения"); return@launch }
        val existing = repo.userApplications(userId.value).first()
        if (existing.any { it.programName == program.value.trim() }) { onError("Заявление на этот факультет и программу уже подано"); return@launch }
        repo.addApplication(ApplicationEntity(userId = userId.value, programName = program.value.trim(), educationForm = educationForm.value.trim(), pdfUri = pdfUri.value, status = "на рассмотрении"))
        onSuccess("Заявление отправлено")
    }
    fun deleteApplication(app: ApplicationEntity) = viewModelScope.launch { repo.deleteApplication(app) }
}

@HiltViewModel
class AdminViewModel @Inject constructor(private val repo: AppRepository): ViewModel() {
    val users = repo.users().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val documents = repo.allDocs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val applications = repo.allApplications().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun updateDocumentStatus(doc: DocumentEntity, status: String) = viewModelScope.launch { repo.updateDoc(doc.copy(status = status)) }
    fun updateApplicationStatus(app: ApplicationEntity, status: String) = viewModelScope.launch { repo.updateApplication(app.copy(status = status)) }
    fun deleteUser(user: UserEntity) = viewModelScope.launch { repo.deleteUser(user) }
}
