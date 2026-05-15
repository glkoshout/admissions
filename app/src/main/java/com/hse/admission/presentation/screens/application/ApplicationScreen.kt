package com.hse.admission.presentation.screens.application

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hse.admission.data.entities.ApplicationEntity
import com.hse.admission.presentation.components.AppButton
import com.hse.admission.presentation.components.ApplicationCard
import com.hse.admission.presentation.viewmodel.ApplicationViewModel
import com.hse.admission.presentation.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationScreen(nav: NavHostController, vm: ApplicationViewModel = hiltViewModel()){
    val context = LocalContext.current
    val search by vm.search.collectAsState(); val state by vm.uiState.collectAsState()
    var facultyExpanded by remember { mutableStateOf(false) }
    var programExpanded by remember { mutableStateOf(false) }
    var faculty by remember { mutableStateOf("не выбрано") }
    var selectedProgram by remember { mutableStateOf("") }
    var selectedForm by remember { mutableStateOf("очно") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<ApplicationEntity?>(null) }
    val faculties = mapOf(
        "Факультет информатики, математики и компьютерных наук" to listOf("Бизнес-информатика","Компьютерные науки и технологии","Прикладная математика и информатика","Программная инженерия","Технологии искусственного и дополненного интеллекта","Фундаментальная и прикладная математика"),
        "Факультет гуманитарных наук" to listOf("Иностранные языки и межкультурная бизнес-коммуникация","Филология","Фундаментальная и прикладная лингвистика"),
        "Факультет менеджмента" to listOf("Международная программа по бизнесу и экономике","Психология в бизнесе","Цифровой маркетинг"),
        "Факультет права" to listOf("Юриспруденция"),
        "Факультет экономики" to listOf("Международная программа по бизнесу и экономике","Экономика и бизнес")
    )
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedPdfUri = uri
        val mime = uri?.let { context.contentResolver.getType(it) }
        error = if (mime == "application/pdf") "" else "Можно выбрать только PDF"
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Подача заявления") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded = facultyExpanded, onExpandedChange = { facultyExpanded = !facultyExpanded }) {
                OutlinedTextField(value = faculty, onValueChange = {}, readOnly = true, label = { Text("Факультет") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultyExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = facultyExpanded, onDismissRequest = { facultyExpanded = false }) {
                    faculties.keys.forEach { f -> DropdownMenuItem(text = { Text(f) }, onClick = { faculty = f; selectedProgram = ""; facultyExpanded = false }) }
                }
            }
            ExposedDropdownMenuBox(expanded = programExpanded, onExpandedChange = { programExpanded = !programExpanded }) {
                OutlinedTextField(value = if (selectedProgram.isBlank()) "не выбрано" else selectedProgram, onValueChange = {}, readOnly = true, label = { Text("Образовательная программа") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = programExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = programExpanded, onDismissRequest = { programExpanded = false }) {
                    faculties[faculty].orEmpty().forEach { pName -> DropdownMenuItem(text = { Text(pName) }, onClick = { selectedProgram = pName; programExpanded = false }) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("очно","заочно","очно-заочно").forEach { form -> FilterChip(selected = selectedForm == form, onClick = { selectedForm = form }, label = { Text(form) }) } }
            AppButton(text = "Прикрепить PDF заявление", modifier = Modifier.fillMaxWidth(), onClick = { pdfPicker.launch(arrayOf("application/pdf")) })
            val file = selectedPdfUri?.let { uri ->
                val c = context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                val n = c?.use { if (it.moveToFirst()) it.getString(0) else null }
                n ?: uri.lastPathSegment
            } ?: "Файл не выбран"
            val shortName = if (file.length > 25) file.take(25) + "..." else file
            Text(shortName)
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            AppButton(text = "Подать заявление", modifier = Modifier.fillMaxWidth(), onClick = {
                if (faculty == "не выбрано") error = "Выберите факультет"
                else if (selectedProgram.isBlank()) error = "Выберите программу"
                else if (selectedPdfUri == null || error.isNotBlank()) error = "Прикрепите корректный PDF"
                else { vm.program.value = "$faculty • $selectedProgram"; vm.educationForm.value = selectedForm; vm.pdfUri.value = selectedPdfUri.toString(); vm.submit({}, { error = it }) }
            })
            AppButton(text = "В главное меню", modifier = Modifier.fillMaxWidth(), onClick = { nav.navigate("home") })
            OutlinedTextField(value = search, onValueChange = { vm.search.value = it }, label = { Text("Поиск по заявлениям") }, modifier = Modifier.fillMaxWidth())
            when (val s = state) {
                UiState.Empty -> Text("Заявления отсутствуют")
                UiState.Loading -> Text("Загрузка...")
                is UiState.Error -> Text(s.msg)
                is UiState.Success -> LazyColumn { items(s.data) { app ->
                    Card(colors = CardDefaults.cardColors(containerColor = com.hse.admission.presentation.components.statusBackgroundColor(app.status))) {
                        Column(Modifier.padding(8.dp)) {
                            ApplicationCard(app.programName, app.status)
                            AppButton(text = "Удалить заявление", onClick = { toDelete = app })
                        }
                    }
                } }
            }
        }
    }
    if (toDelete != null) {
        AlertDialog(onDismissRequest = { toDelete = null }, confirmButton = { TextButton(onClick = { vm.deleteApplication(toDelete!!); toDelete = null }) { Text("Удалить") } }, dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Отмена") } }, title = { Text("Удалить заявление?") }, text = { Text("Вы точно хотите удалить заявление?") })
    }
}
