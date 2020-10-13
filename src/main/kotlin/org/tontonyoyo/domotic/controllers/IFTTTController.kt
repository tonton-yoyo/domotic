package org.tontonyoyo.domotic.controllers

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.tontonyoyo.domotic.services.ConfigService
import org.tontonyoyo.domotic.services.TPLinkService
import org.tontonyoyo.domotic.tplink.TPLInkException

@RestController
class IFTTTController @Autowired constructor(val configService: ConfigService, val tpLinkService: TPLinkService) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("sunrise/{name}")
    fun sunrise(@PathVariable("name") name: String): String {
        val config = configService.getConfigByName(name)
        if (config == null) {
            logger.error("No config found for $name")
        } else {
            try {
                tpLinkService.transitionLightStateOn(config.deviceId, config.duration, config.brightness,config.temperature, config.hue, config.saturation)
            } catch (ex: TPLInkException) {
                logger.error("Error during call transitionLightStateOn", ex)
            }
        }
        return "Done"
    }
}