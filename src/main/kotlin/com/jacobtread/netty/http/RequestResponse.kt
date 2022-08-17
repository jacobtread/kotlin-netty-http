package com.jacobtread.netty.http

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion

// Content-Type constants
const val PLAIN_TEXT_CONTENT_TYPE = "text/plain;charset=UTF-8"
const val HTML_CONTENT_TYPE = "text/html;charset=UTF-8"

/**
 * Type alias for a full http response this is type aliased because it
 * is the only type of http response used throughout the application, and
 * it's much shorter than writing DefaultFullHttpResponse everywhere
 */
typealias HttpResponse = DefaultFullHttpResponse

/**
 * Sets a map of key value pairs as headers on the request
 * that it was invoked upon
 *
 * @param headers The map of headers to set
 * @return The response that this was called on (So that it can be used like a builder)
 */
fun HttpResponse.setHeaders(headers: Map<String, String>): HttpResponse {
    val httpHeaders = headers()
    headers.forEach { (key, value) -> httpHeaders.add(key, value) }
    return this
}

/**
 * Sets a header on the headers list for
 * the request  that it was invoked upon
 *
 * @param key The key of the header to set
 * @param value The value of the header to set
 * @return The response that this was called on (So that it can be used like a builder)
 */
fun HttpResponse.setHeader(key: String, value: String): HttpResponse {
    val httpHeaders = headers()
    httpHeaders.add(key, value)
    return this
}

/**
 * Creates a raw bytes' response with an optional
 * content type parameter
 *
 * @param bytes The raw byte array contents of this response
 * @param contentType The optional content type of this response null for none
 * @param status The status of this response
 * @return The created HttpResponse from the bytes
 */
fun responseBytes(
    bytes: ByteArray,
    contentType: String? = null,
    status: HttpResponseStatus = HttpResponseStatus.OK
): HttpResponse = response(status, Unpooled.wrappedBuffer(bytes), contentType)

/**
 * Creates a text response with an optional
 * content type parameter by default this is text/plain
 *
 * @param content The string contents to use for the response
 * @param contentType The optional content type of the response
 * @param status The http status of this response
 * @return The created HttpResponse from the text
 */
fun responseText(
    content: String,
    contentType: String = PLAIN_TEXT_CONTENT_TYPE,
    status: HttpResponseStatus = HttpResponseStatus.OK,
): HttpResponse = response(status, Unpooled.copiedBuffer(content, Charsets.UTF_8), contentType)

/**
 * Creates a html response with the content
 * type of text/html from the provided content string
 *
 * @param content The html content
 * @param status The http status of this response
 * @return The created HttpResponse from the html
 */
fun responseHtml(
    content: String,
    status: HttpResponseStatus= HttpResponseStatus.OK
): HttpResponse = response(status, Unpooled.copiedBuffer(content, Charsets.UTF_8), HTML_CONTENT_TYPE)

/**
 * Function for easily creating a response with
 * a NOT_FOUND response
 *
 * @return The created response
 */
fun httpNotFound(): HttpResponse = response(HttpResponseStatus.NOT_FOUND)

/**
 * Function for easily creating a response with
 * a BAD_REQUEST response
 *
 * @return The created response
 */
fun httpBadRequest(): HttpResponse = response(HttpResponseStatus.BAD_REQUEST)

/**
 * Function for easily creating a response with
 * a INTERNAL_SERVER_ERROR response
 *
 * @return The created response
 */
fun httpInternalServerError(): HttpResponse = response(HttpResponseStatus.INTERNAL_SERVER_ERROR)

/**
 * Creates a response from a static file stored inside the
 * jar resources if the file doesn't exist or is not a valid file name then
 * the fallback file and path will be used instead. If the fallback is also
 * invalid an empty response with the NOT_FOUND status will be used instead
 *
 * @param fileName The name of the file to respond with
 * @param path The root path of the file in the resources directory
 * @param fallbackFileName The fallback file name to use if the file was not found
 * @param fallbackPath The fallback path to use if the fallback name is used
 * @return The created HttpResponse from the file
 */
fun responseResource(
    fileName: String,
    path: String,
    fallbackFileName: String = "404.html",
    fallbackPath: String = "public",
    status: HttpResponseStatus = HttpResponseStatus.OK,
): HttpResponse {
    if (fileName.isBlank()) return responseResource(fallbackFileName, fallbackPath)
    val resourceStream = HttpRequest::class.java.getResourceAsStream("/$path/$fileName")
    val resource = resourceStream?.readBytes()
    if (resource == null) {
        if (fileName != fallbackFileName) return responseResource(fallbackFileName, fallbackPath)
        return response(HttpResponseStatus.NOT_FOUND)
    }
    val contentType: String = when (fileName.substringAfterLast('.')) {
        "js" -> "text/javascript"
        "css" -> "text/css"
        "html" -> HTML_CONTENT_TYPE
        else -> PLAIN_TEXT_CONTENT_TYPE
    }
    val buffer = Unpooled.wrappedBuffer(resource)
    return response(status, buffer, contentType)
}

/**
 * Creates a http response from the provided status, content
 * and optional content type. Adds the appropriate headers for content
 * type and length as well as the cross-origin access control headers
 * so that browsers can make POST requests to the server
 *
 * @param status The status of the http response
 * @param content The content to use for the response empty buffer by default
 * @param contentType The type of the content stored in the buffer
 * @return The created HttpResponse
 */
fun response(
    status: HttpResponseStatus,
    content: ByteBuf = Unpooled.EMPTY_BUFFER,
    contentType: String? = null,
): HttpResponse {
    val out = HttpResponse(HttpVersion.HTTP_1_1, status, content)
    val contentLength = content.readableBytes()
    val httpHeaders = out.headers()
    // Content description headers
    if (contentType != null) httpHeaders.add(HttpHeaderNames.CONTENT_TYPE, contentType)
    httpHeaders.add(HttpHeaderNames.CONTENT_LENGTH, contentLength)
    // CORS so that requests can be accessed in the browser
    httpHeaders.add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
    httpHeaders.add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*")
    httpHeaders.add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "*")
    return out
}