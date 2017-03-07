// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import org.json.JSONArray;

public interface DataBusHandler {
	/**
	 * リモート側からデータを受信
	 *
	 * @param data 受信したデータ
	 */
	void onReceive(JSONArray data);
}
