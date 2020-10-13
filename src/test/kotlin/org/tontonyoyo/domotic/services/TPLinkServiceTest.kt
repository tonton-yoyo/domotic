package org.tontonyoyo.domotic.services

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.tontonyoyo.domotic.models.DeviceType
import org.tontonyoyo.domotic.models.TPLinkDevice
import org.tontonyoyo.domotic.tplink.TPLinkRepository
import org.junit.Assert.*
import org.junit.Test
import org.tontonyoyo.domotic.models.Duration
import org.tontonyoyo.domotic.models.DurationType

@RunWith(MockitoJUnitRunner::class)
class TPLinkServiceTest {

    @Mock
    private lateinit var tpLinkRepository: TPLinkRepository

    @InjectMocks
    private lateinit var service: TPLinkService

    @Test
    fun `getDevices should call getDevices of the repository`() {
        // Given

        // When
        service.getDevices()

        // Then
        verify(tpLinkRepository).getDevices()
    }

    @Test
    fun `getDevice should return null if the deviceId it not found`() {
        // Given
        val device1 = TPLinkDevice("device1", "Lampe 1", DeviceType.SIMPLE_BULB, false)
        val device2 = TPLinkDevice("device2", "Lampe 2", DeviceType.COLOR_BULB, false)
        given(tpLinkRepository.getDevices()).willReturn(listOf(device1, device2))

        // When
        val result = service.getDevice("device3")

        // Then
        verify(tpLinkRepository).getDevices()
        assertNull(result)
    }

    @Test
    fun `getDevice should call getDevice of repository and filter the result`() {
        // Given
        val device1 = TPLinkDevice("device1", "Lampe 1", DeviceType.SIMPLE_BULB, false)
        val device2 = TPLinkDevice("device2", "Lampe 2", DeviceType.COLOR_BULB, false)
        given(tpLinkRepository.getDevices()).willReturn(listOf(device1, device2))

        // When
        val result = service.getDevice("device1")

        // Then
        verify(tpLinkRepository).getDevices()
        assertEquals(device1, result)
    }

    @Test
    fun `turnOn should call transitionLightState of the repository`() {
        // Given

        // When
        service.turnOn("device1")

        // Then
        verify(tpLinkRepository).transitionLightState(eq("device1"), eq(true), eq(null), eq(null), eq(null), eq(null), eq(null))
    }

    @Test
    fun `turnOff should call transitionLightState of the repository`() {
        // Given

        // When
        service.turnOff("device1")

        // Then
        verify(tpLinkRepository).transitionLightState(eq("device1"), eq(false), eq(null), eq(null), eq(null), eq(null), eq(null))
    }

    @Test
    fun `transitionLightStateOn should convert minutes and call transitionLightState of the repository`() {
        // Given

        // When
        service.transitionLightStateOn("device1", Duration(10, DurationType.MINUTE), 50, 25000, 180, 100)

        // Then
        verify(tpLinkRepository).transitionLightState(eq("device1"), eq(true), eq(600000), eq(50), eq(25000), eq(180), eq(100))
    }

    @Test
    fun `transitionLightStateOn should convert secondes and call transitionLightState of the repository`() {
        // Given

        // When
        service.transitionLightStateOn("device1", Duration(30, DurationType.SECOND), 50, 25000, 180, 100)

        // Then
        verify(tpLinkRepository).transitionLightState(eq("device1"), eq(true), eq(30000), eq(50), eq(25000), eq(180), eq(100))
    }
}