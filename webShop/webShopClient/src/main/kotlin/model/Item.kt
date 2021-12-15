package model

import kotlinx.serialization.Serializable

@Serializable
data class Item(val name: String, val amount: Int, val price: Double)
