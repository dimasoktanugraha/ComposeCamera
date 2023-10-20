package com.artera.composecamera.di

import com.artera.composecamera.data.CameraRepository

object Injection {
    fun provideRepository(): CameraRepository {
        return CameraRepository.getInstance()
    }
}