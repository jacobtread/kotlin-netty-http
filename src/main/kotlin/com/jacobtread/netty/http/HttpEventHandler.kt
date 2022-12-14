package com.jacobtread.netty.http

/**
 * Interface for implementing listeners to events for
 * incoming http traffic [onRequestReceived] out going
 * http responses [onResponseSent] and exception handling
 * with [onExceptionHandled]
 */
interface HttpEventHandler {
    /**
     * Called when any http requests are received.
     *
     * @param request The received http request
     */
    fun onRequestReceived(request: HttpRequest)

    /**
     * Called when any http responses are about to
     * be sent giving the server the opportunity to
     * modify them
     *
     * @param response The response being sent
     */
    fun onResponsePreSend(response: HttpResponse)

    /**
     * Called when responses are sent to http requests.
     *
     * @param response The http response sent
     */
    fun onResponseSent(response: HttpResponse)

    /**
     * Called when an exception is handled.
     *
     * @param cause The exception that was handled
     */
    fun onExceptionHandled(cause: Exception)
}