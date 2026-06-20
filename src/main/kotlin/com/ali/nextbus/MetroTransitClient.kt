package com.ali.nextbus

import com.ali.nextbus.models.DepartureResponse
import com.ali.nextbus.models.Direction
import com.ali.nextbus.models.Route
import com.ali.nextbus.models.Stop
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class MetroTransitClient {

    private val baseUrl = "https://svc.metrotransit.org/NexTrip"
    private val httpClient = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun getRoutes(): List<Route> {
        val response = get("/Routes")
        return json.decodeFromString(response)
    }

    fun getDirections(routeId: String): List<Direction> {
        val response = get("/Directions/$routeId")
        return json.decodeFromString(response)
    }

    fun getStops(routeId: String, directionId: Int): List<Stop> {
        val response = get("/Stops/$routeId/$directionId")
        return json.decodeFromString(response)
    }

    fun getDepartures(routeId: String, directionId: Int, placeCode: String): DepartureResponse {
        val response = get("/$routeId/$directionId/$placeCode")
        return json.decodeFromString(response)
    }

    private fun get(path: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path?format=json"))
            .header("Accept", "application/json")
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("Request to $path failed with status ${response.statusCode()}")
        }
        return response.body()
    }
}