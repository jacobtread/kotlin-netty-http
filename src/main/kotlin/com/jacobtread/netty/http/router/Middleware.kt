package com.jacobtread.netty.http.router

import com.jacobtread.netty.http.HttpRequest
import com.jacobtread.netty.http.HttpResponse

interface Middleware {

    fun handleRequest(request: HttpRequest): HttpResponse?

}