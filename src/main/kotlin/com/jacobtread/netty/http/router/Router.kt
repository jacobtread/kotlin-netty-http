package com.jacobtread.netty.http.router

import com.jacobtread.netty.http.*
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.netty.handler.codec.http.HttpRequest as NettyHttpRequest

/**
 * Router A netty channel inbound handler for handling the routing
 * of HTTP requests to different specified routes
 *
 * Note: Marked as sharable so that the same router can be added to multiple
 * channels to prevent the unnecessarily large amount of allocations that would
 * occur from recreating the router for every single channel
 *
 * New routers can be created by making instances of this object or using
 * the provided builder [createRouter].
 *
 * @constructor Creates a new empty router handler
 */
class Router : SimpleChannelInboundHandler<NettyHttpRequest>(), RoutingGroup {
    /**
     * Whether to display a 404 page when no routes could
     * be matched.
     */
    var enable404Page: Boolean = true
    var eventHandler: HttpEventHandler? = null

    /**
     * routes The list of routes for this router used for
     * determining how to handle incoming http requests
     */
    override val routes = ArrayList<RouteHandler>()

    /**
     * handlerAdded When the router handler is added it also
     * needs to add the HttpRequest decoder and HttpResponse
     * encoder along with the HttpObjectAggregator to
     * aggregate the decoded body contents
     *
     * @param ctx The channel handler context
     */
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        // Pipeline order = Decode -> Aggregator -> Encode
        val channel = ctx.channel()
        channel.pipeline()
            .addFirst(HttpResponseEncoder())
            .addFirst(HttpObjectAggregator(1024 * 8))
            .addFirst(HttpRequestDecoder())
    }

    /**
     * This handler is sharable because it doesn't store
     * any state for a particular channel so it can be used
     * by any connecting channels
     *
     * @return Always true
     */
    override fun isSharable(): Boolean = true

    /**
     * channelRead0 Handles reading the raw netty http requests
     * and wrapping them with the HttpRequest before attempting
     * to handle them with the handlers finishes up by writing
     * the response back to the client
     *
     * @param ctx The channel handler context for the channel
     * @param msg The http request message
     */
    override fun channelRead0(ctx: ChannelHandlerContext, msg: NettyHttpRequest) {
        val request = HttpRequest(msg) // Create a request wrapper

        // Call the event handler with the request object
        eventHandler?.onRequestReceived(request)

        val response = handleHttpRequest(request) // Handle the response
        // Write the response and flush
        ctx.writeAndFlush(response)

        // Call the event handler with the response object
        eventHandler?.onResponseSent(response)
    }

    /**
     * handleHttpRequest Handles the response to a request. Goes through all
     * the routes attempting to get a response falling back on 404.html if
     * the response wasn't handled or BAD_REQUEST / INTERNAL_SERVER_ERROR
     * if an exception was thrown while trying to handle the request
     *
     * @param request The request to handle
     * @return The response to the request
     */
    internal fun handleHttpRequest(request: HttpRequest): HttpResponse {
        try {
            for (route in routes) {
                return route.handle(0, request) ?: continue
            }
            return if (enable404Page) {
                responseResource("404.html", "public", status = NOT_FOUND)
            } else {
                response(NOT_FOUND)
            }
        } catch (e: HttpException) {
            return responseText(e.response, e.contentType, e.status)
        } catch (e: Exception) {
            eventHandler?.onExceptionHandled(e)
            return response(INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * Starts a new http server using this router as its http
     * handler. Returning the channel future after binding.
     *
     * @param port The port to bind the server to
     * @param bossGroup The boss netty event loop group
     * @param workerGroup The worker netty event loop group
     * @return The channel future for the server
     */
    fun startHttpServer(
        port: Int,
        bossGroup: EventLoopGroup,
        workerGroup: EventLoopGroup,
    ): ChannelFuture {
        return ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(this@Router)
            .bind(port)
    }
}

/**
 * Helper function for creating and
 * initializing a router with its routes
 *
 * @param init The initializer function
 * @return The initialized routing
 */
inline fun createRouter(init: Router.() -> Unit): Router {
    val router = Router()
    router.init()
    return router
}

/**
 * Attempts to retrieve the http router from the pipeline
 * of the channel from the receiver
 *
 * @return The router or null if none
 */
fun Channel.getHttpRouter(): Router? = this.pipeline().get(Router::class.java)

/**
 * Helper function for creating a router and starting
 * a http server from it
 *
 * @param port The port to bind the server to
 * @param bossGroup The boss netty event loop group
 * @param workerGroup The worker netty event loop group
 * @param init The initializer function
 * @return The channel future create when starting the server
 */
inline fun createHttpServer(
    port: Int,
    bossGroup: EventLoopGroup,
    workerGroup: EventLoopGroup,
    init: Router.() -> Unit,
): ChannelFuture {
    val router = Router()
    router.init()
    return router.startHttpServer(port, bossGroup, workerGroup)
}
