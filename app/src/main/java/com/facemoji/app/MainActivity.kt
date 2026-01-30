package com.facemoji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.facemoji.app.ui.FaceMojiScreen
import com.facemoji.app.ui.theme.FaceMojiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceMojiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FaceMojiScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
