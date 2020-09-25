package org.tontonyoyo.domotic.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.tontonyoyo.domotic.models.Device
import org.tontonyoyo.domotic.models.DeviceType
import java.io.File
import javax.annotation.PostConstruct

@Service
class DeviceService(@Autowired val objectMapper: ObjectMapper,
                    @Value("\${configFile}") val configFile: String) {

    private lateinit var devices: MutableMap<String, Device>

    @PostConstruct
    fun init() {
        devices = objectMapper.readValue<List<Device>>(File(configFile)).map { it.name to it }.toMap().toMutableMap()
    }

    private fun saveDevices() {
        objectMapper.writeValue(File(configFile), devices.values)
    }

    fun getDevices(): List<Device> = devices.values.sortedBy { it.name }.toList()

    fun getDeviceByName(name: String) = devices.get(name)

    fun createDevice(name: String, id: String, type: DeviceType, duration: Int, brightness: Int, hue: Int?, saturation: Int?) {
        if (devices.containsKey(name)) throw DeviceAlreadyExistException(name)
        devices[name] = Device(name, id, type, duration, brightness, hue, saturation)
        saveDevices()
    }

    fun updateDevice(name: String, duration: Int, brightness: Int, hue: Int?, saturation: Int?) {
        val device = devices[name] ?: throw DeviceNotFoundException(name)
        devices[name] = device.copy(duration = duration, brightness = brightness, hue = hue, saturation = saturation)
        saveDevices()
    }

    fun removeDevice(name: String) {
        if (!devices.containsKey(name)) throw DeviceNotFoundException(name)
        devices.remove(name)
        saveDevices()
    }
}

class DeviceNotFoundException(val name: String) : RuntimeException()

class DeviceAlreadyExistException(val name: String) : RuntimeException()