# SoftLibYAML
Java YAML, CBOR Rebind extend

概要

java や JSONから SoftLibRebind経由で YAMLやCBORを生成する試み
一応出力できるかな、まだ途中.

## 構成

SoftLibRebind Java List/Map/Object が扱える
  - SoftLibJSON        JDK  8   用 JSON,  Java EE    JSON-P JSON-B 互換, 独自実装、JSON共通部分含む
  - SoftLibJakartaJSON JDK 11以降用 JSON,  Jakarta EE JSON Processing, Jakarta EE JSON Binding 互換
  - SoftLibYAML        YAML, CBOR など 仮分類出力用

## Maven

pom.xml のdependency は次のような感じで追加します。
必要な入出力ライブラリを個別に含めればSoftLibRebindも含まれます。

Java Module System JDK11以降用
```
<dependency>
  <groupId>net.siisise<groupId>
  <artifactId>softlib-yaml.module</artifactId>
  <version>0.0.1</version>
  <type>jar</type>
</dependency>
```
JDK 1.8用
```
<dependency>
  <groupId>net.siisise<groupId>
  <artifactId>softlib-yaml</artifactId>
  <version>0.0.1</version>
  <type>jar</type>
</dependency>
```
リリース版 とりあえず 0.0.1
次版 0.0.2-SNAPSHOT ぐらい。

## 予定

   YAML と CBOR を分ける

## LICENSE

 Apache 2.0
 okomeki または しいしせねっと


