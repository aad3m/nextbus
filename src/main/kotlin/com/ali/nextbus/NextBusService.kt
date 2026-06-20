package com.ali.nextbus

import com.ali.nextbus.models.Direction
import com.ali.nextbus.models.Route
import com.ali.nextbus.models.Stop

class NextBusService(private val client: MetroTransitClient) {

    /**
     * Returns the number of minutes until the next bus departure for the given route, direction, and stop.
     *
     * @param routeSubstring A substring of the bus route label that uniquely identifies one route.
     * @param direction The direction of travel — "north", "south", "east", or "west".
     * @param stopSubstring A substring of the stop description that uniquely identifies one stop on the route.
     * @return The number of minutes until the next departure, 0 if the bus is departing now, or null if there are no more departures for today.
     * @throws IllegalArgumentException if the route, direction, or stop cannot be uniquely identified.
     * @throws IllegalStateException if there are no upcoming departures for the resolved stop.
     */
    fun getMinutesUntilNextBus(routeSubstring: String, direction: String, stopSubstring: String): Int? {
        // Find the route by matching the user-provided substring against the route label, case-insensitively
        val route = findRoute(
            routes = client.getRoutes(),
            substring = routeSubstring
        )
        // Get directions for the route, find the one matching the user-provided substring against the direction text, case-insensitively
        val directions = matchDirection(
            directions = client.getDirections(
                routeId = route.routeId),
            substring = direction
        )
        // Get stops for the route and direction, find the one matching the user-provided substring against the stop description, case-insensitively
        val stops = findStop(
            stops = client.getStops(
                routeId = route.routeId,
                directionId = directions.directionId),
            substring = stopSubstring
        )
        // Get departures for the route, direction, and stop, and find the next departure time.
        // If there are no upcoming departures, throw an exception to be handled by the caller.
        val departure = client.getDepartures(
            routeId = route.routeId,
            directionId = directions.directionId,
            placeCode = stops.placeCode
        ).departures.firstOrNull()
         ?: throw IllegalStateException(
            noDeparturesFound(
                route = route.routeLabel,
                direction = directions.directionName,
                stop = stops.description ?: stops.placeCode
            )
         )

        // Calculate the number of minutes until the departure time, based on the current system time.
        // If the departure time is in the past, return null to indicate that there's no more buses for today.
        val minutes = ((departure.departureTime - System.currentTimeMillis() / 1000) / 60).toInt()
        return if (minutes < 0) null else minutes
    }

    /**
     * Handles finding routes by matching a user-provided substring against the route label, case-insensitively.
     *
     * @param  routes The list of routes to search through.
     * @param  substring The user-provided substring to match against the route label.
     * @return The single Route that matches the substring.
     * @throws IllegalArgumentException if no routes match or if multiple routes match the substring.
     */
    private fun findRoute(routes: List<Route>, substring: String): Route {
        val matches = routes.filter {
            it.routeLabel.contains(substring, ignoreCase = true)
        }
        if (matches.isEmpty()) {
            throw IllegalArgumentException(noRouteFound(substring))
        }
        if (matches.size > 1) {
            throw IllegalArgumentException(ambiguousRoute(substring, matches.size))
        }
        return matches.first()
    }

    /**
     * Handles matching direction substring (e.g. "north") to the API's direction text (e.g. "Northbound").
     *
     * @param directions The list of directions to search through.
     * @param substring The user-provided direction substring to match against the direction text.
     * @return The single Direction that matches the input.
     * @throws IllegalArgumentException if no directions match the input.
     */
    private fun matchDirection(directions: List<Direction>, substring: String): Direction {
        val matches = directions.filter {
            it.directionName.startsWith(substring, ignoreCase = true)
        }
        if (matches.isEmpty()) {
            throw IllegalArgumentException(noDirectionFound(substring))
        }
        return matches.first()
    }

    /**
     * Handles finding stops by matching a user-provided substring against the stop description, case-insensitively.
     *
     * @param stops The list of stops to search through.
     * @param substring The user-provided substring to match against the stop description.
     * @return The single Stop that matches the substring.
     * @throws IllegalArgumentException if no stops match or if multiple stops match the substring.
     */
    private fun findStop(stops: List<Stop>, substring: String): Stop {
        val matches = stops.filter {
            it.description?.contains(substring, ignoreCase = true) ?: false
        }
        if (matches.isEmpty()) {
            throw IllegalArgumentException(noStopFound(substring))
        }
        if (matches.size > 1) {
            throw IllegalArgumentException(ambiguousStop(substring, matches.size))
        }
        return matches.first()
    }

    /**
     * Error message generators for various failure cases, to keep the main logic above more readable and to ensure consistent error messages.
     */
    companion object {
        fun noRouteFound(substring: String) = "No route found matching '$substring'"
        fun ambiguousRoute(substring: String, count: Int) = "Ambiguous route substring '$substring' matched $count routes"
        fun noDirectionFound(substring: String) = "No direction found matching '$substring'"
        fun noStopFound(substring: String) = "No stop found matching '$substring'"
        fun ambiguousStop(substring: String, count: Int) = "Ambiguous stop substring '$substring' matched $count stops"
        fun noDeparturesFound(route: String, direction: String, stop: String) =
            "No upcoming departures found for route '$route', direction '$direction', stop '$stop'"
    }
}