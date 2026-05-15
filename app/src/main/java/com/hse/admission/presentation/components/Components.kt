package com.hse.admission.presentation.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable fun AppButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) = Button(onClick = onClick, modifier = modifier) { Text(text, maxLines = 1, softWrap = false, fontSize = androidx.compose.ui.unit.TextUnit.Unspecified) }
@Composable fun AppTextField(value: String, onChange: (String)->Unit, label: String) = OutlinedTextField(value, onValueChange = onChange, label = { Text(label) })
@Composable fun StatusCard(title:String,status:String)= Card{ ListItem(headlineContent={Text(title)}, supportingContent={Text(status)}) }
@Composable fun DocumentCard(title:String,status:String)=StatusCard(title,status)
fun statusBackgroundColor(status: String) = when(status.lowercase()){ "на рассмотрении" -> androidx.compose.ui.graphics.Color(0xFFE0E0E0); "одобрено", "принято" -> androidx.compose.ui.graphics.Color(0xFFCFE7D3); "отклонено" -> androidx.compose.ui.graphics.Color(0xFFF1D1D1); else -> androidx.compose.ui.graphics.Color(0xFFE0E0E0) }
@Composable fun ApplicationCard(title:String,status:String){ val c= statusBackgroundColor(status); Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c)){ ListItem(headlineContent={Text(title)}, supportingContent={Text(status)}) } }
@Composable fun LoadingScreen() { CircularProgressIndicator() }
