package com.artera.composecamera.ui.common

sealed class CameraUIAction {
    object OnCameraClick : CameraUIAction()
    object OnGalleryViewClick : CameraUIAction()
    object OnSwitchCameraClick : CameraUIAction()
    object OnTimerCameraClick : CameraUIAction()
    object OnFlashCameraClick : CameraUIAction()
}
