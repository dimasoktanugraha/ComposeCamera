package com.artera.composecamera.data

class CameraRepository {

    companion object {
        @Volatile
        private var instance: CameraRepository? = null

        fun getInstance(): CameraRepository =
            instance ?: synchronized(this) {
                CameraRepository().apply {
                    instance = this
                }
            }
    }
}