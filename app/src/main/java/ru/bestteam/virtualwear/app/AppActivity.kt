package ru.bestteam.virtualwear.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import ru.bestteam.virtualwear.app.main.MainViewModel
import ru.bestteam.virtualwear.app.navigation.AppHost
import ru.bestteam.virtualwear.app.navigation.MainDestinations
import ru.bestteam.virtualwear.app.navigation.rememberAppNavController
import ru.bestteam.virtualwear.app.ui.theme.MainTheme

@AndroidEntryPoint
class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), 0
            )
        }
        setContent {
            MainTheme {
                val appNavController = rememberAppNavController()
                AppHost(
                    appNavController,
                    MainDestinations.MAIN_ROUTE
                )
            }
        }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}
