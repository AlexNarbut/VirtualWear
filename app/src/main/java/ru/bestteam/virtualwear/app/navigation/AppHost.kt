package ru.bestteam.virtualwear.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.bestteam.virtualwear.app.main.MainScreen
import ru.bestteam.virtualwear.app.main.MainViewModel

@Composable
fun AppHost(navController: AppNavController, startDestination: String) {
    NavHost(
        navController = navController.navController,
        startDestination = startDestination,
    ) {
        appGraph()
    }
}

private fun NavGraphBuilder.appGraph() {
    composable(route = MainDestinations.MAIN_ROUTE) {
        val viewModel = hiltViewModel<MainViewModel>()
        MainScreen(viewModel = viewModel)
    }
}
