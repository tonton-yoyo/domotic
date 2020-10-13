package org.tontonyoyo.domotic.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.tontonyoyo.domotic.models.Config
import org.tontonyoyo.domotic.models.Duration
import java.io.File
import javax.annotation.PostConstruct

@Service
class ConfigService @Autowired constructor(val objectMapper: ObjectMapper,
                                           @Value("\${configFile}") val configFile: String) {

    private lateinit var configs: MutableList<Config>

    @PostConstruct
    fun init() {
        val file = File(configFile)
        configs = if (file.exists()) objectMapper.readValue<List<Config>>(file).toMutableList()
        else mutableListOf()
    }

    private fun saveDevices() {
        objectMapper.writeValue(File(configFile), configs)
    }

    fun getConfigByDeviceId(deviceId: String): Config? =
            configs.find { it.deviceId == deviceId }


    fun getConfigByName(name: String) = configs.find { it.name == name }

    fun createUpdateConfig(deviceId: String,
                           name: String,
                           duration: Duration,
                           brightness: Int,
                           temperature: Int,
                           hue: Int,
                           saturation: Int) {
        if (configs.any { it.deviceId != deviceId && it.name == name }) throw NameAlreadyExistException(name)
        val config = getConfigByDeviceId(deviceId)
        if (config == null) {
            configs.add(Config(deviceId, name, duration, brightness, temperature, hue, saturation))
        } else {
            configs.remove(config)
            configs.add(config.copy(name = name,
                    duration = duration,
                    brightness = brightness,
                    temperature = temperature,
                    hue = hue,
                    saturation = saturation))
        }
        saveDevices()
    }

    fun removeConfig(deviceId: String) {
        val config = getConfigByDeviceId(deviceId)
        if (config != null) {
            configs.remove(config)
            saveDevices()
        }
    }
}

class NameAlreadyExistException(val idOrName: String) : RuntimeException()