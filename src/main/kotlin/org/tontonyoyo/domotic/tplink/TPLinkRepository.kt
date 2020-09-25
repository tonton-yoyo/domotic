package org.tontonyoyo.domotic.tplink

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.tontonyoyo.domotic.models.Device


@Repository
class TPLinkRepository @Autowired constructor(
        private val objectMapper: ObjectMapper,
        private val restTemplate: RestTemplate,
        @Value("\${token}") val token: String) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun callLightingService(device: Device) {
        try {
            val url = "https://wap.tplinkcloud.com?token=${token}"
            val request = HttpEntity(buildBody(device))
            val responseBody = restTemplate.exchange(url, HttpMethod.POST, request, Response::class.java).body
            if (responseBody == null) {
                logger.error("Error during call of LightingService for $device. Response null")
            } else if (responseBody.errorCode != 0 || responseBody.msg != null) {
                logger.error("Error during call of LightingService for $device. " +
                        "ErrorCode = ${responseBody.errorCode} " +
                        "Msg = ${responseBody.msg ?: ""}")
            }
        } catch (e: RestClientException) {
            logger.error("Error during call of LightingService for $device", e)
        }
    }

    private fun buildBody(device: Device): Request {
        val transitionLightState = TransitionLightState(
                transitionPeriod = device.duration,
                brightness = device.brightness,
                hue = device.hue,
                saturation = device.saturation
        )
        val lightService = LightService(transitionLightState)
        val requestData = objectMapper.writeValueAsString(RequestData(lightService))
        return Request(params = RequestParams(device.id, requestData))
    }

    data class Request(val method: String = "passthrough", val params: RequestParams)

    data class RequestParams(val deviceId: String, val requestData: String)

    data class RequestData(
            @JsonProperty("smartlife.iot.smartbulb.lightingservice")
            val lightService: LightService
    )

    data class LightService(
            @JsonProperty("transition_light_state")
            val transitionLightState: TransitionLightState
    )

    data class TransitionLightState(
            @JsonProperty("ignore_default")
            val ignoreDefault: Int = 1,
            @JsonProperty("on_off")
            val onOff: Int = 1,
            @JsonProperty("transition_period")
            val transitionPeriod: Int,
            val brightness: Int,
            val hue: Int?,
            val saturation: Int?
    )

    data class Response(val errorCode: Int, val msg: String?, val result: Result?)

    data class Result(val responseData: String)
}