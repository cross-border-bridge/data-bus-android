// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import org.json.JSONArray;
import org.json.JSONException;

public class MemoryQueueDataBus extends DataBus {
	private final MemoryQueue sendingMemoryQueue;
	private final MemoryQueue receivingMemoryQueue;
	private final MemoryQueueHandler listener;

	public MemoryQueueDataBus(MemoryQueue sendingMemoryQueue, MemoryQueue receivingMemoryQueue) {
		this.sendingMemoryQueue = sendingMemoryQueue;
		this.receivingMemoryQueue = receivingMemoryQueue;
		listener = (new MemoryQueueHandler() {
			@Override
			public void onReceive(String data) {
				try {
					received(new JSONArray(data));
				} catch (JSONException e) {
					if (Logger.enabled) e.printStackTrace();
				}
			}
		});
		this.receivingMemoryQueue.addListener(listener);
	}

	@Override
	public void send(JSONArray data) {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		this.sendingMemoryQueue.send(data.toString());
	}

	@Override
	public void destroy() {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		this.receivingMemoryQueue.removeListener(listener);
		super.destroy();
	}
}
