package com.jacobtread.netty.http

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpMethod
import java.net.URLDecoder
import java.net.URLEncoder
import io.netty.handler.codec.http.HttpRequest as NettyHttpRequest

/**
 * A wrapper around the netty HttpRequest implementation
 * which contains the url tokens as well as a parsed query and
 * functions for accessing the request body
 *
 * @property http The netty http request
 *
 * @constructor Create HttpRequest and parses the tokens and query string
 */
class HttpRequest internal constructor(val http: NettyHttpRequest) {

    private var attributes: HashMap<String, Any>? = null

    /**
     * Underlying params map stores the parameters that were
     * parsed when the url for the request was matched. This will be
     * null if there were no parameters matched on this route
     */
    private var params: HashMap<String, String>? = null

    /**
     * Wrapper field for accessing the method of the underlying
     * http request. Used by the Path Route to match the request method
     */
    val method: HttpMethod
        get() = http.method()

    /**
     * Wrapper field for accessing the headers of the underlying
     * request
     */
    val headers: HttpHeaders get() = http.headers()

    /**
     * The tokens aka all the values between each slash in the
     * url excluding those after the query question mark
     */
    val tokens: List<String>

    /**
     * A mapping of the query parameters to their values for the underlying
     * request. This is parsed from the url using [createQueryMap]
     */
    val query: Map<String, String>

    init {
        // Split the url into the path and query
        val parts = http
            .uri()
            .split('?', limit = 2)
        // Removing leading and trailing slashes for parsing
        val url = parts[0]
            .removePrefix("/")
            .removeSuffix("/")
        this.tokens = url.split('/')
        query = createQueryMap(parts.getOrNull(1))
    }

    /**
     * Reconstructs the url that this object was created with
     * by joining the tokens and query parameters together.
     *
     * This is intended for debug use in order to display the
     * url that was queried
     *
     * @return The re-constructed url
     */
    fun reconstructUrl(): String {
        val urlBuilder = StringBuilder("/")
        tokens.joinTo(urlBuilder, "/") { it }
        val query = query
        if (query.isNotEmpty()) {
            urlBuilder.append("?")
            query.entries.joinTo(urlBuilder, "&") { (key, value) ->
                val keyEncoded = URLEncoder.encode(key, "UTF-8")
                val valueEncoded = URLEncoder.encode(value, "UTF-8")
                "$keyEncoded=$valueEncoded"
            }
        }
        return urlBuilder.toString()
    }

    /**
     * Parses the provided query string splitting the values
     * into pairs and storing them in a HashMap as key values. If the provided
     * query string is empty or null an empty map is returned instead
     *
     * Note: Query parameters without values are just given a blank string
     * as their value so that it can still be checked for using hasQuery
     *
     * @param queryString The url query string or null if there is none
     * @return The map of key values
     */
    private fun createQueryMap(queryString: String?): Map<String, String> {
        if (queryString.isNullOrEmpty()) return emptyMap()
        val query = HashMap<String, String>()
        queryString.split('&').forEach { keyValue ->
            val parts = keyValue.split('=', limit = 2)
                .map { URLDecoder.decode(it, "UTF-8") }
            if (parts.size == 2) {
                query[parts[0]] = parts[1]
            } else if (parts.size == 1) {
                query[parts[0]] = ""
            }
        }
        return query
    }

    /**
     * Sets a parameter on the request. This will initialize
     * the underlying parameters map if it hasn't already been initialized
     * this should only be used by the route matcher when matching the route
     *
     * @param key The key of the parameter
     * @param value The value of the parameter
     */
    internal fun setParam(key: String, value: String) {
        if (params == null) params = HashMap()
        params!![key] = value
    }

    /**
     * Retrieves a route matched parameter will throw an illegal state exception
     * if the parameter was not defined on the route or if no parameters were defined
     * at all
     *
     * @see paramInt For retrieving parameters as an integer value
     *
     * @throws IllegalStateException If the provided key was not a parameter of the request
     * @param key The key of the parameter to retrieve
     * @return The value of the parameter
     */
    fun param(key: String): String {
        val param = params?.get(key)
        check(param != null) { "Request handler attempted to use param $key when it was not defined in the route" }
        return param
    }

