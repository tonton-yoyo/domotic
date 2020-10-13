class DeviceEdition {
    constructor() {
        this.deviceId = getParameter("deviceId")
        this.url = "/devices/" + this.deviceId
        this.device = null;
        this.config = null;
        this.inputName = $("#name");
        this.inputDurationValue = $("#durationValue");
        this.selectDurationType = $("#durationType");
        this.inputBrightness = $("#brightness");
        this.colorTempSelectorPanel = $("#colorTempSelectorPanel")
        this.temperatureSelector = $("#temperatureSelector");
        this.inputTemperature = $("#temperature");
        this.colorSelector = $("#colorSelector");
        this.inputHue = $("#hue");
        this.inputSaturation = $("#saturation");
        this.temperaturePanel = $("#temperaturePanel")
        this.colorPanel = $("#colorPanel")

        this.temperaturePicker = new TemperaturePicker("#temperature", "#temperaturePreview")
        this.temperaturePicker.init()

        this.colorPicker = new iro.ColorPicker("#colorPicker")
    }

    init() {
        let instance = this;

        $("#btnTurnOn").click(function() {instance.turnOn()})
        $("#btnTurnOff").click(function() {instance.turnOff()})
        $("#btnUpdate").click(function() {instance.update()})
        $("#btnTestConfig").click(function() {instance.testConfig()})
        $("#btnDelete").click(function() {instance.delete()})

        instance.temperatureSelector.change(function () {instance._onSelectorChange()})
        instance.colorSelector.change(function () {instance._onSelectorChange()})

        callServer("GET", this.url, null, function(data) {
            instance.device = data.device
            instance.config = data.config
            instance._initForm();
        });
    }

    _initForm() {
        $("h1").text(this.device.alias)
        this.inputName.val(this.config.name)
        this.inputDurationValue.val(this.config.duration.value)
        this.selectDurationType.val(this.config.duration.type)
        this.inputBrightness.val(this.config.brightness)
        if (this.device.deviceModel === 'COLOR_BULB') {
            this.colorTempSelectorPanel.show()
            this.temperatureSelector.attr('checked',this.config.temperature > 0)
            this.colorSelector.attr('checked',this.config.temperature === 0)
            if (this.config.temperature > 0) {
                this.inputTemperature.val(this.config.temperature)
            } else {
                this.inputHue.val(this.config.hue)
                this.inputSaturation.val(this.config.saturation)
                this.colorPicker.color.hsv = { h: this.config.hue, s: this.config.saturation, v: this.config.brightness};
            }
            this._onSelectorChange()
        }
        this.inputName.focus()
    }

    _onSelectorChange() {
        if (this.temperatureSelector.is(":checked")) {
            this.temperaturePanel.show()
            this.colorPanel.hide()
        } else {
            this.temperaturePanel.hide()
            this.colorPanel.show()
        }
    }

    turnOn() {
        callServer("POST", this.url + "/turnOn", null, function(data) {});
    }

    turnOff() {
        callServer("POST", this.url + "/turnOff", null, function(data) {});
    }

    update() {
        callServer("POST", this.url, this._serializeForm(), function(data) {
            window.location.href = "/";
        });
    }

    testConfig() {
        callServer("POST", this.url + "/testConfig", this._serializeForm(), function(data) {});
    }

    delete() {
        callServer("DELETE", this.url, null, function(data) {
            window.location.href = "/";
        });
    }

    _serializeForm() {
        var data = {}
        data.name = this.inputName.val()
        data.durationValue = this.inputDurationValue.val()
        data.durationType = this.selectDurationType.val()
        data.brightness = this.inputBrightness.val()
        data.temperature = 0
        data.hue = 0
        data.saturation = 0
        if (this.device.deviceModel === 'COLOR_BULB') {
            if (this.temperatureSelector.is(":checked")) {
                data.temperature = this.inputTemperature.val()
            } else {
                let color = this.colorPicker.color
                data.hue = color.hue
                data.saturation = color.saturation
                data.brightness = color.value
            }
        }
        return data;
    }
}

$(function() {
    new DeviceEdition().init();
});

