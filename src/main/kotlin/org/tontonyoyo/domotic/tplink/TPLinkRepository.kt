package org.tontonyoyo.domotic.tplink

import com.fasterxml.jackson.annotation.JsonInclude
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
import org.tontonyoyo.domotic.models.DeviceType
import org.tontonyoyo.domotic.models.TPLinkDevice

@Repository
class TPLinkRepository @Autowired constructor(
        private val objectMapper: ObjectMapper,
        private val restTemplate: RestTemplate,
        @Value("\${token}") val token: String) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val PASSTHROUGH = "passthrough"
    private val GET_DEVICE_LIST = "getDeviceList"

    fun getDevices(): List<TPLinkDevice> {
        val request = TPLinkRequest(GET_DEVICE_LIST)
        val response = callTPLink(request, TPLinkGetDevicesResponse::class.java).result
        return if (response != null) response.deviceList.map { device ->
            TPLinkDevice(device.deviceId, device.alias, convertModel(device.deviceModel), device.status == 1)
        } else {
            logger.error("TPLink doesn't return the list of devices")
            listOf()
        }
    }

    private fun convertModel(model: String) = when (model) {
        "LB100(EU)" -> DeviceType.SIMPLE_BULB
        "LB130(EU)" -> DeviceType.COLOR_BULB
        else -> DeviceType.SIMPLE_BULB
    }

    fun transitionLightState(deviceId: String,
                             onOff: Boolean,
                             transitionPeriod: Int? = null,
                             brightness: Int? = null,
                             temperature: Int? = null,
                             hue: Int? = null,
                             saturation: Int? = null) {
        val transitionLightState = TransitionLightState(
                onOff = if (onOff) 1 else 0,
                transitionPeriod = transitionPeriod,
                brightness = brightness,
                colorTemp = temperature,
                hue = hue,
                saturation = saturation
        )
        val lightService = LightService(transitionLightState)
        val requestData = objectMapper.writeValueAsString(RequestData(lightService))
        val request = TPLinkRequest(PASSTHROUGH, RequestParams(deviceId, requestData))
        callTPLink(request, TPLinkResultResponse::class.java)
    }

    private fun <T : TPLinkResponse> callTPLink(request: Any, responseClass: Class<T>): T {
        try {
            val url = "https://wap.tplinkcloud.com?token=${token}"
            val entity = HttpEntity(request)
            val responseBody = restTemplate.exchange(url, HttpMethod.POST, entity, responseClass).body
            if (responseBody == null) {
                logger.error("The TPLink response returns a null body")
                throw TPLInkException(-1, "Unknown error")
            } else if (responseBody.errorCode != 0 || responseBody.msg != null) {
                throw TPLInkException(responseBody.errorCode, responseBody.msg ?: "Unknown error")
            } else {
                return responseBody
            }
        } catch (e: RestClientException) {
            logger.error("An exception occurred during the TPLink call", e)
            throw TPLInkException(-1, "Internal error")
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class TPLinkRequest(val method: String, val params: RequestParams? = null)

    data class RequestParams(val deviceId: String?, val requestData: String?)

    data class RequestData(
            @JsonProperty("smartlife.iot.smartbulb.lightingservice")
            val lightService: LightService
    )

    data class LightService(
            @JsonProperty("transition_light_state")
            val transitionLightState: TransitionLightState
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class TransitionLightState(
            @JsonProperty("ignore_default")
            val ignoreDefault: Int = 1,
            @JsonProperty("on_off")
            val onOff: Int = 1,
            @JsonProperty("transition_period")
            val transitionPeriod: Int?,
            val brightness: Int?,
            @JsonProperty("color_temp")
            val colorTemp: Int?,
            val hue: Int?,
            val saturation: Int?
    )

    interface TPLinkResponse {
        val errorCode: Int
        val msg: String?
    }

    data class TPLinkGetDevicesResponse(
            @JsonProperty("error_code")
            override val errorCode: Int,
            override val msg: String?,
            val result: TPLinkGetDevicesResult?
    ) : TPLinkResponse

    data class TPLinkGetDevicesResult(val deviceList: List<TPLinkDeviceDetail>)
    data class TPLinkDeviceDetail(val deviceId: String, val alias: String, val deviceModel: String, val status: Int)

    data class TPLinkResultResponse(
            @JsonProperty("error_code")
            override val errorCode: Int,
            override val msg: String?,
            val result: Result?
    ) : TPLinkResponse

    data class Result(val responseData: String)
}

class TPLInkException(val errCode: Int, val msg: String) : RuntimeException()