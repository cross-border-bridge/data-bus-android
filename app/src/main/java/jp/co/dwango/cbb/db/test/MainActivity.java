// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db.test;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jp.co.dwango.cbb.db.DataBus;
import jp.co.dwango.cbb.db.DataBusHandler;
import jp.co.dwango.cbb.db.WebViewDataBus;

public class MainActivity extends AppCompatActivity {
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// KITKAT以上の場合は Chrome でのデバッグを有効にする
		if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}
		setContentView(R.layout.activity_main);
		WebView webView = (WebView) findViewById(R.id.web_view);
		assert webView != null;

		// デバッグログ出力を有効化
		DataBus.logging(true);

		// WebView を 準備
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				Log.d("CrossBorderBridge-js", consoleMessage.message() + " (src=" + consoleMessage.sourceId() + ", line=" + consoleMessage.lineNumber() + ")");
				return super.onConsoleMessage(consoleMessage);
			}
		});

		// WebViewDataBusを用いるWebViewを指定してインスタンス化
		final WebViewDataBus dataBus = new WebViewDataBus(this, webView, true);

		// WebView(JavaScript) から メッセージ を受け取る ハンドラ を登録
		final DataBusHandler handler = new DataBusHandler() {
			@Override
			public void onReceive(JSONArray data) {
				Log.d("CrossBorderBridge-java", "received-from-js: " + data.toString());
				Toast.makeText(MainActivity.this, data.toString(), Toast.LENGTH_SHORT).show();
			}
		};

		findViewById(R.id.native_button_add).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataBus.addHandler(handler);
			}
		});

		findViewById(R.id.native_button_remove).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataBus.removeHandler(handler);
			}
		});

		findViewById(R.id.native_button_send).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataBus.send(new JSONArray().put("test%10%20%30test").put(2525).put(true));
			}
		});

		findViewById(R.id.native_button_destroy).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataBus.destroy();
			}
		});

		// WebView へコンテンツをロード
		String html = loadTextFromAsset("html/index.html");
		webView.loadDataWithBaseURL("", html.replace("$(WEB-VIEW-DATA-BUS)", dataBus.getInjectJavaScript()), "text/html", "UTF-8", null);
	}

	// assetsからテキストファイルを読み込む
	private String loadTextFromAsset(String path) {
		InputStream is = null;
		BufferedReader br = null;
		StringBuilder result = new StringBuilder(16384);
		try {
			is = getAssets().open(path);
			br = new BufferedReader(new InputStreamReader(is));
			String str;
			while ((str = br.readLine()) != null) {
				result.append(str).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != is) try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (null != br) try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}
}
