package com.ali.nextbus.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Direction(
    @SerialName("direction_id")
    val directionId: Int,
    @SerialName("direction_name")
    val directionName: String
)