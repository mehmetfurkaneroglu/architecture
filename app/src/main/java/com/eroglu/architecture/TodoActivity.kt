package com.eroglu.architecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eroglu.architecture.ui.theme.ArchitectureTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt'in bu Activity'ye bağımlılık (ViewModel gibi) enjekte etmesini sağlar
class TodoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ArchitectureTheme {
                TodoNavGraph()
            }
        }
    }
}