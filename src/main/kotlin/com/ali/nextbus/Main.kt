
package com.ali.nextbus

fun main(args: Array<String>) {
    if (args.size != 3) {
        println("Usage: nextbus <route> <stop> <direction>")
        return
    }

    val (route, direction, stop) = args
    val client = MetroTransitClient()
    val service = NextBusService(client)

    try {
        val minutes = service.getMinutesUntilNextBus(route, direction, stop)
        if (minutes == null) {
            println("No more buses today.")
        } else {
            println("$minutes Minutes")
        }
    } catch (e: IllegalArgumentException) {
        println("Error: ${e.message}")
    }
}