package ru.bestteam.virtualwear.app

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.icerock.moko.permissions.PermissionsController
import ru.bestteam.virtualwear.app.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    val permissionsController: PermissionsController
) : BaseViewModel()
