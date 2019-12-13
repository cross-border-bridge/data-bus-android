# Change log

## Version 2.1.2
- WebViewDataBus内でContextをActivityにキャストしている箇所を削除

## Version 2.1.1
- nativeのWebViewDataBusから送信したテキスト内容が不正に変換される問題を __Android 4.4(KitKat) 以降__ でのみ対策
  - Android 4.3以前ではこの問題は解消されません
- テストプログラムが実機で動作できる形に修正
  - WebViewDataBusのJavaScriptのインジェクション方式を自動から手動に変更
  - __自動インジェクションは実機では正常に動作しない (現状回避策が無い)__
- 依存するgradleのバージョンを更新

## Version 2.1.0
- `DataBus` をスレッドセーフ化（破壊的変更）
  - `DataBus#handlers` を `private` に変更
  - `DataBus#received` メソッド (`protected`) を追加
    - DataBus実装はデータ受信時にこれを呼ぶ規約に変更
	- すべてのDataBus実装はこの規約に追従する必要があります

## Version 2.0.5
- `MemoryQueue` をスレッドセーフにする
- リファクタ

## Version 2.0.4
- `WebViewDataBus` を手動インジェクトする手段(optional)を追加
- `WebViewDataBus#getInjectJavaScript` の アクセス修飾子を `public` に変更 (手動インジェクト時に用いる)

## Version 2.0.3
- 依存する Gradle と Build Tools を Android Studio 2.3 対応バージョンに更新
- `DataBus#handlers` をスレッドセーフな形に修正

## Version 2.0.2
`MultiplexDataBus` で要素に `null` を含む `JSONArray` を送受信するとクラッシュする問題を修正。

## Version 2.0.1
サードパーティ製のDataBusを開発できるようにするため, `DataBus#handlers` のアクセス修飾子を `protected` に変更。

## Version 2.0.0
初版
