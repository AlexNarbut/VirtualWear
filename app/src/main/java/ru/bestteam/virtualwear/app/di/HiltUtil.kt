package ru.bestteam.virtualwear.app.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry

@Composable
inline fun <reified VM : ViewModel> hiltViewModelForKey(key: String, navBackStackEntry: NavBackStackEntry): VM {
    return viewModel(
        key = key,
        viewModelStoreOwner = navBackStackEntry,
        modelClass = VM::class.java,
        factory = HiltViewModelFactory(
            context = LocalContext.current,
            navBackStackEntry = navBackStackEntry
        )
    )
}