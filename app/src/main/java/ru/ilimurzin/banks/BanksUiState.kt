package ru.ilimurzin.banks

data class BanksUiState(
    val banks: List<Bank> = emptyList(),
    val isLoading: Boolean = true,
    val isError: Boolean = false,
)
