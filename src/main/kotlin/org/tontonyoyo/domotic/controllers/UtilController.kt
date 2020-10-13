package org.tontonyoyo.domotic.controllers

import org.tontonyoyo.domotic.exceptions.ResourceNotFoundException
import javax.servlet.http.HttpServletRequest

object UtilController {

    fun checkAuth(request: HttpServletRequest) {
        if (!isLocalAddr(request) && !isLocalHost(request)) {
            throw ResourceNotFoundException()
        }
    }

    private fun isLocalHost(request: HttpServletRequest) = request.localName.contains("local")

    private fun isLocalAddr(request: HttpServletRequest) = request.remoteAddr.startsWith("192.168.1.")
}