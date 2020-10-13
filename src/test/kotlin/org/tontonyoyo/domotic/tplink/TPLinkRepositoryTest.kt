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
import org.tontonyoyo.domotic.models.DeviceType
import org.tontonyoyo.domotic.models.TPLinkDevice

import org.junit.Assert.*

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

    @Test(expected = TPLInkException::class)
    fun `repository should catch exception during a 'getDeviceList' request`() {
        // Given
        server.expect {}.andRespond(MockRestResponseCreators.withServerError())

        // When
        repository.getDevices()
    }

    @Test(expected = TPLInkException::class)
    fun `repository should manage no response (null body) for 'getDeviceList' request`() {
        // Given
        server.expect {}.andRespond(MockRestResponseCreators.withSuccess().contentType(MediaType.APPLICATION_JSON))

        // When
        repository.getDevices()
    }

    @Test
    fun `repository should manage a 'error' result for 'getDeviceList' request`() {
        // Given
        server.expect{}.andRespond(
                MockRestResponseCreators.withSuccess("""{"error_code": 1, "msg": "a msg"}""", MediaType.APPLICATION_JSON)
        )

        // When
        try {
            repository.getDevices()

            fail("An exception must be throw")
        } catch (ex: TPLInkException) {
            // Then
            assertEquals(1, ex.errCode)
            assertEquals("a msg", ex.msg)
        }
    }

    @Test
    fun `repository should manage a null result for 'getDeviceList' request`() {
        // Given
        server.expect{}.andRespond(
                MockRestResponseCreators.withSuccess("""{"error_code": 0}""", MediaType.APPLICATION_JSON)
        )

        // When
        val result = repository.getDevices()

        // Then
        server.verify()
        assertEquals(result, listOf<TPLinkDevice>())
    }

    @Test
    fun `repository should return the list of devices`() {
        // Given
        val response = """
        {
            "error_code": 0,
            "result": {
                "deviceList": [
                    {
                        "deviceId": "device1",
                        "alias": "Lampe 1",
                        "deviceModel": "LB100(EU)",
                        "status": 0
                    },
                    {
                        "deviceId": "device2",
                        "alias": "Lampe 2",
                        "deviceModel": "LB130(EU)",
                        "status": 0
                    },
                    {
                        "deviceId": "device3",
                        "alias": "Lampe 3",
                        "deviceModel": "LB100(EU)",
                        "status": 1
                    },
                    {
                        "deviceId": "device4",
                        "alias": "Lampe 4",
                        "deviceModel": "LB110(EU)",
                        "status": 0
                    }
                ]
            }
        }""".trimIndent()
        val expectedBody = """{"method": "getDeviceList"}"""
        server.expect(MockRestRequestMatchers.requestTo("https://wap.tplinkcloud.com?token=token"))
                .andExpect(MockRestRequestMatchers.content().json(expectedBody))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        // When
        val result = repository.getDevices()

        // Then
        assertEquals(listOf(
                TPLinkDevice("device1", "Lampe 1", DeviceType.SIMPLE_BULB, false),
                TPLinkDevice("device2", "Lampe 2", DeviceType.COLOR_BULB, false),
                TPLinkDevice("device3", "Lampe 3", DeviceType.SIMPLE_BULB, true),
                TPLinkDevice("device4", "Lampe 4", DeviceType.SIMPLE_BULB, false)
        ), result)
    }

    @Test(expected = TPLInkException::class)
    fun `repository should catch exception during a 'transitionLightState' request`() {
        // Given
        server.expect {}.andRespond(MockRestResponseCreators.withServerError())

        // When
        repository.transitionLightState("123456789", true, 30000, 100, null, 180, 50)
    }

    @Test(expected = TPLInkException::class)
    fun `repository should manage no response (null body) for 'transitionLightState' request`() {
        // Given
        server.expect {}.andRespond(MockRestResponseCreators.withSuccess().contentType(MediaType.APPLICATION_JSON))

        // When
        repository.transitionLightState("123456789", true, 30000, 100, null, 180, 50)
    }

    @Test
    fun `repository should manage a 'error' result for 'transitionLightState' request`() {
        // Given
        server.expect{}.andRespond(
                MockRestResponseCreators.withSuccess("""{"error_code": 1, "msg": "a msg"}""", MediaType.APPLICATION_JSON)
        )

        // When
        try {
            repository.transitionLightState("123456789", true, 30000, 100, null, 180, 50)

            fail("An exception must be throw")
        } catch (ex: TPLInkException) {
            // Then
            assertEquals(1, ex.errCode)
            assertEquals("a msg", ex.msg)
        }
    }

    @Test
    fun `repository should manage a null result for 'transitionLightState' request`() {
        // Given
        server.expect{}.andRespond(
                MockRestResponseCreators.withSuccess("""{"error_code": 0}""", MediaType.APPLICATION_JSON)
        )

        // When
        repository.transitionLightState("123456789", true, 30000, 100, null, 180, 50)

        // Then
        server.verify()
    }

    @Test
    fun `repository should call 'transition_light_state' service with minimal parameters`() {
        // Given
        val expectedBody = """{
            "method": "passthrough", 
            "params": {
            "deviceId": "123456789", 
            "requestData": "{\"smartlife.iot.smartbulb.lightingservice\":{\"transition_light_state\":{\"ignore_default\":1,\"on_off\":0}}}"
            }
        }""".trimEnd()
        val response = "{\n" +
                "    \"error_code\": 0,\n" +
                "    \"result\": {\n" +
                "        \"responseData\": \"{\\\"smartlife.iot.smartbulb.lightingservice\\\":{\\\"transition_light_state\\\":{\\\"on_off\\\":0,\\\"mode\\\":\\\"normal\\\",\\\"hue\\\":0,\\\"saturation\\\":0,\\\"color_temp\\\":0,\\\"brightness\\\":100,\\\"err_code\\\":0}}}\"\n" +
                "    }\n" +
                "}"

        server.expect(MockRestRequestMatchers.requestTo("https://wap.tplinkcloud.com?token=token"))
                .andExpect(MockRestRequestMatchers.content().json(expectedBody))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        // When
        repository.transitionLightState("123456789", false)

        // Then
        server.verify()
    }

    @Test
    fun `repository should call 'transition_light_state' service with all parameters`() {
        // Given
        val expectedBody = """{
            "method": "passthrough", 
            "params": {
            "deviceId": "123456789", 
            "requestData": "{\"smartlife.iot.smartbulb.lightingservice\":{\"transition_light_state\":{\"ignore_default\":1,\"on_off\":1,\"transition_period\":30000,\"brightness\":100,\"hue\":180,\"saturation\":50}}}"
            }
        }""".trimEnd()
        val response = "{\n" +
                "    \"error_code\": 0,\n" +
                "    \"result\": {\n" +
                "        \"responseData\": \"{\\\"smartlife.iot.smartbulb.lightingservice\\\":{\\\"transition_light_state\\\":{\\\"on_off\\\":1,\\\"mode\\\":\\\"normal\\\",\\\"hue\\\":0,\\\"saturation\\\":0,\\\"color_temp\\\":0,\\\"brightness\\\":100,\\\"err_code\\\":0}}}\"\n" +
                "    }\n" +
                "}"

        server.expect(MockRestRequestMatchers.requestTo("https://wap.tplinkcloud.com?token=token"))
                .andExpect(MockRestRequestMatchers.content().json(expectedBody))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        // When
        repository.transitionLightState("123456789", true, 30000, 100, null, 180, 50)

        // Then
        server.verify()
    }
}