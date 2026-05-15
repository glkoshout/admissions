package com.hse.admission

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hse.admission.presentation.navigation.AppNavHost
import com.hse.admission.presentation.theme.HseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HseTheme { AppNavHost() } }
    }
}
