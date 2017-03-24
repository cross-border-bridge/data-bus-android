// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.util.Base64;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebViewDataBus extends DataBus {
	private static final String JAVASCRIPT_INTERFACE = "AndroidDataBusJSI";
	private final Context context;
	private final WebView webView;
	private boolean addedJavaScriptInterface = false;

	/**
	 * 初期化 (データの受け口を作成)
	 *
	 * @param context コンテキスト
	 * @param webView WebViewDataBus を使用する WebView
	 */
	@SuppressLint("AddJavascriptInterface")
	public WebViewDataBus(Context context, WebView webView) {
		this(context, webView, null);
	}

	/**
	 * 初期化 (データの受け口を作成)
	 *
	 * @param context        コンテキスト
	 * @param webView        WebViewDataBus を使用する WebView
	 * @param injectManually window.CBBの手動injectを行う場合はtrueを指定する
	 */
	@SuppressLint("AddJavascriptInterface")
	public WebViewDataBus(Context context, WebView webView, boolean injectManually) {
		this(context, webView, null, injectManually);
	}

	/**
	 * 初期化 (データの受け口を作成)
	 *
	 * @param context コンテキスト
	 * @param webView WebViewDataBus を使用する WebView
	 * @param client  WebViewClient
	 */
	public WebViewDataBus(Context context, WebView webView, final WebViewClient client) {
		this(context, webView, client, false);
	}

	/**
	 * 初期化 (データの受け口を作成)
	 *
	 * @param context        コンテキスト
	 * @param webView        WebViewDataBus を使用する WebView
	 * @param client         WebViewClient
	 * @param injectManually window.CBBの手動injectを行う場合はtrueを指定する
	 */
	@SuppressLint("AddJavascriptInterface")
	public WebViewDataBus(Context context, WebView webView, final WebViewClient client, boolean injectManually) {
		super();
		this.context = context;
		this.webView = webView;
		if (injectManually && null != client) {
			throw new IllegalArgumentException("Cannot specify client if injectManually");
		}
		this.webView.addJavascriptInterface(new WebViewDataBusJSI(
				new WebViewDataBusJSIHandler() {
					@Override
					public void onSend(JSONArray data) {
						for (DataBusHandler h : handlers) {
							h.onReceive(data);
						}
					}
				}
		), JAVASCRIPT_INTERFACE);
		addedJavaScriptInterface = true;
		if (injectManually) {
			return; // do not set client if injectManually
		}
		if (null != client) {
			this.webView.setWebViewClient(new WebViewClient() {
				@Override
				public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
					client.doUpdateVisitedHistory(view, url, isReload);
				}

				@Override
				public void onFormResubmission(WebView view, Message dontResend, Message resend) {
					client.onFormResubmission(view, dontResend, resend);
				}

				@Override
				public void onLoadResource(WebView view, String url) {
					client.onLoadResource(view, url);
				}

				@Override
				public void onPageCommitVisible(WebView view, String url) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						client.onPageCommitVisible(view, url);
					}
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					client.onPageFinished(view, url);
				}

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					injectJavaScript();
					client.onPageStarted(view, url, favicon);
				}

				@Override
				public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						client.onReceivedClientCertRequest(view, request);
					}
				}

				@Override
				public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						client.onReceivedError(view, request, error);
					}
				}

				@Override
				public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
					client.onReceivedHttpAuthRequest(view, handler, host, realm);
				}

				@Override
				public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						client.onReceivedHttpError(view, request, errorResponse);
					}
				}

				@Override
				public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
					client.onReceivedLoginRequest(view, realm, account, args);
				}

				@Override
				public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
					client.onReceivedSslError(view, handler, error);
				}

				@Override
				public void onScaleChanged(WebView view, float oldScale, float newScale) {
					client.onScaleChanged(view, oldScale, newScale);
				}
			});
		} else {
			this.webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					injectJavaScript();
				}
			});
		}
	}

	/**
	 * WebViewDataBus の JavaScript を インジェクト
	 */
	private void injectJavaScript() {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		String script = getInjectJavaScript();
		if (null == script) {
			Logger.e("Cannot get inject script.");
			return;
		}
		String encoded = Base64.encodeToString(script.getBytes(), Base64.NO_WRAP);
		Logger.d("inject: " + encoded);
		webView.loadUrl("javascript:(function() {" +
				"var script = document.createElement('script');" +
				"script.type = 'text/javascript';" +
				"script.innerHTML = window.atob('" + encoded + "');" +
				"document.getElementsByTagName('head').item(0).appendChild(script)" +
				"})()");
	}

	/**
	 * WebViewDataBus の JavaScript を取得（自分でインジェクトしたい場合に利用）
	 *
	 * @return WebViewDataBus の JavaScript
	 */
	public String getInjectJavaScript() {
		if (destroyed) {
			Logger.e("already destroyed");
			return null;
		}
		InputStream is = null;
		BufferedReader br = null;
		StringBuilder result = new StringBuilder(16384);
		try {
			is = context.getAssets().open("js/AndroidDataBus.js");
			br = new BufferedReader(new InputStreamReader(is));
			String str;
			while ((str = br.readLine()) != null) result.append(str).append("\n");
		} catch (IOException e) {
			Logger.printStackTrace(e);
		} finally {
			if (null != is) try {
				is.close();
			} catch (IOException e) {
				Logger.printStackTrace(e);
			}
			if (null != br) try {
				br.close();
			} catch (IOException e) {
				Logger.printStackTrace(e);
			}
		}
		return result.toString();
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void send(final JSONArray data) {
		if (destroyed) {
			Logger.e("already destroyed");
			return;
		}
		Logger.d("sending: " + data.toString());
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				webView.loadUrl(String.format(
						"javascript: window.AndroidDataBusNI.onSend(%s);",
						data.toString()
				));
			}
		});
	}

	@Override
	public void destroy() {
		super.destroy();
		if (addedJavaScriptInterface) {
			webView.removeJavascriptInterface(JAVASCRIPT_INTERFACE);
			addedJavaScriptInterface = false;
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
