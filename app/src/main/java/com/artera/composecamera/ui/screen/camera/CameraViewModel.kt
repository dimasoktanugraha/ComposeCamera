package com.artera.composecamera.ui.screen.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artera.composecamera.data.CameraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CameraViewModel(
    private val repository: CameraRepository
) : ViewModel() {

//    private val _uiState: MutableStateFlow<UiState<Club>> = MutableStateFlow(UiState.Loading)
//    val uiState: StateFlow<UiState<Club>>
//        get() = _uiState
//
//    fun getClubById(clubId: String) {
//        viewModelScope.launch {
//            _uiState.value = UiState.Loading
//            _uiState.value = UiState.Success(repository.getClubById(clubId))
//        }
//    }
}