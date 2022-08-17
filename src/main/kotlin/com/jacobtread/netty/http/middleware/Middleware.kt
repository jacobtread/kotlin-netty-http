package com.jacobtread.netty.http.middleware

import com.jacobtread.netty.http.HttpRequest
import com.jacobtread.netty.http.HttpResponse

/**
 * Middleware are placed before other handlers either
 * on a [group], [middlewareGroup] or placed directly
 * on the route using [Route.middleware].
 *
 * Middleware are executed before any normal route handling
 * and if [handleRequest] returns anything other the null
 * then that response will be used instead of the other
 * handlers being called
 *
 * @constructor Create empty Middleware
 */
fun interface Middleware {

    /**
     * Handles the provided request with this middleware
     * action. If this returns anything other than null its
     * response will be used instead of other handlers.
     *
     * @param request The request to handle
     * @return null to skip this middleware or a response to override
     */
    fun handleRequest(request: HttpRequest): HttpResponse?

}