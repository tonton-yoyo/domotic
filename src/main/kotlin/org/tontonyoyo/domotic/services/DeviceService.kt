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

    private lateinit var devices: MutableList<Device>

    @PostConstruct
    fun init() {
        devices = objectMapper.readValue<List<Device>>(File(configFile)).toMutableList()
    }

    private fun saveDevices() {
        objectMapper.writeValue(File(configFile), devices)
    }

    fun getDevices(): List<Device> = devices.sortedBy { it.name }.toList()

    fun getDeviceByName(name: String) = devices.find { it.name == name }

    private fun getDeviceById(id: String) = devices.find { it.id == id }

    fun createDevice(id: String, name: String, type: DeviceType, duration: Int, brightness: Int, hue: Int?, saturation: Int?) {
        if (devices.any { it.id == id }) throw DeviceAlreadyExistException(id)
        if (devices.any { it.name == name }) throw DeviceAlreadyExistException(name)
        devices.add(Device(id, name, type, duration, brightness, hue, saturation))
        saveDevices()
    }

    fun updateDevice(id: String, name: String, duration: Int, brightness: Int, hue: Int?, saturation: Int?) {
        val device = getDeviceById(id) ?: throw DeviceNotFoundException(id)
        if (devices.any { it.id != id && it.name == name }) throw DeviceAlreadyExistException(name)
        devices.remove(device)
        devices.add(device.copy(name = name, duration = duration, brightness = brightness, hue = hue, saturation = saturation))
        saveDevices()
    }

    fun removeDevice(id: String) {
        val device = getDeviceById(id) ?: throw DeviceNotFoundException(id)
        devices.remove(device)
        saveDevices()
    }
}

class DeviceNotFoundException(val id: String) : RuntimeException()

class DeviceAlreadyExistException(val idOrName: String) : RuntimeException()