package com.jacobtread.netty.http.middleware

import com.jacobtread.netty.http.HttpRequest
import com.jacobtread.netty.http.HttpResponse
import java.util.function.Function
import java.util.function.Predicate

/**
 * Creates a basic guard middleware which uses the
 * provided [condition] will use the default error response
 *
 * @param condition The guard condition predicate
 * @return The guard middleware created from the condition
 */
fun createGuard(condition: Predicate<HttpRequest>): GuardMiddleware {
    return object : GuardMiddleware() {
        override fun isAllowed(request: HttpRequest): Boolean {
            return condition.test(request)
        }
    }
}

/**
 * Creates a basic guard middleware which uses the
 * provided [condition] will use the response created
 * by the [createError] function provided
 *
 * @param condition The guard condition predicate
 * @param createError function for creating the error response
 * @return The guard middleware created from the condition
 */
fun createGuard(condition: Predicate<HttpRequest>, createError: Function<HttpRequest, HttpResponse>): GuardMiddleware {
    return object : GuardMiddleware() {
        override fun isAllowed(request: HttpRequest): Boolean {
            return condition.test(request)
        }

        override fun createErrorResponse(request: HttpRequest): HttpResponse {
            return createError.apply(request)
        }
    }
}