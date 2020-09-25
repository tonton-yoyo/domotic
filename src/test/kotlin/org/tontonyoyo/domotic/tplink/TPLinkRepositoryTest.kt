package org.tontonyoyo.domotic.tplink

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import org.tontonyoyo.domotic.models.Device
import org.tontonyoyo.domotic.models.DeviceType

class TPLinkRepositoryTest {

    private lateinit var server: MockRestServiceServer
    private lateinit var repository: TPLinkRepository

    @Before
    fun setUp() {
        val restTemplate = RestTemplate()
        val objectMapper = jacksonObjectMapper()
        server = MockRestServiceServer.bindTo(restTemplate).build()
        repository = TPLinkRepository(objectMapper, restTemplate, "token")
    }

    @Test
    fun `repository should serialize the request when callLightingService on repository is called`() {
        // Given
        val expectedBody = """{
            "method": "passthrough", 
            "params": {
            "deviceId": "123456789", 
            "requestData": "{\"smartlife.iot.smartbulb.lightingservice\":{\"transition_light_state\":{\"ignore_default\":1,\"on_off\":1,\"transition_period\":30000,\"brightness\":100,\"color_temp\":null,\"hue\":180,\"saturation\":50}}}"
            }
        }""".trimEnd()
        val device = Device("123456789", "device", DeviceType.SIMPLE_BULB, 30000, 100, null, 180, 50)
        server.expect(MockRestRequestMatchers.requestTo("https://wap.tplinkcloud.com?token=token"))
                .andExpect(MockRestRequestMatchers.content().json(expectedBody))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess("{\"error_code\": -1,\"msg\": \"a arror\"}", MediaType.APPLICATION_JSON))

        // When
        repository.callLightingService(device)

        // Then
        server.verify()
    }

    @Test
    fun `repository should manage no response (null body) from TPLink`() {
        // Given
        val device = Device("123456789", "device", DeviceType.SIMPLE_BULB, 30000, 100, null, 180, 50)
        server.expect {}.andRespond(MockRestResponseCreators.withSuccess().contentType(MediaType.APPLICATION_JSON))

        // When
        repository.callLightingService(device)

        // Then
        server.verify()
    }

    @Test
    fun `repository should deserialize the response from TPLink`() {
        // Given
        val response = "{\n" +
                "    \"error_code\": 0,\n" +
                "    \"result\": {\n" +
                "        \"responseData\": \"{\\\"smartlife.iot.smartbulb.lightingservice\\\":{\\\"transition_light_state\\\":{\\\"on_off\\\":1,\\\"mode\\\":\\\"normal\\\",\\\"hue\\\":0,\\\"saturation\\\":0,\\\"color_temp\\\":0,\\\"brightness\\\":100,\\\"err_code\\\":0}}}\"\n" +
                "    }\n" +
                "}"
        val device = Device("123456789", "device", DeviceType.SIMPLE_BULB, 30000, 100, null, 180, 50)
        server.expect {}.andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        // When
        repository.callLightingService(device)

        // Then
        server.verify()
    }

    @Test
    fun `repository should catch exception during calling of TPLink`() {
        // Given
        val device = Device("123456789", "device", DeviceType.SIMPLE_BULB, 30000, 100, null, 180, 50)
        server.expect {}.andRespond(MockRestResponseCreators.withServerError())

        // When
        repository.callLightingService(device)

        // Then
        server.verify()
    }
}