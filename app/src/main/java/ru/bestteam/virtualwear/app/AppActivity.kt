package ru.bestteam.virtualwear.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.icerock.moko.permissions.compose.BindEffect
import ru.bestteam.virtualwear.app.navigation.AppHost
import ru.bestteam.virtualwear.app.navigation.MainDestinations
import ru.bestteam.virtualwear.app.navigation.rememberAppNavController
import ru.bestteam.virtualwear.app.ui.theme.MainTheme

@AndroidEntryPoint
class AppActivity : FragmentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                BindEffect(viewModel.permissionsController)

                val appNavController = rememberAppNavController()
                AppHost(
                    appNavController,
                    MainDestinations.MAIN_ROUTE
                )
            }
        }
    }
}
