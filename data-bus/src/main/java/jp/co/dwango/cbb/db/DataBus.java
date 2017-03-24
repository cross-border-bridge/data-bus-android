// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DataBus {
	private final List<DataBusHandler> handlers = Collections.synchronizedList(new ArrayList<DataBusHandler>());
	protected boolean destroyed = false;

	public static void logging(boolean enabled) {
		Logger.enabled = enabled;
	}

	public abstract void send(final JSONArray args);

	public void addHandler(DataBusHandler handler) {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		if (0 <= handlers.indexOf(handler)) {
			return;
		}
		handlers.add(handler);
	}

	public void removeHandler(DataBusHandler handler) {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		while (handlers.contains(handler)) {
			handlers.remove(handler);
		}
	}

	public void removeAllHandlers() {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		handlers.clear();
	}

	public int getHandlerCount() {
		if (destroyed) {
			Logger.e("already destroyed");
			return 0;
		}
		return handlers.size();
	}

	protected void received(JSONArray data) {
		for (DataBusHandler h : handlers) {
			h.onReceive(data);
		}
	}

	public void destroy() {
		if (!destroyed) {
			removeAllHandlers();
			destroyed = true;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			super.finalize();
		} finally {
			destroy();
		}
	}
}
