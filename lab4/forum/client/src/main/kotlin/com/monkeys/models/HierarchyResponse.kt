package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
data class HierarchyResponse (
    //list with a pair: main_theme and list of sub_themes
    val hierarchy: Map<String, List<String>>
)
