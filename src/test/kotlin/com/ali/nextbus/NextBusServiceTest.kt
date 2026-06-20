package com.ali.nextbus

import com.ali.nextbus.NextBusService.Companion.ambiguousRoute
import com.ali.nextbus.NextBusService.Companion.ambiguousStop
import com.ali.nextbus.NextBusService.Companion.noDeparturesFound
import com.ali.nextbus.NextBusService.Companion.noDirectionFound
import com.ali.nextbus.NextBusService.Companion.noRouteFound
import com.ali.nextbus.NextBusService.Companion.noStopFound
import com.ali.nextbus.models.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertFailsWith

class NextBusServiceTest {

    private val mockClient = mockk<MetroTransitClient>()
    private val service = NextBusService(mockClient)

    @BeforeEach
    fun setUp() {
        every { mockClient.getRoutes() } returns listOf(
            Route(
                routeId = "901",
                agencyId = 0,
                routeLabel = "METRO Blue Line"
            )
        )
        every { mockClient.getDirections("901") } returns listOf(
            Direction(
                directionId = 0,
                directionName = "Eastbound"
            )
        )
        every { mockClient.getStops("901", 0) } returns listOf(
            Stop(
                placeCode = "TF1",
                description = "Target Field Station Platform 1"
            )
        )
        every {
            mockClient.getDepartures(
                routeId = "901",
                directionId = 0,
                placeCode = "TF1"
            )
        } returns DepartureResponse(
            departures = listOf(Departure(departureTime = System.currentTimeMillis() / 1000, actual = true))
        )
    }

    // Happy Path
    @Test
    fun `matches route by partial substring case-insensitively`() {
        val result = service.getMinutesUntilNextBus(
            routeSubstring = "Blue Line",
            direction = "Eastbound",
            stopSubstring = "Target Field Station Platform 1"
        )
        if (result != null) {
            assertTrue(result >= 0)
        }

    }

    @Test
    fun `matches direction by starting substring case-insensitively`() {
        val result = service.getMinutesUntilNextBus(
            routeSubstring = "Metro Blue Line",
            direction = "east",
            stopSubstring = "Target Field Station Platform 1"
        )
        if (result != null) {
            assertTrue(result >= 0)
        }
    }

    @Test
    fun `matches stop by partial substring case-insensitively`() {
        val result = service.getMinutesUntilNextBus(
            routeSubstring = "Metro Blue Line",
            direction = "Eastbound",
            stopSubstring = "Target Field"
        )
        if (result != null) {
            assertTrue(result >= 0)
        }
    }

    @Test
    fun `returns correct minutes until next departure`() {
        val result = service.getMinutesUntilNextBus(
            routeSubstring = "Metro Blue Line",
            direction = "Eastbound",
            stopSubstring = "Target Field Station Platform 1"
        )
        assertEquals(0, result)
    }


    // Sad Path
    @Test
    fun `throws when no matching route found`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            service.getMinutesUntilNextBus(
                routeSubstring = "Gold Line",
                direction = "Target Center",
                stopSubstring = "east"
            )
        }
        assertEquals(noRouteFound("Gold Line"), exception.message)

    }

    @Test
    fun `throws when multiple routes match`() {
        every { mockClient.getRoutes() } returns listOf(
            Route(
                routeId = "901",
                routeLabel = "METRO Blue Line",
                agencyId = 0
            ),
            Route(
                routeId = "902",
                routeLabel = "METRO Blue Line Express",
                agencyId = 0
            )
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            service.getMinutesUntilNextBus(
                routeSubstring = "Blue Line",
                direction = "Eastbound",
                stopSubstring = "Target Field Station Platform 1")
        }
        assertEquals(ambiguousRoute("Blue Line", 2), exception.message)
    }

    @Test
    fun `throws when no matching direction found`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            service.getMinutesUntilNextBus(
                routeSubstring = "METRO Blue Line",
                direction = "Southbound",
                stopSubstring = "Target Field Station Platform 1"
            )
        }
        assertEquals(noDirectionFound("Southbound"), exception.message)
    }

    @Test
    fun `throws when no matching stop found`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            service.getMinutesUntilNextBus(
                routeSubstring = "METRO Blue Line",
                direction = "Eastbound",
                stopSubstring = "Target Center"
            )
        }
        assertEquals(noStopFound("Target Center"), exception.message)
    }

    @Test
    fun `throws when multiple stops match`() {
        every { mockClient.getStops(
            routeId = "901",
            directionId = 0,
        ) } returns listOf(
            Stop(
                placeCode = "TF1",
                description = "Target Field Station Platform 1"
            ),
            Stop(
                placeCode = "TF2",
                description = "Target Field Station Platform 2"
            )
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            service.getMinutesUntilNextBus(
                routeSubstring = "Metro Blue Line",
                direction = "Eastbound",
                stopSubstring = "Target Field"
            )
        }
        assertEquals(ambiguousStop("Target Field", 2), exception.message)
    }

    @Test
    fun `throws when no matching departure found`() {
        every { mockClient.getDepartures(
            routeId = "901",
            directionId = 0,
            placeCode = "TF1") } returns DepartureResponse(
            departures = emptyList()
        )

        val exception = assertFailsWith<IllegalStateException> {
            service.getMinutesUntilNextBus(
                routeSubstring = "METRO Blue Line",
                direction = "Eastbound",
                stopSubstring = "Target Field Station Platform 1"
            )
        }
        assertEquals(noDeparturesFound(
            "METRO Blue Line",
            "Eastbound",
            "Target Field Station Platform 1"),
            exception.message)
    }

    @Test
    fun `returns null when last bus has already departed`() {
        every { mockClient.getDepartures(
            routeId = "901",
            directionId = 0,
            placeCode = "TF1") } returns DepartureResponse(
            departures = listOf(Departure(departureTime = System.currentTimeMillis() / 1000 - 300, actual = true))
        )

        val result = service.getMinutesUntilNextBus(
            routeSubstring = "Metro Blue Line",
            direction = "Eastbound",
            stopSubstring = "Target Field Station Platform 1"
        )
        assertNull(result)
    }

}