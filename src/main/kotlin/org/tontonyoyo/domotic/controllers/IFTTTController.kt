package org.tontonyoyo.domotic.controllers

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.tontonyoyo.domotic.services.DeviceService
import org.tontonyoyo.domotic.tplink.TPLinkRepository

@RestController
class IFTTTController @Autowired constructor(val deviceService: DeviceService, val tpLinkRepository: TPLinkRepository) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("sunrise/{name}")
    fun sunrise(@PathVariable("name") name: String): String {
        val device = deviceService.getDeviceByName(name)
        if (device == null) {
            logger.error("No device found for $name")
        } else {
            tpLinkRepository.callLightingService(device)
        }
        return "Done"
    }
}