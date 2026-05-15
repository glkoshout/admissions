package com.hse.admission.presentation.screens.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable fun SplashScreen(onDone:()->Unit){ LaunchedEffect(Unit){ delay(1200); onDone() }; Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){ Column(horizontalAlignment = Alignment.CenterHorizontally){ Text("ВШЭ", style=MaterialTheme.typography.displayMedium); Spacer(Modifier.height(12.dp)); Text("HSE Admission Mobile"); CircularProgressIndicator() } } }
