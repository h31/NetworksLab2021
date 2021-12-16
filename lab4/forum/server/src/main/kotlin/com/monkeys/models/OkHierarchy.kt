package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
class OkHierarchy (
    //list with a pair: main_theme and list of sub_themes
    val response: Map<String, List<String>>
    )