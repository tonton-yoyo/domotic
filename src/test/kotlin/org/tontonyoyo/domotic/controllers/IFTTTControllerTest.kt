package org.tontonyoyo.domotic.controllers

import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.tontonyoyo.domotic.models.Device
import org.tontonyoyo.domotic.models.DeviceType
import org.tontonyoyo.domotic.services.DeviceService
import org.tontonyoyo.domotic.tplink.TPLinkRepository

@RunWith(MockitoJUnitRunner::class)
class IFTTTControllerTest {

    @Mock
    private lateinit var deviceService: DeviceService

    @Mock
    private lateinit var tpLinkRepository: TPLinkRepository

    @InjectMocks
    private lateinit var controller: IFTTTController

    private lateinit var mockMvc: MockMvc

    @Before
    fun setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build()
    }

    @Test
    fun `sunrise should call TPLInK repository if the device is known`() {
        //Given
        val device = Device("device", "123456789", DeviceType.SIMPLE_BULB, 30000, 100, null, 180, 50)
        given(deviceService.getDeviceByName("123456789")).willReturn(device)

        // When
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/sunrise/123456789"))

        // Then
        response.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("Done"))
        verify(tpLinkRepository).callLightingService(eq(device))
    }

    @Test
    fun `sunrise should not call TPLInK repository if the device is not found`() {
        //Given
        given(deviceService.getDeviceByName("123456789")).willReturn(null)

        // When
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/sunrise/123456789"))

        // Then
        response.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("Done"))
        verify(tpLinkRepository, never()).callLightingService(any())
    }
}