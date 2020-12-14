# <p align="center"><img src="title.png"/></p>
次のインタフェース及び実装を提供します。

|class|description|
|---|---|
|`DataBus`|Androidアプリ（Java）で利用できるDataBus基本クラス|
|`WebViewDataBus`|ネイティブコード(Java) と WebView(JavaScript) 間で利用できるDataBus|
|`MemoryQueue`|同一プロセス内での通信機構|
|`MemoryQueueDataBus`|`MemoryQueue` を用いたDataBus|
|`MultiplexDataBus`|DataBusを多重化|

## Example 
本リポジトリの [app](app) モジュールが, WebViewDataBusで通信をする簡単なサンプルになっています。
- Java: [MainActivity.java](app/src/main/java/jp/co/dwango/cbb/db/test/MainActivity.java)
- HTML: [index.html](app/src/main/assets/html/index.html), [script.js](app/src/main/assets/html/script.js)

## Setup 
### gradle
```
dependencies {
	compile 'jp.co.dwango.cbb:data-bus:2.1.3'
}
```

## Usage
WebViewDataBusの基礎的な使用方法を示します。

#### step 1: WebView + DataBus を準備 (Java)
- `WebView` で __Webコンテンツのloadを行う前__ に `WebViewDataBus` インスタンスを作成する必要があります
- `WebViewDataBus` は, Webコンテンツ側でDataBusを使用するためのJavaScriptコードをインジェクトします

```java
	WebView webView = (WebView) findViewById(R.id.web_view);
	webView.getSettings().setJavaScriptEnabled(true);
	WebViewDataBus dataBus = new WebViewDataBus(this, webView);
	webView.loadUrl(url);
```

> __注意点:__ 1つの `WebView` に対して作ることができる `WebViewDataBus` のインスタンスは1つだけです。
> 複数のDataBusを利用したい場合は, `MultiplexDataBus` を用いて多重化してください。

#### step 2: JavaScript側からsendされたデータをハンドリング (Java)
```java
	dataBus.addHandler(new DataBusHandler() {
		@Override
		public void onReceive(JSONArray data) {
			Log.d("MyApp", "received data from JavaScript: " + data);
		}
	}
```

> 追加したハンドラは `DataBus#removeHandler` または `DataBus#removeAllHandlers` で削除できます。

#### step 3: JavaScript側へデータをsend (Java)
`DataBus#send` で `JSONArray` 形式のデータを JavaScript側へ送信できます。
```java
	dataBus.send(new JSONArray().put("data"));
```

#### step 4: DataBusを準備 (JavaScript)
次のコードでJavaScript側でDataBusのインスタンスを生成できます。

```javascript
var dataBus = new CBB.WebViewDataBus();
```

#### step 5: Java側からsendされたデータをハンドリング (JavaScript)
`DataBus#addHandler` で function を追加することで, Java側がsendしたデータをハンドリングできます。
```javascript
    dataBus.addHandler(function() {
        var data = arguments.join(',');
        console.log("received data from native: " + data);
    });
```

#### step 6: Java側へデータをsend (JavaScript)
`DataBus#send` でデータをJava側へ送信できます。
```javascript
    dataBus.send(1, "arg2", {"arg3": 3});
```

#### step 7: 破棄
##### （JavaScript）
`DataBus#destroy` で破棄することができます。
```javascript
    dataBus.destroy();
```

##### （Java）
`DataBus#destroy` で破棄することができます。
```objective-c
    dataBus.destroy();
```

## License
- Source code, Documents: [MIT](LICENSE)
- Image files: [CC BY 2.1 JP](https://creativecommons.org/licenses/by/2.1/jp/)
