package com.ali.nextbus.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Route(
    @SerialName("route_id")
    val routeId: String,
    @SerialName("agency_id")
    val agencyId: Int,
    @SerialName("route_label")
    val routeLabel: String
)