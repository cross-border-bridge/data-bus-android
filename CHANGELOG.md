# Change log

## Version 2.0.3
- 依存する Gradle と Build Tools を Android Studio 2.3 対応バージョンに更新
- `DataBus#handlers` をスレッドセーフな形に修正

## Version 2.0.2
`MultiplexDataBus` で要素に `null` を含む `JSONArray` を送受信するとクラッシュする問題を修正。

## Version 2.0.1
サードパーティ製のDataBusを開発できるようにするため, `DataBus#handlers` のアクセス修飾子を `protected` に変更。

## Version 2.0.0
初版
