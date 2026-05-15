package com.hse.admission.presentation.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hse.admission.presentation.components.AppButton
import com.hse.admission.presentation.components.AppTextField
import com.hse.admission.presentation.viewmodel.AuthViewModel
import com.hse.admission.presentation.viewmodel.UiState

@Composable
fun LoginScreen(vm: AuthViewModel, onLogin:(String)->Unit, onRegister:()->Unit){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdminMode by remember { mutableStateOf(false) }
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Вход", style = MaterialTheme.typography.headlineSmall)
                RoleModeSwitch(isAdminMode = isAdminMode, onChange = { selectedAdmin ->
                    isAdminMode = selectedAdmin
                    if (!selectedAdmin) email = ""
                })
                AppTextField(email,{email=it},"Email")
                AppTextField(password,{password=it},"Пароль")
                AppButton(text = "Войти", modifier = Modifier.fillMaxWidth(), onClick = { vm.login(email,password,isAdminMode) })
                AnimatedVisibility(visible = !isAdminMode) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = onRegister) { Text("Регистрация") }
                    }
                }
                if(state is UiState.Success<*>) LaunchedEffect(state) { onLogin((state as UiState.Success<String>).data) }
                if(state is UiState.Error) Text((state as UiState.Error).msg, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun RoleModeSwitch(isAdminMode: Boolean, onChange: (Boolean) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(52.dp).background(Color(0xFFE4E8EF), RoundedCornerShape(24.dp)).padding(4.dp)) {
        val segment = maxWidth / 2
        val x by animateDpAsState(if (isAdminMode) segment else 0.dp, label = "switch")
        Box(Modifier.offset(x).width(segment - 4.dp).fillMaxHeight().background(Color(0xFF003A8C), RoundedCornerShape(20.dp)))
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text("Абитуриент", modifier = Modifier.weight(1f).clickable { onChange(false) }, color = if (!isAdminMode) Color.White else Color(0xFF1E2430), textAlign = TextAlign.Center)
            Text("Админ", modifier = Modifier.weight(1f).clickable { onChange(true) }, color = if (isAdminMode) Color.White else Color(0xFF1E2430), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RegisterScreen(vm: AuthViewModel,onSuccess:()->Unit){
    var f by remember { mutableStateOf("") }; var e by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var pass by remember { mutableStateOf("") }
    val st by vm.state.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Регистрация")
                AppTextField(f,{f=it},"ФИО"); AppTextField(e,{e=it},"Email"); AppTextField(p,{p=it},"Телефон"); AppTextField(pass,{pass=it},"Пароль")
                AppButton(text = "Создать аккаунт", modifier = Modifier.fillMaxWidth(), onClick = { vm.register(f,e,p,pass) })
                if(st is UiState.Success<*>) LaunchedEffect(st) { onSuccess() }
                if(st is UiState.Error) Text((st as UiState.Error).msg, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
