function displayError(error) {
    $("#errorContainer").text(error).show();
}

function callServer(method, url, data, success) {
    $("#errorContainer").hide();
    return $.ajax({
        method: method,
        url: url,
        data: data
    }).fail(function() {
        displayError("Unable to call server")
    }).done(function(data) {
        if (data != null && data.error !== undefined) {
            displayError(data.error)
        } else {
            success(data);
        }
    });
}

function getParameter(name) {
    let param = location.search.substr(1).split("&")
        .map(item => item.split("="))
        .filter(item => item[0] === name)
    if (param.length === 1) return param[0][1]
    else return null;
}