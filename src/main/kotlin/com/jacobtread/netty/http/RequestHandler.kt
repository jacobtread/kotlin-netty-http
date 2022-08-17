package com.jacobtread.netty.http

import io.netty.handler.codec.http.HttpResponseStatus

/**
 * This is exception can be thrown during a request to halt
 * routing and is caught in the router. The router will respond
 * with the provided response and status when this is thrown
 *
 * @property response The response body
 * @property status The http status code
 */
class HttpException(
    val status: HttpResponseStatus,
    val response: String = status.reasonPhrase(),
    val contentType: String = PLAIN_TEXT_CONTENT_TYPE
) : RuntimeException(response)

/**
 * Type alias for a function which has a HttpRequest receiver
 * and responds with a http response
 */
typealias RequestHandler = HttpRequest.() -> HttpResponse

fun throwStatus(status: HttpResponseStatus, message: String = status.reasonPhrase()): Nothing {
    throw HttpException(status, message)
}

fun throwBadRequest(): Nothing = throwStatus(HttpResponseStatus.BAD_REQUEST)
fun throwForbidden(): Nothing = throwStatus(HttpResponseStatus.FORBIDDEN)
fun throwServerError(): Nothing = throwStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)