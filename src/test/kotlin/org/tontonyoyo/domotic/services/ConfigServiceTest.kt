package org.tontonyoyo.domotic.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.*
import org.junit.Before
import org.junit.Test
import org.tontonyoyo.domotic.models.Config
import org.tontonyoyo.domotic.models.Duration
import org.tontonyoyo.domotic.models.DurationType
import java.io.File

class ConfigServiceTest {

    private val config1 = Config("1", "device1", Duration(1, DurationType.MINUTE), 100, 30000, 180, 75)

    private lateinit var objectMapper: ObjectMapper
    private lateinit var configService: ConfigService

    @Before
    fun setUp() {
        objectMapper = jacksonObjectMapper()
        val originalFile = javaClass.getResource("/config-test.json").file
        val configFile = File.createTempFile("config-test", ".json")
        File(originalFile).copyTo(configFile, true)
        println("Config File = ${configFile.path}")
        configService = ConfigService(objectMapper, configFile.path)
        configService.init()
    }

    @Test
    fun `getConfigByDeviceId should returns null for unknown deviceId`() {
        // Given

        // When
        val result = configService.getConfigByDeviceId("3")

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getConfigByDeviceId should returns the requested deviceId`() {
        // Given

        // When
        val result = configService.getConfigByDeviceId("1")

        // Then
        assertThat(result).isEqualTo(config1)
    }

    @Test
    fun `getDeviceByName should returns null for unknown name`() {
        // Given

        // When
        val result = configService.getConfigByName("device3")

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getDeviceByName should returns the requested name`() {
        // Given

        // When
        val result = configService.getConfigByName("device1")

        // Then
        assertThat(result).isEqualTo(config1)
    }

    @Test(expected = NameAlreadyExistException::class)
    fun `createUpdateConfig should throw an exception if the config with the given name already exists`() {
        // Given

        // When
        configService.createUpdateConfig("new Id", "device1", Duration(2, DurationType.MINUTE), 100, 30000, 0, 0)

        // Then
        // An exception is expected
    }

    @Test
    fun `createUpdateConfig should create a new config and save it if the config doesn't exist`() {
        // Given
        // the service with 2 devices : device1 and device2
        val expectedNewDevice = Config("3", "device3", Duration(2, DurationType.MINUTE), 100, 30000, 0, 0)

        // When
        configService.createUpdateConfig("3", "device3", Duration(2, DurationType.MINUTE), 100, 30000, 0, 0)

        // Then
        assertThat(configService.getConfigByName("device3")).isEqualTo(expectedNewDevice)
        val newList = objectMapper.readValue<List<Config>>(File(configService.configFile))
        assertThat(newList).contains(expectedNewDevice)
    }

    @Test
    fun `removeDevice should throw an exception if the device doesn't exist`() {
        // Given
        val originalList = objectMapper.readValue<List<Config>>(File(configService.configFile))

        // When
        configService.removeConfig("3")

        // Then
        val finalList = objectMapper.readValue<List<Config>>(File(configService.configFile))
        assertThat(finalList).isEqualTo(originalList)
    }

    @Test
    fun `removeDevice should delete the device and save it if the device exists`() {
        // Given

        // When
        configService.removeConfig("1")

        // Then
        assertThat(configService.getConfigByName("device1")).isNull()
        val newList = objectMapper.readValue<List<Config>>(File(configService.configFile))
        assertThat(newList).doesNotContain(config1)
    }
}