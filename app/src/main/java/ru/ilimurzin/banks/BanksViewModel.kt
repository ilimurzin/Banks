package ru.ilimurzin.banks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BanksViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BanksUiState())
    val uiState: StateFlow<BanksUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
            try {
                val fetchedBanks = Banks.fetch()
                _uiState.value = _uiState.value.copy(banks = fetchedBanks, isLoading = false)
            } catch (e: Exception) {
                Log.e(null, null, e)
                _uiState.value = _uiState.value.copy(isError = true, isLoading = false)
            }
        }
    }
}
