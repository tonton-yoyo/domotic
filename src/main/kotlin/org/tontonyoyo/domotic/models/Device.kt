package org.tontonyoyo.domotic.models

data class Device(val name: String,
                  val id: String,
                  val type: DeviceType,
                  val duration: Int,
                  val brightness: Int,
                  val hue: Int?,
                  val saturation: Int?)

enum class DeviceType {
    SIMPLE_BULB,
    COLOR_BULB
}