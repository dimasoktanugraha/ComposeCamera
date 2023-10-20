package com.artera.composecamera.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.artera.composecamera.data.CameraRepository
import com.artera.composecamera.ui.screen.camera.CameraViewModel
import com.artera.composecamera.ui.screen.home.HomeViewModel
import com.artera.composecamera.ui.screen.location.LocationViewModel
import com.artera.composecamera.ui.screen.payment.PaymentViewModel

class ViewModelFactory(private val repository: CameraRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}