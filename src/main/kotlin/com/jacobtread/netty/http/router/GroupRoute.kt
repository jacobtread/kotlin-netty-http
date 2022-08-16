package com.jacobtread.netty.http.router

import com.jacobtread.netty.http.HttpRequest
import com.jacobtread.netty.http.HttpResponse

/**
 * GroupRoute Handles the grouping of multiple routes that are matched
 * after the provided prefix.
 *
 * For example
 * group("/apple") {
 *
 * }
 * Will catch any requests that start with /apple and any routes defined
 * inside that group will be passed the remaining tokens
 *
 * Note: Groups will consume the tokens they match, so they will not be
 * visible to child routes including child groups.
 *
 * Note: Groups will not match catch-all parameters properly resulting in
 * issues
 *
 * @constructor Creates a new group route
 *
 * @see Route for pattern matching
 * @param pattern The pattern to use for matching this group
 */
class GroupRoute(pattern: String) : Route(pattern), RoutingGroup {

    /**
     * routes The list of routes for this route group. Used for routing
     * to child routes in the handle function
     */
    override val routes = ArrayList<RouteHandler>()

    /**
     * handle Handles the matching of this route group as well as
     * the underlying routes if none of the routes match then null
     * is returned resulting in the next route being moved onto
     *
     * @param start The starting point in the request tokens
     * @param request The request that is being made
     * @return The response on success or null
     */
    override fun handle(start: Int, request: HttpRequest): HttpResponse? {
        val tokenCount = tokenCount
        // Try and match the tokens of the request
        if (!matchRange(request, start, tokenCount)) return null
        // The next starting position offset by the consumed tokens
        val offsetStart = start + tokenCount
        // Handle middleware
        val mResponse = handleMiddleware(request)
        if (mResponse != null) return mResponse
        // Try routing with the child routes
        for (route in routes) {
            val response = route.handle(offsetStart, request)
            if (response != null) return response
        }
        return null
    }
}


