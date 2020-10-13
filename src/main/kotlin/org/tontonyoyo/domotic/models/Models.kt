package org.tontonyoyo.domotic.models

data class Config(val deviceId: String,
                  val name: String,
                  val duration: Duration,
                  val brightness: Int,
                  val temperature: Int,
                  val hue: Int,
                  val saturation: Int)

data class TPLinkDevice(val deviceId: String, val alias: String, val deviceModel: DeviceType, val status: Boolean)

data class TPLinkDeviceAndConfig(val device:TPLinkDevice, val config: Config)

data class Duration(val value: Int, val type: DurationType)

enum class DeviceType {
    SIMPLE_BULB,
    COLOR_BULB
}

enum class DurationType {
    SECOND,
    MINUTE
}