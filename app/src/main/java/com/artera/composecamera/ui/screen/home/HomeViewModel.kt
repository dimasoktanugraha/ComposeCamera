package com.artera.composecamera.ui.screen.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artera.composecamera.data.CameraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: CameraRepository
) : ViewModel() {

//    private val _uiState: MutableStateFlow<UiState<List<Club>>> = MutableStateFlow(UiState.Loading)
//    val uiState: StateFlow<UiState<List<Club>>>
//        get() = _uiState
//
//    fun getAllHeroes() {
//        viewModelScope.launch {
//            repository.getAllClubs()
//                .catch {
//                    _uiState.value = UiState.Error(it.message.toString())
//                }
//                .collect { heroes ->
//                    _uiState.value = UiState.Success(heroes)
//                }
//        }
//    }
//
//    private val _query = mutableStateOf("")
//    val query: State<String> get() = _query
//
//    fun search(newQuery: String) {
//        _query.value = newQuery
//        viewModelScope.launch {
//            repository.searchClubs(_query.value)
//                .catch {
//                    _uiState.value = UiState.Error(it.message.toString())
//                }
//                .collect { heroes ->
//                    _uiState.value = UiState.Success(heroes)
//                }
//        }
//    }
}