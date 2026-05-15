package com.hse.admission.presentation.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.hse.admission.presentation.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(nav: NavHostController, vm: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val photoUri by vm.avatarUri.collectAsState()
    LaunchedEffect(Unit) { vm.loadCurrentUserAvatar() }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            runCatching { context.contentResolver.takePersistableUriPermission(uri, takeFlags) }
            vm.saveAvatar(uri.toString())
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (photoUri.isNullOrBlank()) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(120.dp), tint = Color(0xFFB8BCC2))
        } else {
            AsyncImage(model = photoUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFB8BCC2)))
        }
        Text("Профиль пользователя", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF003A8C))
        Button(onClick = { photoPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Выбрать фото из галереи") }
        OutlinedTextField(value = oldPassword, onValueChange = { oldPassword = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Старый пароль") })
        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Новый пароль") })
        Button(onClick = { vm.changePassword(oldPassword, newPassword) { message = it } }, modifier = Modifier.fillMaxWidth()) { Text("Сменить пароль") }
        if (message.isNotBlank()) Text(message, color = Color(0xFF003A8C))
        Button(onClick = { nav.navigate("home") }, modifier = Modifier.fillMaxWidth()) { Text("В меню") }
    }
}
