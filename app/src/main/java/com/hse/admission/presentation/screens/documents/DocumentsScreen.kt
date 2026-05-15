package com.hse.admission.presentation.screens.documents

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hse.admission.presentation.components.AppButton
import com.hse.admission.presentation.components.AppTextField
import com.hse.admission.presentation.components.DocumentCard
import com.hse.admission.presentation.viewmodel.DocumentsViewModel
import com.hse.admission.presentation.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(nav: NavHostController, vm: DocumentsViewModel = hiltViewModel()){
    val title by vm.title.collectAsState(); val filePath by vm.filePath.collectAsState(); val sortAsc by vm.sortAsc.collectAsState(); val state by vm.uiState.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) vm.filePath.value = uri.toString() }
    Scaffold(topBar = { TopAppBar(title = { Text("Документы") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextField(title, { vm.title.value = it }, "Название документа")
            AppButton("Выбрать файл в галерее") { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            Text(if (filePath.isBlank()) "Файл не выбран" else "Выбран: $filePath")
            AppButton("Загрузить") { vm.addDocument({}, {}) }
            AppButton("В главное меню") { nav.navigate("home") }
            Switch(checked = sortAsc, onCheckedChange = { vm.sortAsc.value = it })
            Text(if (sortAsc) "Старые сначала" else "Новые сначала")
            when (val s = state) {
                UiState.Empty -> Text("Документы отсутствуют")
                UiState.Loading -> Text("Загрузка...")
                is UiState.Error -> Text(s.msg)
                is UiState.Success -> LazyColumn { items(s.data) { DocumentCard(it.title, it.status) } }
            }
        }
    }
}
