package com.hse.admission.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val fullName: String, val email: String, val phone: String, val password: String, val role: String = "applicant", val avatarUri: String? = null)

@Entity(tableName = "documents")
data class DocumentEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val userId: Long, val title: String, val filePath: String, val status: String = "на проверке", val uploadDate: Long = System.currentTimeMillis())

@Entity(tableName = "applications")
data class ApplicationEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val userId: Long, val programName: String, val educationForm: String, val pdfUri: String = "", val status: String = "черновик", val createdAt: Long = System.currentTimeMillis())
