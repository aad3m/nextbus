package com.ali.nextbus.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Departure(
    @SerialName("departure_time")
    val departureTime: Long,
    @SerialName("departure_text")
    val departureText: String? = null,
    @SerialName("actual")
    val actual: Boolean,
)

@Serializable
data class DepartureResponse(
    @SerialName("departures")
    val departures: List<Departure>
)