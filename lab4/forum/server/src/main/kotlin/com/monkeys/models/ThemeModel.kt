package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
data class ThemeModel(
    val subTheme: String
)