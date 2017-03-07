// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import android.util.Log;

class Logger {
	private static final String TAG = "DataBus";
	static boolean enabled;

	static void d(String message) {
		if (enabled) {
			Log.d(TAG, message);
		}
	}

	static void w(String message) {
		if (enabled) {
			Log.w(TAG, message);
		}
	}

	static void e(String message) {
		if (enabled) {
			Log.e(TAG, message);
		}
	}

	static void printStackTrace(Exception e) {
		if (enabled) {
			e.printStackTrace();
		}
	}
}
