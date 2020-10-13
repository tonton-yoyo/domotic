class DevicesList {
    constructor(container) {
        this.container = $(container)
    }

    init() {
        let instance = this;
        callServer("GET", "/devices", null, function(data) {
            $.each(data, function(index, device) {instance._drawDevice(device)})
            instance.container.show()
        });
    }

    _drawDevice(device) {
        var link = $("<a href=\"#\" class=\"list-group-item list-group-item-action\"></a>");
        link.text(device.alias)
        link.attr("href", "/edit?deviceId=" + device.deviceId);
        if (device.status) {
            link.addClass("list-group-item-success")
        } else {
            link.addClass("list-group-item-light")
        }
        this.container.append(link)
    }
}

$(function() {
    new DevicesList("#devicesList").init();
});
