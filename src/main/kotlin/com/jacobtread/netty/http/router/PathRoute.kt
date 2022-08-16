package com.jacobtread.netty.http.router

import com.jacobtread.netty.http.HttpRequest
import com.jacobtread.netty.http.HttpResponse
import com.jacobtread.netty.http.RequestHandler
import io.netty.handler.codec.http.HttpMethod

/**
 * PathRoute Handles matching requests against an url pattern and
 * checking that the method is matching
 *
 * @property method The method that this path route should match
 * if this is null any method will be accepted
 * @property handler The request handler to pass onto if the request is matched
 * @constructor Creates a new path route
 *
 * @see Route for pattern matching
 * @param pattern The pattern to use for matching this route
 */
class PathRoute(
    pattern: String,
    private val method: HttpMethod?,
    private val handler: RequestHandler,
) : Route(pattern) {

    /**
     * handle Handles the matching of this route using the method
     * and url with the underlying Route matching and handles the
     * request, returning the response on success or null
     *
     * @param start The starting point in the request tokens
     * @param request The request that is being made
     * @return The response on success or null
     */
    override fun handle(start: Int, request: HttpRequest): HttpResponse? {
        // Check that the method is null or matching
        if (method != null && method != request.method) return null
        // Match the url with the catch-all inclusive matcher
        if (!matchWithCatchall(start, request)) return null
        // Handle middleware and response
        return  handleMiddleware(request) ?: request.handler()
    }
}
