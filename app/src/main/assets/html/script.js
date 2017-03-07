// Copyright © 2017 DWANGO Co., Ltd.

var dataBus = new CBB.WebViewDataBus();

var handler = function() {
    var text = "";
    for (var i = 0; i < arguments.length; i++) {
        if (0 != i) text += ", ";
        text += arguments[i];
        console.log("received arguments[" + i + "] = " + arguments[i]);
    }
    alert(text);
};

function OnAddButtonClick() {
    dataBus.addHandler(handler);
}

function OnSendButtonClick() {
    console.log("button clicked");
    dataBus.send("arg1", 2, "3");
}

function OnRemoveButtonClick() {
    dataBus.removeHandler(handler);
}

function OnRemoveAllButtonClick() {
    dataBus.removeAllHandlers();
}

function OnDestroyButtonClick() {
    dataBus.destroy();
}