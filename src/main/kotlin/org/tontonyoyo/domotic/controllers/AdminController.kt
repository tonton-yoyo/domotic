package org.tontonyoyo.domotic.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.tontonyoyo.domotic.exceptions.FunctionalException
import org.tontonyoyo.domotic.exceptions.ResourceNotFoundException
import org.tontonyoyo.domotic.models.*
import org.tontonyoyo.domotic.services.ConfigService
import org.tontonyoyo.domotic.services.TPLinkService
import org.tontonyoyo.domotic.tplink.TPLInkException
import javax.servlet.http.HttpServletRequest

@RestController
class AdminController @Autowired constructor(val configService: ConfigService, val tpLinkService: TPLinkService) {

    @GetMapping("/devices")
    fun getDevices(request: HttpServletRequest): List<TPLinkDevice> {
        UtilController.checkAuth(request)
        try {
            return tpLinkService.getDevices()
        } catch (e: TPLInkException) {
            throw FunctionalException(e.errCode, e.msg)
        }
    }

    @GetMapping("/devices/{deviceId}")
    fun getDevice(request: HttpServletRequest, @PathVariable("deviceId") deviceId: String): TPLinkDeviceAndConfig {
        UtilController.checkAuth(request)
        try {
            val device = tpLinkService.getDevice(deviceId) ?: throw ResourceNotFoundException()
            val config = configService.getConfigByDeviceId(deviceId) ?: Config(deviceId, "", Duration(1, DurationType.MINUTE), 100, 5750, 0, 0)
            return TPLinkDeviceAndConfig(device, config)
        } catch (e: TPLInkException) {
            throw FunctionalException(e.errCode, e.msg)
        }
    }

    @PostMapping("/devices/{deviceId}")
    fun saveConfig(request: HttpServletRequest, @PathVariable("deviceId") deviceId: String, configParam: ConfigParam): TPLinkDeviceAndConfig {
        UtilController.checkAuth(request)
        try {
            val device = tpLinkService.getDevice(deviceId)
            if (device == null) {
                throw ResourceNotFoundException()
            } else {
                configService.createUpdateConfig(deviceId,
                        configParam.name,
                        configParam.duration,
                        configParam.brightness,
                        configParam.temperature,
                        configParam.hue,
                        configParam.saturation)
            }
        } catch (e: TPLInkException) {
            throw FunctionalException(e.errCode, e.msg)
        }
        return getDevice(request, deviceId)
    }

    @DeleteMapping("/devices/{deviceId}")
    fun deleteConfig(request: HttpServletRequest, @PathVariable("deviceId") deviceId: String): TPLinkDeviceAndConfig {
        UtilController.checkAuth(request)
        try {
            configService.removeConfig(deviceId)
        } catch (e: TPLInkException) {
            throw FunctionalException(e.errCode, e.msg)
        }
        return getDevice(request, deviceId)
    }

    @PostMapping("/devices/{deviceId}/testConfig")
    fun testConfig(request: HttpServletRequest, @PathVariable("deviceId") deviceId: String, configParam: ConfigParam): TPLinkDeviceAndConfig {
        UtilController.checkAuth(request)
        try {
            val device = tpLinkService.getDevice(deviceId)
            if (device == null) {
                throw ResourceNotFoundException()
            } else {
                tpLinkService.turnOff(deviceId)
                tpLinkService.transitionLightStateOn(deviceId,
                        configParam.duration,
                        configParam.brightness,
                        configParam.temperature,
                        configParam.hue,
                        configParam.saturation)
            }
        } catch (e: TPLInkException) {
            throw FunctionalException(e.errCode, e.msg)
        }
        return getDevice(request, deviceId)
    }

    @PostMapping("/devices/{deviceId}/turnOn")
    fun turnOn(request: HttpServletRequest, @PathVariable("deviceId") deviceId: String, model: Model): TPLinkDeviceAndConfig {
        UtilController.checkAuth(request)
        try {
            tpLinkService.turnOn(deviceId)
        } catch (e: TPLInkException) {
            throw FunctionalException(e.errCode, e.msg)
        }
        return getDevice(request, deviceId)
    }

    @PostMapping("/devices/{deviceId}/turnOff")
    fun turnOff(request: HttpServletRequest, @PathVariable("deviceId") deviceId: String, model: Model): TPLinkDeviceAndConfig {
        UtilController.checkAuth(request)
        try {
            tpLinkService.turnOff(deviceId)
        } catch (e: TPLInkException) {
            throw FunctionalException(e.errCode, e.msg)
        }
        return getDevice(request, deviceId)
    }
}

data class ConfigParam(val name: String,
                       val durationValue: Int,
                       val durationType: DurationType,
                       val brightness: Int,
                       val temperature: Int,
                       val hue: Int,
                       val saturation: Int) {
    val duration = Duration(durationValue, durationType)
}