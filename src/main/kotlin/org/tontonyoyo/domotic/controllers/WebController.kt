package org.tontonyoyo.domotic.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@Controller
class WebController {

    @GetMapping("/")
    fun rootPath(request: HttpServletRequest): String {
        UtilController.checkAuth(request)
        return "list.html"
    }

    @GetMapping("/edit")
    fun rootPath(@RequestParam("deviceId") deviceId: String, request: HttpServletRequest): String {
        UtilController.checkAuth(request)
        return "edit.html"
    }

}