package com.jacobtread.netty.http.router

import com.jacobtread.netty.http.RequestHandler
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
 * New routing groups can be created with the [group] builder function
 * which creates a new group within this group
 */
interface RoutingGroup {

    /**
     * routes The route storage implementation.
     * Implemented on the underlying class
     */
    val routes: ArrayList<RouteHandler>

    /**
     * route Adds a new Path Route to the routes list that uses
     * the provided pattern, method and handler
     *
     * @param pattern The pattern to use on the route
     * @param method The method to match for the route
     * @param handler The handler for handling route requests
     */
    fun route(pattern: String, method: HttpMethod?, handler: RequestHandler): PathRoute {
        val route = PathRoute(pattern, method, handler)
        routes.add(route)
        return route
    }

    /**
     * route Shortcut function for adding a route
     * that accepts any method
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     */
    fun route(pattern: String, handler: RequestHandler): PathRoute = route(pattern, null, handler)

    /**
     * get Shortcut function for adding a route
     * which accepts GET requests from the provided
     * pattern
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     */
    fun get(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.GET, handler)

    /**
     * post Shortcut function for adding a route
     * which accepts POST requests from the provided
     * pattern
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     */
    fun post(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.POST, handler)

    /**
     * put Shortcut function for adding a route
     * which accepts PUT requests from the provided
     * pattern
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     */
    fun put(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.PUT, handler)

    /**
     * patch Shortcut function for adding a route
     * which accepts PATCH requests from the provided
     * pattern
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     */
    fun patch(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.PATCH, handler)

    /**
     * delete Shortcut function for adding a route
     * which accepts DELETE requests from the provided
     * pattern
     *
     * @param pattern The pattern for the route
     * @param handler The handler for the route
     */
    fun delete(pattern: String, handler: RequestHandler): PathRoute = route(pattern, HttpMethod.DELETE, handler)

    /**
     * everything Shortcut function for handling every request that
     * hits this route. This should be added after all other routes
     * because it will match any content its given
     *
     * Contents matched will be provided to the request as the "*"
     * parameter
     *
     * @param handler The handler for the route
     */
    fun everything(handler: RequestHandler): PathRoute = route(":*", null, handler)
}

/**
 * group Inline function for initializing routing groups
 * initializes the routes for the group using the provided
 * initialization function and adds the group to the routes
 *
 * @param pattern The pattern for this routing group
 * @param init The initialization function
 * @receiver The created group to initialize
 */
inline fun RoutingGroup.group(pattern: String = "", init: GroupRoute.() -> Unit): GroupRoute {
    val group = GroupRoute(pattern)
    group.init()
    routes.add(group)
    return group
}