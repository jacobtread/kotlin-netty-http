package com.jacobtread.netty.http

/**
 * BadRequestException Exception thrown when the request is malformed
 * or is missing a query parameter or other reasons such as invalid data
 * or the inability to be deserialized
 *
 * @constructor Create empty BadRequestException
 */
class BadRequestException : RuntimeException("Client request was invalid")

/**
 * Type alias for a function which has a HttpRequest receiver
 * and responds with a http response
 */
typealias RequestHandler = HttpRequest.() -> HttpResponse
