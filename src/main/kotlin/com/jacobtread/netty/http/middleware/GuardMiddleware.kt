package com.jacobtread.netty.http.middleware

import com.jacobtread.netty.http.HttpRequest
import com.jacobtread.netty.http.HttpResponse
import com.jacobtread.netty.http.response
import io.netty.handler.codec.http.HttpResponseStatus

/**
 * Simple structure for a "Guard" middleware which checks
 * whether the request is allowed using [isAllowed] and if
 * that fails then the response is override using [createErrorResponse]
 * which by default uses the [HttpResponseStatus.FORBIDDEN] status
 * an empty body
 *
 * @constructor Create empty Guard middleware
 */
abstract class GuardMiddleware : Middleware {

    /**
     * Checks the request to see if it meets the conditions
     * of this guard used by [handleRequest] to determine
     * the outcome of this middleware
     *
     * @param request The request to check
     * @return Whether the request is allowed by this middleware
     */
    abstract fun isAllowed(request: HttpRequest): Boolean

    /**
     * Creates a response which is used when the request
     * did not meet the conditions in [isAllowed]
     *
     * @param request The request to create the response for
     * @return The created http response
     */
    open fun createErrorResponse(request: HttpRequest): HttpResponse = response(HttpResponseStatus.FORBIDDEN)

    /**
     * Handles the request checking it against the
     * [isAllowed] condition returning an error
     * response if not otherwise returns null
     *
     * @param request The request to check with this guard
     * @return The error response or null if it was allowed
     */
    override fun handleRequest(request: HttpRequest): HttpResponse? {
        if (isAllowed(request)) return null
        return createErrorResponse(request)
    }
}