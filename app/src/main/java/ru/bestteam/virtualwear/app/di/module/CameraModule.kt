package ru.bestteam.virtualwear.app.di.module

import android.content.Context
import android.hardware.camera2.CameraManager
import android.view.WindowManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.bestteam.virtualwear.feature.camera.CameraUtil
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {
    @Singleton
    @Provides
    fun provideCameraUtil(@ApplicationContext context: Context): CameraUtil {
        return CameraUtil(
            context.getSystemService(Context.CAMERA_SERVICE) as CameraManager,
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        )
    }
}