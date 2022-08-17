package com.jacobtread.netty.http

import com.jacobtread.netty.http.router.createRouter
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RoutingTest {
    /**
     * Creates simple routes for each method and then
     * checks to ensure it correctly routes to the
     * route with that method
     */
    @Test
    fun `create simple routing methods test`() {
        val route = "/api/simple-route"
        val text = "Simple Route Text"

        val methods = arrayOf(
            HttpMethod.OPTIONS,
            HttpMethod.GET,
            HttpMethod.HEAD,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.PATCH,
            HttpMethod.DELETE,
            HttpMethod.TRACE,
            HttpMethod.CONNECT,
        )
        methods.forEach { method ->

            val router = createRouter {
                route(route, method) {
                    responseText(text)
                }
            }

            // Create a fake request to the created route
            val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, method, route)
            val fakeInternalRequest = HttpRequest(fakeRequest)

            // Get the response from the router
            val response = router.handleHttpRequest(fakeInternalRequest)
            assertEquals(response.status(), HttpResponseStatus.OK)

            val content = response.content()
            assert(content.isReadable)

            val contentText = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)

            assertEquals(text, contentText)
        }
    }

    /**
     * Tests the right response is gained for the right input
     * provided when many routes are defined.
     */
    @Test
    fun `test builder multiple route creations`() {
        val routes = arrayOf("a", "b", "c", "d", "e")

        val router = createRouter {
            routes.forEach { value ->
                get(value) {
                    responseText(value)
                }
            }
        }

        routes.forEach { value ->
            // Create a fake request to the created route
            val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/$value")
            val fakeInternalRequest = HttpRequest(fakeRequest)

            // Get the response from the router
            val response = router.handleHttpRequest(fakeInternalRequest)
            assertEquals(response.status(), HttpResponseStatus.OK)

            val content = response.content()
            assert(content.isReadable)

            val contentText = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)

            assertEquals(value, contentText)
        }
    }

    @Test
    fun `test like routing`() {
        val router = createRouter {
            get("list") {
                responseText("list")
            }
            get("list/:id") {
                responseText("list id")
            }
        }

        run {
            // Create a fake request to the created route
            val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/list")
            val fakeInternalRequest = HttpRequest(fakeRequest)
            // Get the response from the router
            val response = router.handleHttpRequest(fakeInternalRequest)
            assertEquals(response.status(), HttpResponseStatus.OK)
            val content = response.content()
            assert(content.isReadable)
            val contentText = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)
            assertEquals("list", contentText)
        }

        run {
            // Create a fake request to the created route
            val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/list/1")
            val fakeInternalRequest = HttpRequest(fakeRequest)
            // Get the response from the router
            val response = router.handleHttpRequest(fakeInternalRequest)
            assertEquals(response.status(), HttpResponseStatus.OK)
            val content = response.content()
            assert(content.isReadable)
            val contentText = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)
            assertEquals("list id", contentText)
        }
    }

    /**
     * Tests the router handling against the same route with
     * different methods uses and ensures the right response
     * is gained for the right route
     */
    @Test
    fun `test builder multiple route creations multiple methods`() {
        val routes = arrayOf("a", "b", "c", "d", "e")

        val router = createRouter {
            routes.forEach { value ->
                get(value) {
                    responseText(value)
                }

                post(value) {
                    responseText("post-$value")
                }
            }
        }

        routes.forEach { value ->
            // Create a fake request to the created route
            val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/$value")
            val fakeInternalRequest = HttpRequest(fakeRequest)

            // Get the response from the router
            val response = router.handleHttpRequest(fakeInternalRequest)
            assertEquals(response.status(), HttpResponseStatus.OK)

            val content = response.content()
            assert(content.isReadable)

            val contentText = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)

            assertEquals(value, contentText)
        }

        routes.forEach { value ->
            // Create a fake request to the created route
            val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.POST, "/$value")
            val fakeInternalRequest = HttpRequest(fakeRequest)

            // Get the response from the router
            val response = router.handleHttpRequest(fakeInternalRequest)
            assertEquals(response.status(), HttpResponseStatus.OK)

            val content = response.content()
            assert(content.isReadable)

            val contentText = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)

            assertEquals("post-$value", contentText)
        }
    }

}