    /**
     * Retrieves the route parameter and parses it as an integer will
     * throw bad request exception if the parameter was not an integer
     *
     * @param key The key of the route parameter
     * @param radix The radix to parse the integer using
     * @throws BadRequestException If the client provided a non integer value for the parameter
     * @throws IllegalStateException If the provided key was not a parameter of the request
     * @return The parsed parameter
     */
    fun paramInt(key: String, radix: Int = 10): Int = param(key).toIntOrNull(radix) ?: throw BadRequestException()

    /**
     * Retrieves the query value with the provided key.
     * Will throw a BadRequestException if the query key was
     * not provided
     *
     * @see queryOrNull Alternative to this which returns null when not found instead of [BadRequestException]
     * @see hasQuery Used to check if the query value exists
     * @see queryInt For retrieving query values as integers
     *
     * @throws BadRequestException Thrown if the query key was not provided
     * @param key The key to search for
     * @return The value of the key
     */
    fun query(key: String): String = query[key] ?: throw BadRequestException()

    /**
     * Retrieves the query value of the provided key.
     * Returning null if the key was not found
     *
     * @see query Alternative to this which throws [BadRequestException] when not found
     *
     * @param key The key to search for
     * @return The value of the key or null if it was not provided
     */
    fun queryOrNull(key: String): String? = query[key]

    /**
     * Returns whether the request has
     * the provided query key
     *
     * @param key The key to search for
     * @return Whether the key exists or not
     */
    fun hasQuery(key: String): Boolean = query.containsKey(key)

    /**
     * Retrieves the query value of the provided key as
     * an integer. Will throw BadRequestException if the key was
     * not provided or the provided value was not an integer
     *
     * @param key The key to search for
     * @param radix The radix to parse the integer using
     * @throws BadRequestException Thrown if the query key was not provided
     * or if the value was not an integer
     * @return The integer query value
     */
    fun queryInt(key: String, radix: Int = 10): Int = query[key]?.toIntOrNull(radix) ?: throw BadRequestException()

    /**
     * Retrieves the query value of the provided key as
     * an integer. Will return the provided default value if the
     * key wasn't provided or wasn't an integer
     *
     * @param key The key to search for
     * @param default The default value to use if the value was missing or
     * couldn't be parsed
     * @param radix The radix to parse the integer using
     * @return THe integer query value or the default value
     */
    fun queryInt(key: String, default: Int, radix: Int = 10): Int = query[key]?.toIntOrNull(radix) ?: default

    /**
     * Reads the body of the request as a ByteArray
     * and returns the result
     *
     * @see contentString For retrieving the content as a string instead
     *
     * @throws BadRequestException Thrown if the request doesn't have a body
     * @return The contents as a byte array
     */
    fun contentBytes(): ByteArray {
        if (http !is FullHttpRequest) throw BadRequestException()
        val contentBuffer = http.content()
        val bytes = ByteArray(contentBuffer.readableBytes())
        contentBuffer.readBytes(bytes)
        return bytes
    }

    /**
     * Reads the body of the request as a ByteArray
     * and decodes it as a UTF-8 string and returns it
     *
     * @throws BadRequestException Thrown if the request doesn't have a body
     * @return The contents as a UTF-8 String
     */
    fun contentString(): String = contentBytes().decodeToString()

    /**
     * Sets an attribute on this request
     *
     * This can be used to set state across request handlers for
     * example setting an authentication state through a middleware
     * handler (e.g. [com.jacobtread.netty.http.middleware.GuardMiddleware])
     *
     * @see getAttribute for retrieving set attributes
     *
     * @param T The type of value stored for this attribute key
     * @param key The key used to identify this attribute
     * @param value The value to store for the attribute
     */
    fun <T> setAttribute(key: String, value: T) {
        val attributes: HashMap<String, Any> = this.attributes ?: HashMap()
        attributes[key] = value as Any /* Erasing the type of the value */
        if (this.attributes == null) {
            this.attributes = attributes
        }
    }

    /**
     * Retrieves an attribute from this request
     *
     * @see setAttribute for setting attributes
     *
     * @param T The type of value stored for this attribute key
     * @param key The key used to identify this attribute
     * @return The stored attribute value or null if none are present
     */
    fun <T> getAttribute(key: String): T? {
        val attributes = this.attributes
        val value = attributes?.get(key) ?: return null
        @Suppress("UNCHECKED_CAST")
        return value as T /* Casting value unsafely back to type */
    }
}