package com.ali.nextbus

import kotlin.test.Test


/**
 * Integration tests for [MetroTransitClient].
 *
 * These tests hit the live Metro Transit NexTrip API and require network access.
 * They are intended to verify that the client can successfully deserialize real
 * API responses, not to test business logic.
 */
class MetroTransitClientIntegrationTest {

    private val client = MetroTransitClient()

    /**
     * Verifies that the routes endpoint returns a non-empty list and that
     * the response deserializes correctly into [com.ali.nextbus.models.Route] objects.
     */
    @Test
    fun `can fetch routes`() {
        val routes = client.getRoutes()
        assert(routes.isNotEmpty())
    }

    /**
     * Verifies that directions can be fetched for a known route.
     *
     * Uses the METRO Blue Line as a stable, well-known route to look up by label,
     * then asserts that at least one direction is returned for it.
     */
    @Test
    fun `can fetch directions for blue line`() {
        val routes = client.getRoutes()
        val blueLineRoute = routes.first { it.routeLabel.contains("Blue Line", ignoreCase = true) }
        val directions = client.getDirections(blueLineRoute.routeId)
        assert(directions.isNotEmpty())
    }

    /**
     * Verifies that stops can be fetched for a known route and direction.
     *
     * Uses the METRO Green Line as a stable, well-known route to look up by label,
     * then fetches the first direction for that route and asserts that at least one stop is returned.
     */
    @Test
    fun `can fetch stops for green line`() {
        val routes = client.getRoutes()
        val greenLineRoute = routes.first { it.routeLabel.contains("Green Line", ignoreCase = true) }
        val direction = client.getDirections(greenLineRoute.routeId).first()
        val stops = client.getStops(greenLineRoute.routeId, direction.directionId)
        assert(stops.isNotEmpty())
    }

    /**
     * Verifies that departures can be fetched for a known route, direction, and stop.
     *
     * Uses the METRO Gold Line, as a stable, well-known route to look up by label,
     * then fetches the first direction and first stop for that route,
     * and asserts that at least one departure is returned.
     */
    @Test
    fun `can fetch departures for gold line`() {
        val routes = client.getRoutes()
        val goldLineRoute = routes.first { it.routeLabel.contains("Gold Line", ignoreCase = true) }
        val direction = client.getDirections(goldLineRoute.routeId).first()
        val stops = client.getStops(goldLineRoute.routeId, direction.directionId)
        val departures = client.getDepartures(goldLineRoute.routeId, direction.directionId, stops.first().placeCode)
        assert(departures.departures.isNotEmpty())
    }
}