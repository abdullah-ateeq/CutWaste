package com.example.cutwaste

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.cutwaste.ui.navigation.CutWasteNavGraph
import com.example.cutwaste.ui.theme.CutWasteTheme
import com.example.cutwaste.ui.viewmodel.CutWasteViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CutWasteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()

            CutWasteTheme(
                appTheme = settings.theme,
                dynamicColor = settings.dynamicColor
            ) {
                CutWasteNavGraph(viewModel = viewModel)
            }
        }
    }
}