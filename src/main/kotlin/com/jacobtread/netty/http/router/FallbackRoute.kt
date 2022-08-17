package com.jacobtread.netty.http.router

import com.jacobtread.netty.http.HttpRequest
import com.jacobtread.netty.http.HttpResponse
import com.jacobtread.netty.http.RequestHandler
import com.jacobtread.netty.http.middleware.Middleware

/**
 * Simple route handler for falling back upon. Will handle
 * any requests without validation should be placed at the
 * end of a routing group to catch any requests that were
 * not match to others.
 *
 * @property handler The request handler
 */
class FallbackRoute(private val handler: RequestHandler) : RouteHandler {

    private var middleware: ArrayList<Middleware>? = null

    override fun handle(start: Int, request: HttpRequest): HttpResponse {
        // Handle middleware and response
        return handleMiddleware(request) ?: request.handler()
    }

    /**
     * Adds middleware to this route
     *
     * @param middlewares The middleware to add
     * @return The self instance to use as a builder
     */
    fun middleware(vararg middlewares: Middleware): FallbackRoute {
        var middleware = middleware
        if (middleware == null) {
            middleware = ArrayList()
            this.middleware = middleware
        }
        middleware.addAll(middlewares)
        return this
    }


    /**
     * Handles middleware for this request
     *
     * @param request The incoming request
     * @return A response from middleware or null
     */
    private fun handleMiddleware(request: HttpRequest): HttpResponse? {
        val middlewares = middleware ?: return null
        for (middleware in middlewares) {
            val response = middleware.handleRequest(request)
            if (response != null) return response
        }
        return null
    }
}