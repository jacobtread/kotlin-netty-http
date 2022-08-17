package com.jacobtread.netty.http.router

import com.jacobtread.netty.http.RequestHandler
import com.jacobtread.netty.http.middleware.Middleware
import io.netty.handler.codec.http.HttpMethod

/**
 * RoutingGroup Represents a group of routes. Has helper functions
 * for adding different kinds of routes for different methods easily
 *
 * Methods:
 *
 * - POST: [post]
 * - GET: [get]
 * - PUT: [put]
 * - DELETE: [delete]
 * - PATCH: [patch]
 *
 * Alternatively to individual methods if you need to target a different
 * method that isn't listed already you can use the [route] function and
 * provide the method as the second parameter
 *
 * There is also the [everything] function which is for creating routes
 * that catch requests from every method and url past the current match
 * 
 * The [fallback] function is similar to [everything] except it doesn't
 * set the * parameter
 *
 * New routing groups can be created with the [group] builder function
 * which creates a new group within this group
 */
interface RoutingGroup {

    /**
     * The route storage implementation.
     * Implemented on the underlying class
     */
    val routes: ArrayList<RouteHandler>

    /**
     * Adds a new Path Route to the routes list that uses
     * the provided pattern, method and handler
     *
     * @param pattern The pattern to use on the route
     * @param method The method to match for the route
     * @param handler The handler for handling route requests
     * @return The created path route
     */
    fun route(pattern: String, method: HttpMethod?, handler: RequestHandler): PathRoute {
        val route = PathRoute(pattern, method, handler)
        routes.add(route)
        return route
    }

    /**
     * Shortcut function for adding a route
     * that accepts any method
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     * @return The created path route
     */
    fun route(pattern: String, handler: RequestHandler): PathRoute = route(pattern, null, handler)

    /**
     * function for adding a route which accepts GET requests
     * that match the provided pattern relative to the previous
     * routing groups
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     * @return The created path route
     */
    fun get(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.GET, handler)

    /**
     * function for adding a route which accepts POST requests
     * that match the provided pattern relative to the previous
     * routing groups
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     * @return The created path route
     */
    fun post(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.POST, handler)

    /**
     * function for adding a route which accepts PUT requests
     * that match the provided pattern relative to the previous
     * routing groups
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     * @return The created path route
     */
    fun put(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.PUT, handler)

    /**
     * function for adding a route which accepts PATCH requests
     * that match the provided pattern relative to the previous
     * routing groups
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     * @return The created path route
     */
    fun patch(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.PATCH, handler)

    /**
     * function for adding a route which accepts DELETE requests
     * that match the provided pattern relative to the previous
     * routing groups
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     * @return The created path route
     */
    fun delete(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.DELETE, handler)

    /**
     * function for adding a route which accepts requests from any
     * method with any path. Used as a sort of catch-all route which
     * will accept anything that reaches it
     *
     * Contents matched will be provided to the request as the "*"
     * parameter
     *
     * @param handler The handler for the route
     * @return The created path route
     */
    fun everything(handler: RequestHandler): PathRoute = route(":*", null, handler)

    /**
     * function for adding a route which accepts requests from any
     * method with any path. Used as a sort of catch-all route which
     * will accept anything that reaches it
     *
     * This is similar to [everything] except the * parameter is not
     * set this is a simpler version which doesn't attempt to match
     * the pattern
     *
     * @param handler The handler for the route
     * @return The created path route
     */
    fun fallback(handler: RequestHandler): FallbackRoute {
        val route = FallbackRoute(handler)
        routes.add(route)
        return route
    }
}

/**
 * Inline function for initializing routing groups
 * initializes the routes for the group using the provided
 * initialization function and adds the group to the routes
 *
 * @param pattern The pattern for this routing group
 * @param init The initialization function
 * @receiver The group this group will belong to
 * @return The created group
 */
inline fun RoutingGroup.group(pattern: String = "", init: GroupRoute.() -> Unit): GroupRoute {
    val group = GroupRoute(pattern)
    group.init()
    routes.add(group)
    return group
}

/**
 * Similar to [group] except rather than matching a specific pattern for the
 * group this is used to create a separate group under the current group
 * with a list of middleware to check first
 *
 * Can be used to guard a group of routes with middleware
 *
 * @param middleware The middleware to use for this group
 * @param init The initialization function
 * @receiver The group this group will belong to
 * @return The created group
 */
inline fun RoutingGroup.middlewareGroup(vararg middleware: Middleware, init: GroupRoute.() -> Unit): GroupRoute {
    val group = GroupRoute("")
    group.middleware(*middleware)
    group.init()
    routes.add(group)
    return group
}