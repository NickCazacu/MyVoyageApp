package com.nichita.myvoyage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.nichita.myvoyage.ui.nav.AppNavHost
import com.nichita.myvoyage.ui.theme.BgGradient
import com.nichita.myvoyage.ui.theme.MyVoyageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyVoyageTheme {
                // Градиентный фон «жидкого стекла»: сквозь прозрачные
                // поверхности экранов виден этот градиент.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(BgGradient))
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
