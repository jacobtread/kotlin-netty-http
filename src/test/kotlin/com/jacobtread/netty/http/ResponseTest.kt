package com.jacobtread.netty.http

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ResponseTest {

    @Test
    fun `test text response`() {
        val text = "Example Text"
        val testResponse = responseText(text)

        assertEquals(testResponse.status(), HttpResponseStatus.OK)

        val headers = testResponse.headers()
        assertEquals(headers[HttpHeaderNames.CONTENT_TYPE], PLAIN_TEXT_CONTENT_TYPE)

        val content = testResponse.content()
        assertTrue(content.isReadable)
        val textContent = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)

        assertEquals(text, textContent)
    }

    @Test
    fun `test response types`() {
        var testResponse =  httpInternalServerError()
        assertEquals(testResponse.status(), HttpResponseStatus.INTERNAL_SERVER_ERROR)

        testResponse = httpBadRequest()
        assertEquals(testResponse.status(), HttpResponseStatus.BAD_REQUEST)

        testResponse = httpNotFound()
        assertEquals(testResponse.status(), HttpResponseStatus.NOT_FOUND)
    }

    @Test
    fun `test html response`() {
        val html = "<html></html>"
        val testResponse = responseHtml(html)

        assertEquals(testResponse.status(), HttpResponseStatus.OK)

        val headers = testResponse.headers()
        assertEquals(headers[HttpHeaderNames.CONTENT_TYPE], HTML_CONTENT_TYPE)

        val content = testResponse.content()
        assertTrue(content.isReadable)
        val textContent = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)

        assertEquals(html, textContent)
    }

    @Test
    fun `test set header`() {
        val html = "<html></html>"
        val testResponse = responseHtml(html)

        testResponse.setHeader("test", "a")

        assertEquals(testResponse.status(), HttpResponseStatus.OK)

        val headers = testResponse.headers()
        assertEquals(headers["test"], "a")

        val content = testResponse.content()
        assertTrue(content.isReadable)
        val textContent = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)

        assertEquals(html, textContent)
    }

    @Test
    fun `test set headers bulk`() {
        val headersToAdd = mapOf(
            "a" to "b",
            "b" to "c",
            "c" to "d"
        )

        val html = "<html></html>"
        val testResponse = responseHtml(html)

        testResponse.setHeaders(headersToAdd)

        assertEquals(testResponse.status(), HttpResponseStatus.OK)

        val headers = testResponse.headers()

        headersToAdd.forEach { (key, value) ->
            assertEquals(headers[key], value)
        }

        val content = testResponse.content()
        assertTrue(content.isReadable)
        val textContent = content.readCharSequence(content.readableBytes(), Charsets.UTF_8)

        assertEquals(html, textContent)
    }

    @Test
    fun `test bytes response`() {
        val bytes = byteArrayOf(0, 5, 120, 5, 3, 123, 3, 13, 13, 9)
        val testResponse = responseBytes(bytes)

        assertEquals(testResponse.status(), HttpResponseStatus.OK)

        val content = testResponse.content()
        assertTrue(content.isReadable)

        val byteContent = ByteArray(content.readableBytes())
        content.readBytes(byteContent)

        assertTrue(bytes.contentEquals(byteContent))
    }

}