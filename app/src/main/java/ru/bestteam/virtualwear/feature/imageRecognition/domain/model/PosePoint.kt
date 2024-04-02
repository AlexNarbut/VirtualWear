package ru.bestteam.virtualwear.feature.imageRecognition.domain.model

import com.google.mlkit.vision.pose.PoseLandmark

data class PosePoint(val bodyPart: BodyPart, var coordinate: PointCoordinate, val score: Float)

data class PointCoordinate(
    val x: Float,
    val y: Float,
    val z: Float = 0f
)

enum class BodyPart {
    UNKNOWN,

    NOSE,

    LEFT_EYE_INNER,
    LEFT_EYE,
    LEFT_EYE_OUTER,

    RIGHT_EYE_INNER,
    RIGHT_EYE,
    RIGHT_EYE_OUTER,

    LEFT_EAR,
    RIGHT_EAR,

    LEFT_MOUTH,
    RIGHT_MOUTH,

    LEFT_SHOULDER,
    RIGHT_SHOULDER,

    LEFT_ELBOW,
    RIGHT_ELBOW,

    LEFT_WRIST,
    RIGHT_WRIST,

    LEFT_PINKY,
    RIGHT_PINKY,

    LEFT_INDEX,
    RIGHT_INDEX,

    LEFT_THUMB,
    RIGHT_THUMB,

    LEFT_HIP,
    RIGHT_HIP,

    LEFT_KNEE,
    RIGHT_KNEE,

    LEFT_ANKLE,
    RIGHT_ANKLE,

    LEFT_HEEL,
    RIGHT_HEEL,

    LEFT_FOOT_INDEX,
    RIGHT_FOOT_INDEX;

    companion object {
        const val KEY_POINT_NUMBER = 17

        fun fromTensorInt(position: Int): BodyPart = when (position) {
            0 -> NOSE
            1 -> LEFT_EYE
            2 -> RIGHT_EYE
            3 -> LEFT_EAR
            4 -> RIGHT_EAR
            5 -> LEFT_SHOULDER
            6 -> RIGHT_SHOULDER
            7 -> LEFT_ELBOW
            8 -> RIGHT_ELBOW
            9 -> LEFT_WRIST
            10 -> RIGHT_WRIST
            11 -> LEFT_HIP
            12 -> RIGHT_HIP
            13 -> LEFT_KNEE
            14 -> RIGHT_KNEE
            15 -> LEFT_ANKLE
            16 -> RIGHT_ANKLE
            else -> UNKNOWN
        }
    }

    fun fromMlInt(position: Int): BodyPart = when (position) {
        PoseLandmark.NOSE -> NOSE

        PoseLandmark.LEFT_EYE_INNER -> LEFT_EYE_INNER
        PoseLandmark.LEFT_EYE -> LEFT_EYE
        PoseLandmark.LEFT_EYE_OUTER -> LEFT_EYE_OUTER

        PoseLandmark.RIGHT_EYE_INNER -> RIGHT_EYE_INNER
        PoseLandmark.RIGHT_EYE -> RIGHT_EYE
        PoseLandmark.RIGHT_EYE_OUTER -> RIGHT_EYE_OUTER

        PoseLandmark.LEFT_EAR -> LEFT_EAR
        PoseLandmark.RIGHT_EAR -> RIGHT_EAR

        PoseLandmark.LEFT_MOUTH -> LEFT_MOUTH
        PoseLandmark.RIGHT_MOUTH -> RIGHT_MOUTH

        PoseLandmark.LEFT_SHOULDER -> LEFT_SHOULDER
        PoseLandmark.RIGHT_SHOULDER -> RIGHT_SHOULDER

        PoseLandmark.LEFT_ELBOW -> LEFT_ELBOW
        PoseLandmark.RIGHT_ELBOW -> RIGHT_ELBOW

        PoseLandmark.LEFT_WRIST -> LEFT_WRIST
        PoseLandmark.RIGHT_WRIST -> RIGHT_WRIST

        PoseLandmark.LEFT_PINKY -> LEFT_PINKY
        PoseLandmark.RIGHT_PINKY -> RIGHT_PINKY

        PoseLandmark.LEFT_INDEX -> LEFT_INDEX
        PoseLandmark.RIGHT_INDEX -> RIGHT_INDEX

        PoseLandmark.LEFT_THUMB -> LEFT_THUMB
        PoseLandmark.RIGHT_THUMB -> RIGHT_THUMB

        PoseLandmark.LEFT_HIP -> LEFT_HIP
        PoseLandmark.RIGHT_HIP -> RIGHT_HIP

        PoseLandmark.LEFT_KNEE -> LEFT_KNEE
        PoseLandmark.RIGHT_KNEE -> RIGHT_KNEE

        PoseLandmark.LEFT_ANKLE -> LEFT_ANKLE
        PoseLandmark.RIGHT_ANKLE -> RIGHT_ANKLE

        PoseLandmark.LEFT_HEEL -> LEFT_HEEL
        PoseLandmark.RIGHT_HEEL -> RIGHT_HEEL

        PoseLandmark.LEFT_FOOT_INDEX -> LEFT_FOOT_INDEX
        PoseLandmark.RIGHT_FOOT_INDEX -> RIGHT_FOOT_INDEX

        else -> UNKNOWN
    }
}
