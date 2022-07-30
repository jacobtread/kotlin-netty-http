package com.jacobtread.netty.http.router

import com.jacobtread.netty.http.HttpRequest
import com.jacobtread.netty.http.HttpResponse

/**
 * RouteHandler Represents a Handler for a http request the handler
 * should attempt to match and process the request in the handle function
 * returning a response or returning null if this handler didn't handle
 * the request
 *
 * If null is provided the router / route group should move onto the
 * next handler in the list
 *
 * @constructor Create empty RouteHandler
 */
sealed interface RouteHandler {

    /**
     * handle Handles the logic of this request handler if the return
     * result of this handler is null then the next handler will handle
     * the request
     *
     * @param start The portion index to start at
     * @param request The request to handle
     * @return The request response or null
     */
    fun handle(start: Int, request: HttpRequest): HttpResponse?

}