package com.example.cutwaste.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Splash : Screen("splash", "Splash")
    data object Calculator : Screen("calculator", "Calculator", Icons.Default.Calculate)
    data object History : Screen("history", "History", Icons.Default.History)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object About : Screen("about", "About", Icons.Default.Info)

    companion object {
        val bottomNavItems = listOf(
            Calculator,
            History,
            Settings,
            About
        )
    }
}
