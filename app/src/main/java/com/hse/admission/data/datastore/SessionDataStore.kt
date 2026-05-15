package com.hse.admission.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.ds by preferencesDataStore("session")
class SessionDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val userIdKey = longPreferencesKey("user_id")
    private val roleKey = stringPreferencesKey("role")
    private val avatarKey = stringPreferencesKey("avatar_uri")
    val session: Flow<Pair<Long?, String?>> = context.ds.data.map { it[userIdKey] to it[roleKey] }
    suspend fun save(userId: Long, role: String) { context.ds.edit { it[userIdKey] = userId; it[roleKey] = role } }
    suspend fun saveAvatar(uri: String) { context.ds.edit { it[avatarKey] = uri } }
    val avatarUri: Flow<String?> = context.ds.data.map { it[avatarKey] }
    suspend fun clear() { context.ds.edit { prefs -> prefs.remove(userIdKey); prefs.remove(roleKey); prefs.remove(avatarKey) } }
}
