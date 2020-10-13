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
import org.tontonyoyo.domotic.models.Config
import org.tontonyoyo.domotic.models.Duration
import org.tontonyoyo.domotic.models.DurationType
import org.tontonyoyo.domotic.services.ConfigService
import org.tontonyoyo.domotic.services.TPLinkService

@RunWith(MockitoJUnitRunner::class)
class IFTTTControllerTest {

    @Mock
    private lateinit var configService: ConfigService

    @Mock
    private lateinit var tpLinkService: TPLinkService

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
        val duration = Duration(1, DurationType.MINUTE)
        val device = Config("1", "123456789", duration, 100, 30000, 180, 75)
        given(configService.getConfigByName("123456789")).willReturn(device)

        // When
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/sunrise/123456789"))

        // Then
        response.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("Done"))
        verify(tpLinkService).transitionLightStateOn(eq("1"), eq(duration), eq(100), eq(30000), eq(180), eq(75))
    }

    @Test
    fun `sunrise should not call TPLInK repository if the device is not found`() {
        //Given
        given(configService.getConfigByName("123456789")).willReturn(null)

        // When
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/sunrise/123456789"))

        // Then
        response.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("Done"))
        verify(tpLinkService, never()).transitionLightStateOn(any(), any(), any(), any(), any(), any())
    }
}