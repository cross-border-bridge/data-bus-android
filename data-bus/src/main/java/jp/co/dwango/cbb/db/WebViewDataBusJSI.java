// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import android.webkit.JavascriptInterface;

import org.json.JSONArray;

class WebViewDataBusJSI {
	private final WebViewDataBusJSIHandler handler;

	WebViewDataBusJSI(WebViewDataBusJSIHandler handler) {
		this.handler = handler;
	}

	// js から native へのメッセージ
	@JavascriptInterface
	public void send(String data) {
		try {
			Logger.d("received: " + data);
			handler.onSend(new JSONArray(data));
		} catch (Exception e) {
			if (Logger.enabled) e.printStackTrace();
		}
	}
}
