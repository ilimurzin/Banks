package ru.ilimurzin.banks

data class Bank(
    val bic: String,
    val name: String,
    val nameInEnglish: String? = null,
    val registryNumber: String? = null,
    val addressCombined: String? = null,
)
