package ru.bestteam.virtualwear.app.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.icerock.moko.permissions.PermissionsController
import ru.bestteam.virtualwear.feature.permission.api.PermissionChecker
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {
    @Singleton
    @Provides
    fun providePermissionController(@ApplicationContext context: Context): PermissionsController {
        return PermissionsController(applicationContext = context)
    }

    @Singleton
    @Provides
    fun providePermissionChecker(permissionsController: PermissionsController): PermissionChecker {
        return PermissionChecker.create(permissionsController)
    }
}