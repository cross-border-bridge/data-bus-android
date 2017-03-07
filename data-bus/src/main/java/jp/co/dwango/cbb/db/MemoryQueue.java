// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import java.util.ArrayList;
import java.util.List;

/**
 * 同一プロセス内で識別名付きのテキストデータを交換できるIPC
 */
public class MemoryQueue {
	private final List<MemoryQueueHandler> handlers = new ArrayList<MemoryQueueHandler>();

	public MemoryQueue() {
	}

	void addListener(MemoryQueueHandler handler) {
		handlers.add(handler);
	}

	void removeListener(MemoryQueueHandler handler) {
		handlers.remove(handler);
	}

	void send(String data) {
		for (MemoryQueueHandler handler : handlers) {
			handler.onReceive(data);
		}
	}
}
