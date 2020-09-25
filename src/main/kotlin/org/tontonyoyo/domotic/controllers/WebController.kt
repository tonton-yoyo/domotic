package org.tontonyoyo.domotic.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.tontonyoyo.domotic.models.DeviceType
import org.tontonyoyo.domotic.services.DeviceService
import org.tontonyoyo.domotic.services.DeviceAlreadyExistException
import org.tontonyoyo.domotic.services.DeviceNotFoundException

@Controller
class WebController @Autowired constructor(val deviceService: DeviceService) {

    @GetMapping("/devices")
    fun getDevices(model: Model): String {
        model.addAttribute("devices", deviceService.getDevices())
        return "devices"
    }

    @PostMapping("/devices")
    fun createDevice(@RequestParam name: String,
                     @RequestParam id: String,
                     @RequestParam type: DeviceType,
                     @RequestParam duration: Int,
                     @RequestParam brightness: Int,
                     @RequestParam(required = false) hue: Int?,
                     @RequestParam(required = false) saturation: Int?,
                     model: Model): String {
        try {
            deviceService.createDevice(name, id, type, duration, brightness, hue, saturation)
        } catch (ex: DeviceAlreadyExistException) {
            model.addAttribute("error", "A device already exist with the name ${ex.name}")
        }
        return getDevices(model)
    }

    @PostMapping("/devices/{name}/update")
    fun updateDevice(@PathVariable("name") name: String,
                     @RequestParam duration: Int,
                     @RequestParam brightness: Int,
                     @RequestParam(required = false) hue: Int?,
                     @RequestParam(required = false) saturation: Int?,
                     model: Model): String {
        try {
            deviceService.updateDevice(name, duration, brightness, hue, saturation)
        } catch (ex: DeviceNotFoundException) {
            model.addAttribute("error", "No device found with the name ${ex.name}")
        }
        return getDevices(model)
    }

    @PostMapping("/devices/{name}/delete")
    fun deleteDevice(@PathVariable("name") name: String,
                     model: Model): String {
        try {
            deviceService.removeDevice(name)
        } catch (ex: DeviceNotFoundException) {
            model.addAttribute("error", "No device found with the name ${ex.name}")
        }
        return getDevices(model)
    }
}