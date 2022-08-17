package com.jacobtread.netty.http.router

import com.jacobtread.netty.http.HttpRequest
import com.jacobtread.netty.http.HttpResponse
import com.jacobtread.netty.http.middleware.Middleware

/**
 * Route Represents a route handler which matches requests against
 * patterns. This class provides a way for child classes to match
 * url patterns.
 *
 * @constructor Creates a new route
 * @param pattern The url pattern for this route leading and trailing slashes are omitted
 * (e.g "/apple/banana" and "apple/banana/" is equivalent to "apple/banana" this is to avoid weird parsing)
 * you can provide parameters for matching a route using : then the name of the parameter
 * (e.g /:path) they can be accessed on the request object using the param functions in the case of
 * /:path it can be accessed using param("path") you can catch any number of remaining tokens using
 * the catch-all parameter which is :* this can be accessed as a parameter using param("*")
 * Note: The catch-all parameter can only be used as the last parameter attempting to use
 * it anywhere else will result in it simply only matching one token
 */
abstract class Route internal constructor(pattern: String) : RouteHandler {

    private var middleware: ArrayList<Middleware>? = null

    /**
     * patternTokens A list containing the individual tokens of the
     * route pattern the leading and trailing slashes are removed
     * before splitting. This is used when comparing to url tokens
     */
    private val patternTokens: List<String> = pattern
        .removePrefix("/")
        .removePrefix("/")
        .split('/')
        .filter { it.isNotBlank() }

    /**
     * tokenCount Returns the number of tokens this pattern contains
     */
    val tokenCount: Int get() = patternTokens.size

    /**
     * Adds middleware to this route
     *
     * @param middlewares The middleware to add
     * @return The self instance to use as a builder
     */
    fun middleware(vararg middlewares: Middleware): Route {
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
    fun handleMiddleware(request: HttpRequest): HttpResponse? {
        val middlewares = middleware ?: return null
        for (middleware in middlewares) {
            val response = middleware.handleRequest(request)
            if (response != null) return response
        }
        return null
    }

    /**
     * matchRange Matches the specified range of request tokens from the start index
     * to the end index. Will return true if the range matches or false if it doesn't
     *
     * @param request The http request used for its tokens and for
     * setting parameters that have been matched
     * @param startIndex The index of the request tokens to start matching
     * @param count The total number of tokens to match
     * @return True if all the tokens match otherwise false
     */
    fun matchRange(request: HttpRequest, startIndex: Int, count: Int): Boolean {
        if (count == 0) return true
        val requestTokens = request.tokens
        // If we don't have enough tokens
        if ((requestTokens.size - startIndex) < count) return false
        for (i in 0 until count) {
            val token = patternTokens[i]
            val value = requestTokens[startIndex + i]
            if (token.startsWith(':')) { // If we are matching a parameter
                request.setParam(token.substring(1), value)
            } else if (token != value) {
                return false
            }
        }
        return true
    }

    /**
     * matchWithCatchall Matches for patterns where the last token is a
     * catch-all parameter and consumes the catchall tokens from the
     * request returning true if a catch-all was captured otherwise
     * attempts to match the entire url instead using matchRange
     *
     * @param start The index of the request tokens to match from
     * @param request The request to match
     * @return Whether the tokens were matches as a catch-all
     */
    fun matchWithCatchall(start: Int, request: HttpRequest): Boolean {
        val requestTokens = request.tokens
        val tokenCount = patternTokens.size
        if (tokenCount > 0 && patternTokens.last() == ":*") {
            // Check that everything until the catch-all is matching
            if (!matchRange(request, start, tokenCount - 1)) return false
            // The index from where to start the capture
            val catchIndex = start + tokenCount - 1
            val builder = StringBuilder()
            // Append all the tokens after the catch index
            for (i in catchIndex until requestTokens.size) {
                builder.append(requestTokens[i])
                if (i < requestTokens.size - 1) {
                    builder.append('/')
                }
            }
            // Set the catch-all parameter
            request.setParam("*", builder.toString())
            return true
        } else {
            // Try and match all the tokens
            return matchRange(request, start, tokenCount)
        }
    }
}