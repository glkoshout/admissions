package com.hse.admission.presentation.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.hse.admission.data.entities.UserEntity
import com.hse.admission.presentation.components.AppButton
import com.hse.admission.presentation.components.ApplicationCard
import com.hse.admission.presentation.components.statusBackgroundColor
import com.hse.admission.presentation.viewmodel.AdminViewModel
import com.hse.admission.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(nav: NavHostController, onLogout: () -> Unit, vm: AdminViewModel = hiltViewModel(), authVm: AuthViewModel = hiltViewModel()){
    val context = LocalContext.current
    val users by vm.users.collectAsState(); val apps by vm.applications.collectAsState(); val role by authVm.currentRole.collectAsState()
    var archiveTab by remember { mutableStateOf(false) }
    var detailsId by remember { mutableStateOf<Long?>(null) }
    var downloadUri by remember { mutableStateOf<String?>(null) }
    var showUsersDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    val createDoc = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { outUri ->
        if (outUri != null && downloadUri != null) {
            context.contentResolver.openInputStream(Uri.parse(downloadUri))?.use { input ->
                context.contentResolver.openOutputStream(outUri)?.use { output -> input.copyTo(output) }
            }
        }
    }

    LaunchedEffect(role.second) { if (role.second != null && role.second != "admin") nav.navigate("login") }
    val list = if (archiveTab) apps.filter { it.status == "одобрено" || it.status == "отклонено" } else apps.filter { it.status == "на рассмотрении" }

    Scaffold(topBar = { TopAppBar(title = { Text("Панель администратора") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AdminTabSwitch(archiveTab = archiveTab, onChange = { archiveTab = it })
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(list) { app ->
                    val user = users.firstOrNull { it.id == app.userId }
                    Card(colors = CardDefaults.cardColors(containerColor = statusBackgroundColor(app.status))) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val avatarUri = user?.avatarUri
                                if (avatarUri.isNullOrBlank()) Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(40.dp), tint = Color(0xFFB8BCC2))
                                else AsyncImage(model = avatarUri, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                ApplicationCard(title = "${user?.fullName ?: if (archiveTab) "Удаленный пользователь" else "Пользователь"}: ${app.programName}", status = app.status)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                AppButton(text = "Одобрить", modifier = Modifier.weight(1f), onClick = { vm.updateApplicationStatus(app, "одобрено") })
                                AppButton(text = "Отклонить", modifier = Modifier.weight(1f), onClick = { vm.updateApplicationStatus(app, "отклонено") })
                                Button(
                                    onClick = { detailsId = if (detailsId == app.id) null else app.id },
                                    modifier = Modifier.width(78.dp),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Text(text = if (detailsId == app.id) "^" else "˅", fontSize = 26.sp)
                                }
                            }
                            if (detailsId == app.id) {
                                LabeledInfo(label = "ФИО", value = user?.fullName ?: "-")
                                LabeledInfo(label = "Телефон", value = user?.phone ?: "-")
                                LabeledInfo(label = "Почта", value = user?.email ?: "-")
                                val parts = app.programName.split(" • ")
                                LabeledInfoMultiline(label = "Факультет", value = parts.getOrElse(0){"-"})
                                LabeledInfoMultiline(label = "Образовательная программа", value = parts.getOrElse(1){"-"})
                                AppButton(text = "Скачать заявление", onClick = { if (app.pdfUri.isNotBlank()) { downloadUri = app.pdfUri; createDoc.launch("zayavlenie_${app.id}.pdf") } })
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AppButton("Выйти из аккаунта", modifier = Modifier.weight(1f)) { onLogout() }
                AppButton("Список пользователей", modifier = Modifier.weight(1f)) { showUsersDialog = true }
            }
        }
    }

    if (showUsersDialog) {
        AlertDialog(
            onDismissRequest = { showUsersDialog = false },
            title = { Text("Пользователи") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                    items(users) { user ->
                        Card {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (user.avatarUri.isNullOrBlank()) Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(36.dp), tint = Color(0xFFB8BCC2))
                                else AsyncImage(model = user.avatarUri, contentDescription = null, modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = user.fullName, fontWeight = FontWeight.Bold)
                                    Text(text = user.email)
                                }
                                TextButton(onClick = { userToDelete = user }) { Text("Удалить") }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showUsersDialog = false }) { Text("Закрыть") } }
        )
    }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Удалить аккаунт?") },
            text = { Text("Точно ли вы хотите удалить аккаунт пользователя ${user.fullName}?") },
            dismissButton = { TextButton(onClick = { userToDelete = null }) { Text("Отмена") } },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteUser(user)
                    userToDelete = null
                }) { Text("Удалить") }
            }
        )
    }
}


@Composable
private fun LabeledInfo(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "$label:", fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}

@Composable
private fun LabeledInfoMultiline(label: String, value: String) {
    Column {
        Text(text = "$label:", fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}

@Composable
private fun AdminTabSwitch(archiveTab: Boolean, onChange: (Boolean) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(52.dp).background(Color(0xFFE4E8EF), RoundedCornerShape(24.dp)).padding(4.dp)) {
        val segment = maxWidth / 2
        val x by animateDpAsState(if (archiveTab) segment else 0.dp, label = "admin_tab")
        Box(Modifier.offset(x).width(segment - 4.dp).fillMaxHeight().background(Color(0xFF003A8C), RoundedCornerShape(20.dp)))
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text("Текущие заявления", modifier = Modifier.weight(1f).clickable { onChange(false) }, color = if (!archiveTab) Color.White else Color(0xFF1E2430), textAlign = TextAlign.Center)
            Text("Архив", modifier = Modifier.weight(1f).clickable { onChange(true) }, color = if (archiveTab) Color.White else Color(0xFF1E2430), textAlign = TextAlign.Center)
        }
    }
}
