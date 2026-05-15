package com.hse.admission.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(primary = Color(0xFF003A8C), secondary = Color(0xFF1976D2), background = Color.White, surfaceVariant = Color(0xFFF0F2F5))
private val DarkColors = darkColorScheme(primary = Color(0xFF6FA8FF), secondary = Color(0xFF8CC5FF))
@Composable fun HseTheme(darkTheme:Boolean = isSystemInDarkTheme(), content:@Composable ()->Unit){ MaterialTheme(colorScheme = if(darkTheme) DarkColors else LightColors, content = content) }
