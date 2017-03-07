// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import org.json.JSONArray;
import org.json.JSONException;

public class MultiplexDataBus extends DataBus implements DataBusHandler {
	private final DataBus dataBus;
	private final String dataId;

	public MultiplexDataBus(DataBus dataBus, String dataId) {
		this.dataBus = dataBus;
		this.dataId = dataId;
		this.dataBus.addHandler(this);
	}

	@Override
	public void send(JSONArray data) {
		try {
			JSONArray packet = new JSONArray();
			packet.put(dataId);
			for (int i = 0; i < data.length(); i++) {
				packet.put(data.get(i));
			}
			dataBus.send(packet);
		} catch (JSONException e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onReceive(JSONArray packet) {
		try {
			if (packet.length() < 1) return;
			if (!dataId.equals(packet.get(0))) return;
			JSONArray data = new JSONArray();
			for (int i = 1; i < packet.length(); i++) data.put(packet.get(i));
			for (DataBusHandler h : handlers) h.onReceive(data);
		} catch (JSONException e) {
			Logger.printStackTrace(e);
		}
	}
}
