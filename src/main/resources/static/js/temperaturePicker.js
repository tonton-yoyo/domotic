class TemperaturePicker {
    constructor(inputId, previewId) {
        this.input = $(inputId)
        this.preview = $(previewId)
    }

    init() {
        let instance = this;
        this.input.change(function () {
            instance._onValueChange()
        })
        this._onValueChange()
    }

    _onValueChange() {
        var kelvin = this.input.val()
        var rgb = this._convertColorTempToRGB(kelvin)
        this.preview.css("background-color", "rgb(" + rgb.r + "," + rgb.g + "," + rgb.b + ")");
    }

    _convertColorTempToRGB(kelvin) {
        var temp = kelvin / 100;
        var red, green, blue;
        if( temp <= 66 ){
            red = 255;
            green = temp;
            green = 99.4708025861 * Math.log(green) - 161.1195681661;
            if( temp <= 19){
                blue = 0;
            } else {
                blue = temp-10;
                blue = 138.5177312231 * Math.log(blue) - 305.0447927307;
            }
        } else {
            red = temp - 60;
            red = 329.698727446 * Math.pow(red, -0.1332047592);
            green = temp - 60;
            green = 288.1221695283 * Math.pow(green, -0.0755148492 );
            blue = 255;
        }
        return {
            r : Math.min(Math.max(red,0), 255),
            g : Math.min(Math.max(green,0), 255),
            b : Math.min(Math.max(blue,0), 255),
        }
    }
}
