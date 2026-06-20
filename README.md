# nextbus

A Kotlin CLI tool that tells you how many minutes until the next bus departure on a given route, direction, and stop using the [Metro Transit NexTrip API](http://svc.metrotransit.org/).

## Prerequisites

- Java 17+
- Gradle (included via wrapper)

## Running

```bash
./gradlew run --args='"<ROUTE>" "<DIRECTION>" "<STOP>"'
```

- `ROUTE` — a substring of the bus route label that uniquely identifies one route
- `DIRECTION` — `north`, `south`, `east`, or `west`
- `STOP` — a substring of the stop description that uniquely identifies one stop on that route

### Examples

```bash
# METRO Blue Line southbound from Target Field
./gradlew run --args='"METRO Blue Line" "south" "Target Field Station Platform 1"'

# METRO Gold Line eastbound
./gradlew run --args='"Gold" "east" "Smith & 5th"'
```

If no more buses are running for the day:
```
No more buses today.
```

## Running Tests

```bash
# Unit tests only 
./gradlew test --tests "com.ali.nextbus.NextBusServiceTest"

# Integration tests (hits the live Metro Transit API)
./gradlew test --tests "com.ali.nextbus.MetroTransitClientIntegrationTest"

# All tests
./gradlew test
```

## Project Structure

```
src/
├── main/kotlin/com/ali/nextbus/
│   ├── Main.kt                  # Entry point, arg parsing, output
│   ├── MetroTransitClient.kt    # HTTP calls to the NexTrip API
│   ├── NextBusService.kt        # Business logic
│   └── models/
│       ├── Route.kt
│       ├── Direction.kt
│       ├── Stop.kt
│       └── Departure.kt
└── test/kotlin/com/ali/nextbus/
    ├── NextBusServiceTest.kt            # Unit tests (mockk)
    └── MetroTransitClientIntegrationTest.kt  # Integration tests (live API)
```

## Design Decisions

**Separation of concerns** — `MetroTransitClient` handles only HTTP and deserialization. All business logic lives in `NextBusService`, which takes the client as a constructor parameter making it fully mockable.

**Substring matching** — per the spec, `ROUTE` and `STOP` inputs are substrings that must uniquely identify one route or stop. If a substring matches multiple results an `IllegalArgumentException` is thrown with a descriptive message.

**No departures** — if the last bus for the day has already left, the app prints `No more buses today.` rather than returning a negative number.

**Departure time** — `departure_time` from the API is a Unix timestamp in seconds. Minutes are calculated as `(departureTime - currentTimeMillis / 1000) / 60`.