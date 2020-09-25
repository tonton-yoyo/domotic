package org.tontonyoyo.domotic.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.tontonyoyo.domotic.models.Device
import org.tontonyoyo.domotic.models.DeviceType
import org.tontonyoyo.domotic.services.DeviceService
import org.tontonyoyo.domotic.services.DeviceAlreadyExistException
import org.tontonyoyo.domotic.services.DeviceNotFoundException
import org.tontonyoyo.domotic.tplink.TPLinkRepository

@Controller
class WebController @Autowired constructor(val deviceService: DeviceService, val tpLinkRepository: TPLinkRepository) {

    @GetMapping("/list")
    fun getDevices(model: Model): String {
        model.addAttribute("devices", deviceService.getDevices())
        return "list"
    }

    @GetMapping("/devices/new")
    fun prepareNewDevice(model: Model): String {
        return "new"
    }

    @PostMapping("/devices/new")
    fun createDevice(@RequestParam id: String,
                     @RequestParam name: String,
                     @RequestParam type: DeviceType,
                     @RequestParam duration: Int,
                     @RequestParam brightness: Int,
                     @RequestParam(required = false) hue: Int?,
                     @RequestParam(required = false) saturation: Int?,
                     model: Model): String {
        try {
            deviceService.createDevice(name, id, type, duration, brightness, hue, saturation)
        } catch (ex: DeviceAlreadyExistException) {
            model.addAttribute("error", "A device already exist with the id or name ${ex.idOrName}")
            model.addAttribute("id", id)
            model.addAttribute("name", name)
            model.addAttribute("type", type.name)
            model.addAttribute("duration", duration)
            model.addAttribute("brightness", brightness)
            model.addAttribute("hue", hue)
            model.addAttribute("saturation", saturation)
            return prepareNewDevice(model)
        }
        return getDevices(model)
    }

    @GetMapping("/devices/{id}")
    fun prepareEditDevice(@PathVariable("id") id: String, model: Model): String {
        try {
            val device = deviceService.getDeviceById(id) ?: throw DeviceNotFoundException(id)
            return prepareEditDeviceWithValue(model,
                    device,
                    device.brightness,
                    device.hue,
                    device.saturation
            )
        } catch (ex: DeviceNotFoundException) {
            model.addAttribute("error", "No device found with the id ${ex.id}")
        }
        return getDevices(model)
    }

    fun prepareEditDeviceWithValue(model: Model,
                                   device: Device,
                                   brightness: Int,
                                   hue: Int?,
                                   saturation: Int?): String {
        try {
            model.addAttribute("id", device.id)
            model.addAttribute("name", device.name)
            model.addAttribute("type", device.type.name)
            model.addAttribute("duration", device.duration)
            model.addAttribute("brightness", brightness)
            model.addAttribute("hue", hue)
            model.addAttribute("saturation", saturation)
            return "edit"
        } catch (ex: DeviceNotFoundException) {
            model.addAttribute("error", "No device found with the id ${ex.id}")
        }
        return getDevices(model)
    }

    @PostMapping("/devices/{id}")
    fun editDevice(@PathVariable("id") id: String,
                     @RequestParam name: String,
                     @RequestParam duration: Int,
                     @RequestParam brightness: Int,
                     @RequestParam(required = false) hue: Int?,
                     @RequestParam(required = false) saturation: Int?,
                     @RequestParam(required = false) test: String?,
                     model: Model): String {
        try {
            if (test != null) {
                val device = deviceService.getDeviceById(id) ?: throw DeviceNotFoundException(id)
                tpLinkRepository.callLightingService(device.copy(duration = 1, brightness = brightness, hue = hue, saturation = saturation))
                return prepareEditDeviceWithValue(model,
                        device,
                        brightness,
                        hue,
                        saturation
                )
            } else {
                deviceService.updateDevice(id, name, duration, brightness, hue, saturation)
                return getDevices(model)
            }
        } catch (ex: DeviceNotFoundException) {
            model.addAttribute("error", "No device found with the id ${ex.id}")
        } catch (ex: DeviceAlreadyExistException) {
            model.addAttribute("error", "A device already exists for the name ${ex.idOrName}")
        }
        return prepareEditDevice(id, model)
    }
}