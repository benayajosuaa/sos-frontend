package com.example.sos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.sos.navigation.AppNavigation
import com.example.sos.ui.components.SosBlue
import com.example.sos.ui.components.SosLightBlue

/**
 * Skema warna SOS untuk komponen bawaan Material (date/time picker,
 * checkbox, dialog, dll.) supaya konsisten dengan biru di desain.
 */
private val SosColorScheme = lightColorScheme(
    primary = SosBlue,
    onPrimary = Color.White,
    primaryContainer = SosLightBlue,
    onPrimaryContainer = SosBlue,
    secondary = SosBlue,
    secondaryContainer = SosLightBlue,
    onSecondaryContainer = SosBlue,
    tertiary = SosBlue,
    tertiaryContainer = SosLightBlue,
    onTertiaryContainer = SosBlue,
    background = Color.White,
    surface = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFF1F3F6)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colorScheme = SosColorScheme) {
                Surface {
                    AppNavigation()
                }
            }
        }
    }
}
