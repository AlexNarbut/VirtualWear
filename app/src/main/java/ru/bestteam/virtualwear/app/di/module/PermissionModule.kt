package ru.bestteam.virtualwear.app.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.icerock.moko.permissions.PermissionsController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {

    @Singleton
    @Provides
    fun providePermissionController(@ApplicationContext context: Context): PermissionsController {
        return PermissionsController(applicationContext = context)
    }
}