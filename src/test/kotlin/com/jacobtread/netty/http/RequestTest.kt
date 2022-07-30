package com.jacobtread.netty.http

import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpVersion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class RequestTest {

    @Test
    fun `test string query exists check`() {
        val keyA = "query1"
        val valueA = "abc"

        val keyB = "query2"
        val valueB = "xyz"

        val url = "/api?$keyA=$valueA&$keyB=$valueB"

        // Create a fake request to the created route
        val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, url)
        val fakeInternalRequest = HttpRequest(fakeRequest)

        assertTrue(fakeInternalRequest.hasQuery(keyA))
        assertTrue(fakeInternalRequest.hasQuery(keyB))
    }

    @Test
    fun `test string or null query parsing`() {
        val keyA = "query1"
        val valueA = "abc"

        val keyB = "query2"
        val valueB = "xyz"

        val url = "/api?$keyA=$valueA&$keyB=$valueB"

        // Create a fake request to the created route
        val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, url)
        val fakeInternalRequest = HttpRequest(fakeRequest)

        val query1 = fakeInternalRequest.queryOrNull(keyA)
        assertNotNull(query1)
        assertEquals(valueA, query1)

        val query2 = fakeInternalRequest.queryOrNull(keyB)
        assertNotNull(query2)
        assertEquals(valueB, query2)
    }

    @Test
    fun `test string or exception query parsing`() {
        val keyA = "query1"
        val valueA = "abc"

        val keyB = "query2"
        val valueB = "xyz"

        val url = "/api?$keyA=$valueA&$keyB=$valueB"

        // Create a fake request to the created route
        val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, url)
        val fakeInternalRequest = HttpRequest(fakeRequest)

        try {
            val query1 = fakeInternalRequest.query(keyA)
            assertEquals(valueA, query1)
        } catch (e: BadRequestException) {
            throw AssertionError("Failed to parse request missing $keyA")
        }
        try {
            val query2 = fakeInternalRequest.query(keyB)
            assertEquals(valueB, query2)
        } catch (e: BadRequestException) {
            throw AssertionError("Failed to parse request missing $keyB")
        }
    }

    @Test
    fun `test integer query parsing`() {
        val keyA = "query1"
        val valueA = 123

        val keyB = "query2"
        val valueB = 5

        val url = "/api?$keyA=$valueA&$keyB=$valueB"

        // Create a fake request to the created route
        val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, url)
        val fakeInternalRequest = HttpRequest(fakeRequest)
        try {
            val query1 = fakeInternalRequest.queryInt(keyA)
            assertEquals(valueA, query1)
        } catch (e: BadRequestException) {
            throw AssertionError("Failed to parse request missing or invalid $keyA")
        }

        try {
            val query2 = fakeInternalRequest.queryInt(keyB)
            assertEquals(valueB, query2)
        } catch (e: BadRequestException) {
            throw AssertionError("Failed to parse request missing or invalid $keyB")
        }
    }

    @Test
    fun `test integer query with default parsing`() {
        val keyA = "query1"
        val valueA = "abc"

        val keyB = "query2"
        val valueB = 5

        val url = "/api?$keyA=$valueA&$keyB=$valueB"

        // Create a fake request to the created route
        val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, url)
        val fakeInternalRequest = HttpRequest(fakeRequest)

        val query1 = fakeInternalRequest.queryInt(keyA, default = 5)
        assertEquals(5, query1)

        try {
            val query2 = fakeInternalRequest.queryInt(keyB)
            assertEquals(valueB, query2)
        } catch (e: BadRequestException) {
            throw AssertionError("Failed to parse request missing or invalid $keyB")
        }
    }

    @Test
    fun `test param string`() {
        val keyA = "query1"
        val valueA = "abc"

        // Create a fake request to the created route
        val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/api")
        val fakeInternalRequest = HttpRequest(fakeRequest)

        fakeInternalRequest.setParam(keyA, valueA)

        val param = fakeInternalRequest.param(keyA)
        assertEquals(valueA, param)
    }

    @Test
    fun `test param int`() {
        val keyA = "query1"
        val valueA = 5

        // Create a fake request to the created route
        val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/api")
        val fakeInternalRequest = HttpRequest(fakeRequest)

        fakeInternalRequest.setParam(keyA, valueA.toString())

        val param = fakeInternalRequest.paramInt(keyA)
        assertEquals(valueA, param)
    }

    @Test
    fun `test content bytes`() {
        val body = byteArrayOf(0, 5, 10, 22, 10, 0, 5, 12, 1, 3)

        // Create a fake request to the created route
        val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.POST, "/api", Unpooled.wrappedBuffer(body))
        val fakeInternalRequest = HttpRequest(fakeRequest)

        val contentBytes = fakeInternalRequest.contentBytes()

        assertTrue(contentBytes.contentEquals(body))
    }

    @Test
    fun `test content string`() {
        val body = "Example String Body"

        // Create a fake request to the created route
        val fakeRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.POST, "/api", Unpooled.copiedBuffer(body, Charsets.UTF_8))
        val fakeInternalRequest = HttpRequest(fakeRequest)

        val contentString = fakeInternalRequest.contentString()

        assertEquals(body, contentString)
    }
}