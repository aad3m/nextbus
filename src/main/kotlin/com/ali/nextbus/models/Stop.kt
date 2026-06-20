package com.ali.nextbus.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stop(
    @SerialName("place_code")
    val placeCode: String,
    @SerialName("description")
    val description: String? = null
)