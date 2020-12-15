// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DataBus {
	private final Object locker = new Object();
	private final List<DataBusHandler> handlers = Collections.synchronizedList(new ArrayList<DataBusHandler>());
	private final Object lockerForErrorListeners = new Object();
	private final List<DataBusErrorListener> errorListeners =  Collections.synchronizedList(new ArrayList<DataBusErrorListener>());
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
		synchronized (locker) {
			if (0 <= handlers.indexOf(handler)) {
				return;
			}
			handlers.add(handler);
		}
	}

	public void removeHandler(DataBusHandler handler) {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		synchronized (locker) {
			while (handlers.contains(handler)) {
				handlers.remove(handler);
			}
		}
	}

	public void removeAllHandlers() {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		synchronized (locker) {
			handlers.clear();
		}
	}

	public void addErrorListener(DataBusErrorListener errorListener) {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		synchronized (lockerForErrorListeners) {
			if (0 <= errorListeners.indexOf(errorListener)) {
				return;
			}
			errorListeners.add(errorListener);
		}
	}

	public void removeErrorListener(DataBusErrorListener errorListener) {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		synchronized (lockerForErrorListeners) {
			while (errorListeners.contains(errorListener)) {
				errorListeners.remove(errorListener);
			}
		}
	}

	public void removeAllErrorListener() {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		synchronized (lockerForErrorListeners) {
			errorListeners.clear();
		}
	}

	public int getHandlerCount() {
		if (destroyed) {
			Logger.e("already destroyed");
			return 0;
		}
		return handlers.size();
	}

	protected void received(JSONArray data) {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		synchronized (locker) {
			for (DataBusHandler h : handlers) {
				h.onReceive(data);
			}
		}
	}

	protected void onOutOfMemoryError(OutOfMemoryError error) {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		synchronized (lockerForErrorListeners) {
			for (DataBusErrorListener l : errorListeners) {
				l.onOutOfMemoryError(error);
			}
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
