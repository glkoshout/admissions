package com.hse.admission.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hse.admission.presentation.screens.admin.AdminScreen
import com.hse.admission.presentation.screens.application.ApplicationScreen
import com.hse.admission.presentation.screens.auth.LoginScreen
import com.hse.admission.presentation.screens.auth.RegisterScreen
import com.hse.admission.presentation.screens.home.HomeScreen
import com.hse.admission.presentation.screens.profile.ProfileScreen
import com.hse.admission.presentation.screens.splash.SplashScreen
import com.hse.admission.presentation.viewmodel.AuthViewModel

@Composable
fun AppNavHost() {
    val nav = rememberNavController(); val vm: AuthViewModel = hiltViewModel()
    val logout: () -> Unit = {
        vm.logout()
        nav.navigate("splash") {
            popUpTo(nav.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }
    NavHost(nav, startDestination = "splash") {
        composable("splash") { SplashScreen { nav.navigate("login") } }
        composable("login") { LoginScreen(vm, onLogin = { role -> nav.navigate(if(role=="admin") "admin" else "home") }, onRegister = { nav.navigate("register") }) }
        composable("register") { RegisterScreen(vm) { nav.navigate("home") } }
        composable("home") { HomeScreen(nav, onLogout = logout) }
        composable("profile") { ProfileScreen(nav) }
        composable("application") { ApplicationScreen(nav) }
        composable("admin") { AdminScreen(nav, onLogout = logout) }
    }
}
