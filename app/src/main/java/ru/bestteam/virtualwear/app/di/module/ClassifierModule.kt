package ru.bestteam.virtualwear.app.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.bestteam.virtualwear.feature.imageRecognition.data.classifier.MlPoseClassifier
import ru.bestteam.virtualwear.feature.imageRecognition.domain.ImagePoseClassifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClassifierModule {
    @Singleton
    @Provides
    fun provideClassifier(@ApplicationContext context: Context): ImagePoseClassifier {
        return MlPoseClassifier()
    }
}