package org.tontonyoyo.domotic.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.tontonyoyo.domotic.models.Device
import org.tontonyoyo.domotic.models.DeviceType
import java.io.File

class DeviceServiceTest {

    private val device1 = Device("device1", "1", DeviceType.COLOR_BULB, 30000, 80, 180, 50)
    private val device2 = Device("device2", "2", DeviceType.SIMPLE_BULB, 30000, 100, null, null)

    private lateinit var objectMapper: ObjectMapper
    private lateinit var deviceService: DeviceService

    @Before
    fun setUp() {
        objectMapper = jacksonObjectMapper()
        val originalFile = javaClass.getResource("/config-test.json").file
        val configFile = File.createTempFile("config-test", ".json")
        File(originalFile).copyTo(configFile, true)
        println("Config File = ${configFile.path}")
        deviceService = DeviceService(objectMapper, configFile.path)
        deviceService.init()
    }

    @Test
    fun `getDevices should returns all devices`() {
        // Given
        // the service with 2 devices : device1 and device2

        // When
        val result = deviceService.getDevices()

        // Then
        Assertions.assertThat(result).isEqualTo(listOf(device1, device2))
    }

    @Test
    fun `getDeviceByName should returns null for unknown device`() {
        // Given
        // the service with 2 devices : device1 and device2

        // When
        val result = deviceService.getDeviceByName("device3")

        // Then
        Assertions.assertThat(result).isNull()
    }

    @Test
    fun `getDeviceByName should returns the requested device`() {
        // Given
        // the service with 2 devices : device1 and device2

        // When
        val result = deviceService.getDeviceByName("device1")

        // Then
        Assertions.assertThat(result).isEqualTo(device1)
    }

    @Test(expected = DeviceAlreadyExistException::class)
    fun `createDevice should throw an exception if the device already exists`() {
        // Given
        // the service with 2 devices : device1 and device2

        // When
        deviceService.createDevice("device1", "nexId", DeviceType.SIMPLE_BULB, 30000, 100, null, null)

        // Then
        // An exception is expected
    }

    @Test
    fun `createDevice should create a new device and save it if the device doesn't exist`() {
        // Given
        // the service with 2 devices : device1 and device2
        val expectedNewDevice = Device("device3", "nexId", DeviceType.SIMPLE_BULB, 30000, 100, null, null)

        // When
        deviceService.createDevice("device3", "nexId", DeviceType.SIMPLE_BULB, 30000, 100, null, null)

        // Then
        Assertions.assertThat(deviceService.getDeviceByName("device3")).isEqualTo(expectedNewDevice)
        val newList = objectMapper.readValue<List<Device>>(File(deviceService.configFile))
        Assertions.assertThat(newList).contains(expectedNewDevice)
    }

    @Test(expected = DeviceNotFoundException::class)
    fun `updateDevice should throw an exception if the device doesn't exist`() {
        // Given
        // the service with 2 devices : device1 and device2

        // When
        deviceService.updateDevice("device3", 30000, 100, null, null)

        // Then
        // An exception is expected
    }

    @Test
    fun `updateDevice should update the device and save it if the device exists`() {
        // Given
        // the service with 2 devices : device1 and device2
        val newDevice1 = device1.copy(duration = 120000, brightness = 100, hue = null, saturation = null)

        // When
        deviceService.updateDevice("device1", 120000, 100, null, null)

        // Then
        Assertions.assertThat(deviceService.getDeviceByName("device1")).isEqualTo(newDevice1)
        val newList = objectMapper.readValue<List<Device>>(File(deviceService.configFile))
        Assertions.assertThat(newList).contains(newDevice1)
        Assertions.assertThat(newList).doesNotContain(device1)
    }

    @Test(expected = DeviceNotFoundException::class)
    fun `removeDevice should throw an exception if the device doesn't exist`() {
        // Given
        // the service with 2 devices : device1 and device2

        // When
        deviceService.removeDevice("device3")

        // Then
        // An exception is expected
    }

    @Test
    fun `removeDevice should delete the device and save it if the device exists`() {
        // Given
        // the service with 2 devices : device1 and device2

        // When
        deviceService.removeDevice("device1")

        // Then
        Assertions.assertThat(deviceService.getDeviceByName("device1")).isNull()
        val newList = objectMapper.readValue<List<Device>>(File(deviceService.configFile))
        Assertions.assertThat(newList).doesNotContain(device1)
    }
}