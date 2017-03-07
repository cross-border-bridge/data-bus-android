// Copyright © 2017 DWANGO Co., Ltd.

if (!window.AndroidDataBusHandlers) {
    window.AndroidDataBusHandlers = new Array();
}

if (!window.AndroidDataBusNI) {
    window.AndroidDataBusNI = new Object();
}

if (!window.CBB) {
    window.CBB = new Object();
}

window.CBB.WebViewDataBus = (function() {
    function WebViewDataBus() {
        this._destroyed = false;
    }
    WebViewDataBus.prototype.send = function() {
        if (this._destroyed) return;
        var args = Array.prototype.slice.call(arguments, 0);
        window.AndroidDataBusJSI.send(JSON.stringify(args));
    }
    WebViewDataBus.prototype.addHandler = function(handler) {
        if (this._destroyed) return;
        for (var i = 0; i < window.AndroidDataBusHandlers.length; i++) {
            if (window.AndroidDataBusHandlers[i] === handler) {
                return;
            }
        }
        window.AndroidDataBusHandlers[window.AndroidDataBusHandlers.length] = handler;
    }
    WebViewDataBus.prototype.removeHandler = function(handler) {
        if (this._destroyed) return;
        for (var i = 0; i < window.AndroidDataBusHandlers.length; i++) {
            if (window.AndroidDataBusHandlers[i] === handler) {
                window.AndroidDataBusHandlers.splice(i, 1);
                return;
            }
        }
    }
    WebViewDataBus.prototype.removeAllHandlers = function() {
        if (this._destroyed) return;
        window.AndroidDataBusHandlers = new Array();
    }
    WebViewDataBus.prototype.destroy = function() {
        if (this._destroyed) return;
        this.removeAllHandlers();
        this._destroyed = true;
    }
    return WebViewDataBus;
})();

window.AndroidDataBusNI.onSend = function(data) {
    for (var i = 0; i < window.AndroidDataBusHandlers.length; i++) {
        window.AndroidDataBusHandlers[i].apply(this, data);
    }
};