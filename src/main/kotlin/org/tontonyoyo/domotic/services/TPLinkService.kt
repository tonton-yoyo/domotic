package org.tontonyoyo.domotic.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.tontonyoyo.domotic.models.Duration
import org.tontonyoyo.domotic.models.DurationType
import org.tontonyoyo.domotic.models.TPLinkDevice
import org.tontonyoyo.domotic.tplink.TPLinkRepository

@Service
class TPLinkService @Autowired constructor(val tpLinkRepository: TPLinkRepository) {

    fun getDevices(): List<TPLinkDevice> = tpLinkRepository.getDevices()

    fun getDevice(deviceId: String): TPLinkDevice? = tpLinkRepository.getDevices().find { it.deviceId == deviceId }

    fun turnOn(deviceId: String): Unit =
            tpLinkRepository.transitionLightState(deviceId, true)

    fun turnOff(deviceId: String): Unit =
            tpLinkRepository.transitionLightState(deviceId, false)

    fun transitionLightStateOn(deviceId: String,
                               duration: Duration,
                               brightness: Int,
                               temperature: Int,
                               hue: Int,
                               saturation: Int) {
        val transitionPeriod: Int = when (duration.type) {
            DurationType.MINUTE -> duration.value * 60 * 1000
            DurationType.SECOND -> duration.value * 1000
        }

        tpLinkRepository.transitionLightState(deviceId, true, transitionPeriod, brightness, temperature, hue, saturation)
    }

}