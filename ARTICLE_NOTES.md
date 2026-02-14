# Android で iOS Liquid Glass を再現する試み

## TL;DR

iOS 26 の `.glassEffect(.regular)` が内部で行っている **多層レンダリングパイプライン** を分析し、Android の AGSL (Android Graphics Shading Language) + Haze ライブラリで段階的に再現した記録。Galaxy S25 Ultra 実機で検証。

---

## 目次（記事分割案）

1. **Part 1: iOS Liquid Glass の内部構造を解剖する** — 10層パイプラインの分析、CABackdropLayer、CASDFLayer
2. **Part 2: Android で Backdrop Blur を正しく実装する** — Element Blur vs Backdrop Blur、Haze ライブラリ
3. **Part 3: カスタム AGSL シェーダーで屈折・Fresnel・Specular を実装する** — SDF、Schlick 近似、色収差
4. **Part 4: ジャイロスコープ連動と仕上げ** — TYPE_ROTATION_VECTOR、パラメータ調整、残課題

---

## Part 1: iOS Liquid Glass の内部構造

### 背景・モチベーション

iOS 26 (WWDC25) で導入された Liquid Glass は、単なる「すりガラス風ブラー」ではない。Apple は WWDC25 Session 219 "Meet Liquid Glass" で、Liquid Glass の核心を **Lensing（レンズ効果）** と定義している。従来のマテリアルが光を散乱させていたのに対し、Liquid Glass はリアルタイムで光を曲げ、成形し、集中させる [^wwdc219]。

屈折・色収差・フレネル反射・スペキュラハイライト・ノイズテクスチャ・ジャイロ連動ライティングなど、物理ベースの複数エフェクトを重ねた多層パイプラインで構成されている。

Android でこれに近い質感を出すには何が必要かを調べ、Lab アプリを作りながら段階的に実装した。

[^wwdc219]: Apple, "Meet Liquid Glass", WWDC25 Session 219

### iOS Liquid Glass の公式 API

SwiftUI では `.glassEffect()` modifier で適用する [^apple-glasseffect]：

```swift
nonisolated func glassEffect(
    _ glass: Glass = .regular,
    in shape: some Shape = DefaultGlassEffectShape()
) -> some View
```

3つのバリアントがある：

| バリアント | 透明度 | 用途 |
|---|---|---|
| `.regular` | 中程度 | ツールバー、ボタン、ナビゲーション |
| `.clear` | 高い | メディア上のフローティングコントロール |
| `.identity` | なし | 無効状態、条件的無効化 |

UIKit では `UIGlassEffect` + `UIVisualEffectView` で実現する [^apple-uiglasseffect]：

```swift
let glassEffect = UIGlassEffect()
glassEffect.isInteractive = true
let visualEffectView = UIVisualEffectView(effect: glassEffect)
```

複数のガラス要素をマージする `GlassEffectContainer` も提供されている [^apple-glasscontainer]。`spacing` パラメータがモーフィングの閾値距離を制御し、近接するガラス要素が「ぬるっと」融合する。

[^apple-glasseffect]: Apple Developer Documentation, [glassEffect(_:in:)](https://developer.apple.com/documentation/swiftui/view/glasseffect(_:in:))
[^apple-uiglasseffect]: Apple Developer Documentation, [UIGlassEffect](https://developer.apple.com/documentation/uikit/uiglasseffect)
[^apple-glasscontainer]: Apple Developer Documentation, [GlassEffectContainer](https://developer.apple.com/documentation/swiftui/glasseffectcontainer)

### 内部レンダリングパイプラインの逆解析

iOS の Liquid Glass がどのように描画されているかは、いくつかの逆解析プロジェクトから判明している。

#### Core Animation レイヤー階層

AlexStrNik の ShatteredGlass [^shatteredglass] による macOS 26 の逆解析で、以下のレイヤー構造が判明した：

```
Root View
├── CABackdropLayer (ガラス歪みを適用)
│   └── CASDFLayer (@0) - テクスチャ入力を提供
├── CASDFLayer + Highlight (@1) - エッジ照明（角度付き）
└── CASDFLayer + Highlight (@2) - 反対方向のエッジ照明
```

#### 発見されたプライベートクラス群

| クラス名 | 役割 |
|---|---|
| `CABackdropLayer` | メインの歪みレンダリング。WindowServer 経由で背景キャプチャ |
| `CASDFLayer` | Signed Distance Field レイヤー（形状定義） |
| `CASDFElementLayer` | SDF 形状定義プリミティブ |
| `SDFPortalLayer` | `CAPortalLayer` の SDF バリアント |
| `_UILiquidLensView` | iOS のレンズエフェクト実装 |

[^shatteredglass]: AlexStrNik, [ShatteredGlass](https://github.com/AlexStrNik/ShatteredGlass) — macOS 26 Liquid Glass deconstruction

#### Core Animation フィルター

| フィルター名 | 機能 |
|---|---|
| `glassBackground` | 屈折、ブラー、バイブランシー、トーンマッピングを適用 |
| `CASDFOutputEffect` | 形状ジオメトリを SDF テクスチャに変換 |
| `CASDFGlassHighlightEffect` | 方向ベースのエッジ照明シミュレーション |
| `vibrantColorMatrix` | ハイライトトーン用カスタム 4x5 カラー変換マトリックス |

#### CABackdropLayer の動作原理

`CABackdropLayer` は WindowServer と直接連携して、ビューの背後のピクセルデータをキャプチャする [^vaidyam-cabackdrop]。通常の Core Animation レイヤーとは異なり、`windowServerAware = true` の設定が必要で、WindowServer 内のレンダリングパスに統合される。

```swift
let blur = CAFilter(type: kCAFilterGaussianBlur)!
blur.setValue(30.0, forKey: "inputRadius")
blur.setValue(true, forKey: "inputNormalizeEdges")

let saturate = CAFilter(type: kCAFilterColorSaturate)!
saturate.setValue(1.8, forKey: "inputAmount")

layer.filters = [blur, saturate]
```

`scale` プロパティがサンプリング解像度を制御し、0.25 が性能と品質のバランス点とされている。

[^vaidyam-cabackdrop]: Aditya Vaidyam, [CAPluginLayer & CABackdropLayer](https://aditya.vaidyam.me/blog/2018/02/17/)

### レンダリングパイプラインの全体像

各ガラスエフェクトは **3つのオフスクリーンテクスチャ** を必要とする [^juniperphoton]：

```
[ユーザーのコンテンツ]
        ↓
[CABackdropLayer] ← WindowServer がビュー背後のピクセルをキャプチャ
        ↓
[オフスクリーンテクスチャ #1: SDF 計算]
  CASDFLayer → CASDFOutputEffect → SDF テクスチャ生成
        ↓
[オフスクリーンテクスチャ #2: Glass Background]
  glassBackground フィルター適用:
  - ガウシアンブラー（scale=0.25 でダウンサンプリング）
  - 屈折（SDF の法線マップから displacement 計算）
  - 色収差（RGB チャネル別オフセット）
  - フレネル反射（エッジで強度増加）
  - バイブランシー & トーンマッピング
        ↓
[オフスクリーンテクスチャ #3: ハイライト]
  CASDFGlassHighlightEffect × 2（対角方向）
  + vibrantColorMatrix（4x5 カラー変換）
        ↓
[最終合成] → 画面に描画
```

`GlassEffectContainer` を使用すると複数のガラスエフェクトが1つの `CABackdropLayer` に統合され、オフスクリーンテクスチャ数を削減できる。

[^juniperphoton]: JuniperPhoton, [Adopting Liquid Glass: Experiences and Pitfalls](https://juniperphoton.substack.com/p/adopting-liquid-glass-experiences)

### LiquidGlassKit による5つのコアエフェクト

DnV1eX の LiquidGlassKit [^liquidglasskit] は iOS 13-18 へのバックポート + iOS 26+ での拡張カスタマイズを提供するオープンソースプロジェクト (Swift 79.5%, Metal 19.8%)。Metal シェーダーの解析から5つのコアエフェクトが特定できる：

| エフェクト | 説明 |
|---|---|
| **Refraction** | 設定可能な屈折率でガラスを通る光の屈曲をシミュレート |
| **Chromatic Dispersion** | エッジでのプリズム的な色分離。R/B を異なる方向にオフセット、G はそのまま |
| **Fresnel Reflections** | 視角で強度が変化するエッジ照明 |
| **Glare Highlights** | 表面法線に反応する方向性スペキュラーストリーク |
| **Shape Merging** | カスタマイズ可能なコーナー丸めを持つ複数矩形の Smooth Union |

背景キャプチャには2つの方式がある：

| 方式 | iOS バージョン | 特徴 |
|---|---|---|
| `CABackdropLayer` + ZeroCopyBridge (IOSurface) | iOS 13-26.1 | 高性能、ゼロコピー、プライベート API |
| Root View Rendering | iOS 26.2+ | パブリック API のみ、CPU 負荷高 |

ZeroCopyBridge は IOSurface を介した Metal テクスチャへのゼロコピーブリッジで、メモリコピーを回避して高フレームレートを維持する。

[^liquidglasskit]: DnV1eX, [LiquidGlassKit](https://github.com/DnV1eX/LiquidGlassKit)

### 10層パイプラインへの分解

上記の分析を総合すると、iOS Liquid Glass は以下の10層に分解できる：

| Layer | 効果 | 概要 |
|-------|------|------|
| 1 | **Backdrop blur** | `CABackdropLayer` + ガウシアンブラー |
| 2 | **Saturation boost** | `kCAFilterColorSaturate` で彩度 +10〜20% |
| 3 | **Refraction** | SDF 法線ベースで背景をレンズ状に歪ませる |
| 4 | **Chromatic dispersion** | R/G/B チャンネル個別屈折率サンプリング |
| 5 | **Tint overlay** | 背景色に適応した半透明ティント |
| 6 | **Contrast adjustment** | 微調整（+5% 程度） |
| 7 | **Fresnel edge glow** | Schlick 近似。ジャイロ連動 |
| 8 | **Specular highlights** | `CASDFGlassHighlightEffect` × 2 方向 |
| 9 | **Dynamic lighting** | デバイスの傾き（ジャイロ）に応じて光方向が変化 |
| 10 | **Noise texture** | 微細なサーフェステクスチャ |

（スコープ外: Adaptive shadow — コンテンツ形状に追従するドロップシャドウ）

---

## Part 2: Android での Backdrop Blur 実装

### Lab アプリの構成

検証用に **3パターン × 3ブラーモード** の比較 Lab を構築：

**パターン:**
- NavBar + BottomBar（画像グリッドの上下にバーが浮く）
- Floating Card（背景画像の上にカードが浮く）
- Full Screen Overlay（フルスクリーンブラー + モーダル）

**ブラーモード:**
- **Haze** — Haze ライブラリ単体（backdrop blur のみ）
- **Cloudy** — Cloudy ライブラリ単体（element blur のみ）
- **Liquid Glass** — Haze backdrop blur + カスタム AGSL シェーダー

### 課題: Element Blur vs Backdrop Blur

最初の実装では Cloudy ライブラリの `.cloudy()` でブラーを適用していた。これは **element blur**（要素自体をぼかす）であり、実際には静的な画像コピーをぼかしているだけ。

```
Before:  AsyncImage(static copy) → .cloudy(blur) → .liquidGlass(refraction)
```

iOS の `CABackdropLayer` は **backdrop blur**（背後のコンテンツをリアルタイムにぼかす）。スクロールしても、コンテンツが動いても、バーの裏で背景がぼけ続ける。

この違いは見た目に大きく影響する。Element blur は「ぼかした画像を貼り付けた」静的な見た目になるが、backdrop blur はリアルタイムにコンテンツが透けて見え、スクロールに追従する。

### 解決: Haze ライブラリへの置換

#### Haze とは

[Haze](https://github.com/chrisbanes/haze) [^haze-github] は Chris Banes（元 Google Android DevRel、Tivi・Accompanist の作者）が開発した Compose Multiplatform 向け backdrop blur ライブラリ。

- **ライセンス**: Apache License 2.0
- **使用バージョン**: 1.7.1（最新は 1.7.2, 2026-02-10 リリース）
- **対応プラットフォーム**: Android, iOS, macOS, Desktop (JVM), WASM

[^haze-github]: Chris Banes, [chrisbanes/haze](https://github.com/chrisbanes/haze) — Background blurring for Compose Multiplatform

#### Haze の内部アーキテクチャ

Haze 1.0 のブログ記事 [^haze-1.0] と DeepWiki の分析 [^haze-deepwiki] から、内部アーキテクチャが判明：

**Source-Effect パターン:**
1. `Modifier.hazeSource(state)` がコンテンツを `GraphicsLayer` にキャプチャ
2. キャプチャされたコンテンツが `HazeArea` として共有 `HazeState` に登録
3. `Modifier.hazeEffect(state)` が登録されたエリアを読み取り、プラットフォーム固有のブラーを適用

**プラットフォーム別ブラー実装:**

| Android API | 実装 | コスト |
|---|---|---|
| SDK 31- | Scrim fallback（半透明オーバーレイ） | 最小 |
| SDK 31 | RenderScript [^haze-1.6] | 中 |
| SDK 32 | 複数スタック `GraphicsLayer` | ~2x フレーム時間 |
| SDK 33+ | AGSL RuntimeShader | ~1.25x フレーム時間 |

iOS/Desktop/WASM は Skia RuntimeEffect を使用。

**アーキテクチャの進化:**
- Pre-1.0: Android 固有の `RenderNode` を直接使用。親の `haze` modifier がすべてのブラー領域を描画（"smoke and mirrors"）
- 1.0: Compose 1.7 の `GraphicsLayer` API に移行し、単一のクロスプラットフォーム実装を実現

[^haze-1.0]: Chris Banes, [Haze 1.0](https://chrisbanes.me/posts/haze-1.0/)
[^haze-deepwiki]: [chrisbanes/haze DeepWiki](https://deepwiki.com/chrisbanes/haze)
[^haze-1.6]: Haze 1.6 release — RenderScript support for all Android versions

#### Haze の使用方法

```kotlin
val hazeState = rememberHazeState()
val hazeStyle = HazeStyle(
    blurRadius = 24.dp,
    tints = listOf(HazeTint(Color.White.copy(alpha = 0.08f))),
)

// Source: 実際のコンテンツ
ImageGrid(modifier = Modifier.hazeSource(state = hazeState))

// Effect: backdrop blur
Box(modifier = Modifier.hazeEffect(state = hazeState, style = hazeStyle))
```

LiquidGlass モードのみ Haze + liquidGlass の2層構成に変更：

```
After:  hazeSource(実コンテンツ) → hazeEffect(backdrop blur) → enhancedLiquidGlass(refraction)
```

### Cloudy ライブラリについて

[Cloudy](https://github.com/skydoves/Cloudy) [^cloudy-github] は skydoves (Jaewoong Eum) が開発した KMP ブラー + ガラスレンズライブラリ。

- **ライセンス**: Apache License 2.0
- **使用バージョン**: 0.5.0
- **2つのコア機能**:
  - `Modifier.cloudy(radius)` — ブラーエフェクト (Android 31+: RenderEffect GPU, 30-: Native C++ CPU with NEON/SIMD)
  - `Modifier.liquidGlass()` — ガラスレンズエフェクト (Android 33+: AGSL RuntimeShader)

Cloudy の `liquidGlass()` は FletchMcKee/liquid にインスパイアされており、SDF ベースの屈折 + 色収差を AGSL シェーダーで実装している。本プロジェクトではこのシェーダーをフォークして拡張した。

[^cloudy-github]: skydoves (Jaewoong Eum), [skydoves/Cloudy](https://github.com/skydoves/Cloudy) — Jetpack Compose blur & liquid glass

### Blur 半径の換算

Haze と Cloudy でブラー半径の単位が異なるため、等価な見た目になるよう換算関数を用意した：

```kotlin
// Haze:  sigma = blurRadiusDp × density
// Cloudy: sigma = radiusPx / 2
// → radiusPx = 2 × blurRadiusDp × density
fun hazeEquivalentCloudyRadius(hazeBlurRadiusDp: Float): Int {
    val density = LocalDensity.current.density
    return (2f * hazeBlurRadiusDp * density).roundToInt()
}
```

### ガウシアンブラーの背景知識

1D ガウス関数 [^rastergrid-blur]：

```
G(x) = (1 / sqrt(2 * pi * sigma^2)) * exp(-x^2 / (2 * sigma^2))
```

2D ガウスは **分離可能** (separable) なため、水平パス + 垂直パスの2パスに分解でき、計算量が `O(N^2)` から `O(2N)` に削減される。

モバイル GPU では Dual Kawase Blur [^dual-kawase] が効率的で、Haze も内部でこの手法を使用している。ダウンサンプル → ブラー → アップサンプルのパイプラインで、ブラー半径を倍にしても追加は2パスのみ（対数スケーリング）。

[^rastergrid-blur]: RasterGrid, [Efficient Gaussian Blur with Linear Sampling](https://www.rastergrid.com/blog/2010/09/efficient-gaussian-blur-with-linear-sampling/)
[^dual-kawase]: frost.kiwi, [Video Game Blurs and How the Best One Works (Dual Kawase)](https://blog.frost.kiwi/dual-kawase/)

### iOS 寄りパラメータ調整

`liquidGlass()` のデフォルトパラメータを iOS に近づけた：

| Parameter | Cloudy Default | iOS-tuned | 理由 |
|-----------|---------------|-----------|------|
| refraction | 0.25 | 0.25 | そのまま |
| curve | 0.25 | 0.20 | iOS は控えめ |
| dispersion | 0.0 | 0.15 | iOS は色収差あり |
| saturation | 1.0 | 1.15 | iOS +10-20% |
| contrast | 1.0 | 1.05 | 微調整 |
| tint | Transparent | White(0.08) | iOS はより微妙な白ティント |
| edge | 0.2 | 0.25 | Fresnel 幅拡大 |

### パラメータ操作 UI

各パラメータを実機上でリアルタイム調整できるスライダーパネルを実装。Liquid Glass モード時のみ展開可能。Reset ボタンでデフォルト値に戻せる。

---

## Part 3: カスタム AGSL シェーダー

### AGSL (Android Graphics Shading Language) とは

AGSL は Android 13 (API 33) で導入された GPU シェーダー言語 [^agsl-docs]。内部的には Skia Shading Language (SkSL) をベースとしており、Chet Haase の解説 [^agsl-chet] によれば「SkSL を Android API 上で使いやすくリネームしたもの」。

AGSL は GLSL ES 1.0 の構文をベースとしているが、いくつかの重要な違いがある [^agsl-vs-glsl]：

| 項目 | GLSL | AGSL |
|------|------|------|
| Y軸原点 | 左下 | 左上（Canvas 座標系） |
| エントリポイント | `void main()` + `gl_FragColor` | `vec4 main(vec2 fragCoord)` で色を返す |
| 色空間 | 手動管理 | `toLinearSrgb()` / `fromLinearSrgb()` 組み込み |
| プリプロセッサ | `#define` 使用可 | なし。`const` 変数を使用 |
| 型名 | `vec2` / `mat3` | `float2` / `float3x3` も使用可（+ `half` / `short`） |

**対 Metal との比較:**
- AGSL は C スタイル（GLSL ES 1.0 → SkSL 派生）
- Metal Shading Language (MSL) は C++14 ベース（clang/LLVM 実装）
- 両者ともプラットフォーム固有だが、GPU シェーダーとして同等の表現力を持つ
- AGSL は Skia 経由でコンパイル、MSL は Apple の Metal コンパイラ経由

**RuntimeShader API:**

```kotlin
val shader = RuntimeShader(SHADER_SOURCE_STRING)
shader.setFloatUniform("resolution", width.toFloat(), height.toFloat())
shader.setFloatUniform("lightDirection", lightDir.x, lightDir.y)

// View に適用
view.setRenderEffect(RenderEffect.createRuntimeShaderEffect(shader, "content"))

// Compose Modifier で適用
Modifier.graphicsLayer {
    renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")
        .asComposeRenderEffect()
}
```

[^agsl-docs]: Android Developers, [Android Graphics Shading Language (AGSL)](https://developer.android.com/develop/ui/views/graphics/agsl)
[^agsl-chet]: Chet Haase, [AGSL: Made in the Shade(r)](https://medium.com/androiddevelopers/agsl-made-in-the-shade-r-7d06d14fe02a)
[^agsl-vs-glsl]: Android Developers, [Differences between AGSL and GLSL](https://developer.android.com/develop/ui/views/graphics/agsl/agsl-vs-glsl)

### SDF (Signed Distance Field) による形状定義

#### SDF の基本

SDF はある点から形状境界までの最短距離を返す関数 [^iq-sdf2d]：
- **負の値**: 点は形状の内部
- **ゼロ**: 点は境界上
- **正の値**: 点は形状の外部

SDF の勾配 (gradient) は iso-contour に垂直で、そのまま **サーフェス法線** として使える [^iq-normals]。これが屈折やフレネル効果の計算に不可欠。

[^iq-sdf2d]: Inigo Quilez, [2D distance functions](https://iquilezles.org/articles/distfunctions2d/)
[^iq-normals]: Inigo Quilez, [Normals for an SDF](https://iquilezles.org/articles/normalsSDF/)

#### Rounded Rectangle SDF

Inigo Quilez の per-corner radius 版 [^iq-sdf2d]：

```glsl
float sdRoundedBox(in vec2 p, in vec2 b, in vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x  = (p.y > 0.0) ? r.x  : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}
```

パラメータ:
- `p` = サンプル点（原点中心）
- `b` = 矩形の半分サイズ `(width/2, height/2)`
- `r` = `vec4(topRight, bottomRight, topLeft, bottomLeft)` 各角の丸み半径

Cloudy のシェーダーもこれと同等の `roundedRectDistance()` を使用している。

#### Distance + Gradient（法線付き SDF）

Inigo Quilez の Distance+Gradient 版 [^iq-distgrad]：

```glsl
vec3 sdgBox(in vec2 p, in vec2 b) {
    vec2 w = abs(p) - b;
    vec2 s = vec2(p.x < 0.0 ? -1.0 : 1.0, p.y < 0.0 ? -1.0 : 1.0);
    float g = max(w.x, w.y);
    vec2  q = max(w, 0.0);
    float l = length(q);
    return vec3(
        (g > 0.0) ? l : g,                                           // distance
        s * ((g > 0.0) ? q/l : ((w.x > w.y) ? vec2(1,0) : vec2(0,1))) // gradient
    );
}
```

`vec3(distance, gradient.x, gradient.y)` を返す。gradient がそのまま法線として使える。

丸め操作は距離値の定数減算のみ：
```glsl
vec3 sdgRound(in vec3 dis_gra, in float r) {
    return vec3(dis_gra.x - r, dis_gra.yz);  // 勾配は変わらない
}
```

[^iq-distgrad]: Inigo Quilez, [Distance+Gradient 2D SDF](https://iquilezles.org/articles/distgradfunctions2d/)

#### Squircle SDF と iOS の Continuous Corner

iOS は `cornerCurve = .continuous` (iOS 13+) で、標準の rounded rect とは異なる **continuous corner** を使用している。Figma の "Desperately Seeking Squircles" [^figma-squircles] によると：

- Superellipse (`|x|^n + |y|^n = r^n`, n=5) は一見近いが、「小さいが系統的な不一致」がある
- Apple の実際の形状は **Bezier curve の patchwork** で構成されている
- 各角 = 円弧 + 2つの対称な cubic Bezier curve
- "smoothing" パラメータが Bezier 部分の比率を制御

Inigo Quilez は Shadertoy [^iq-squircle] で Squircle SDF を公開しているが、「**真の SDF ではない** — x=0, y=0 軸上または原点から十分遠い場合のみ正確」と注記している。実用上は `sdRoundedBox` の使用を推奨。

現状のプロジェクトでは通常の rounded rect SDF を使用しており、squircle SDF への置換は残課題。

Raph Levien の "Blurred Rounded Rectangles" [^raph-blurred] は、ブラー済み rounded rect を解析的に計算する手法を提案。ブラー半径が大きくなると形状が squircle に近づくという洞察が興味深い（結合半径 = `sqrt(r_corner^2 + 1.25 * r_blur^2)`）。

[^figma-squircles]: Daniel Furse (Figma), [Desperately Seeking Squircles](https://www.figma.com/blog/desperately-seeking-squircles/)
[^iq-squircle]: Inigo Quilez, [Squircle SDF (Shadertoy)](https://www.shadertoy.com/view/fsccz4)
[^raph-blurred]: Raph Levien, [Blurred Rounded Rectangles](https://raphlinus.github.io/graphics/2020/04/21/blurred-rounded-rects.html)

### Cloudy シェーダーのフォーク

Cloudy の AGSL シェーダーソース (`LiquidGlassShaderSource.AGSL`) は Apache 2.0 ライセンス。これをフォークして拡張した。

#### シェーダーパイプライン（オリジナル Cloudy）

1. **SDF 評価** — `roundedRectDistance()` でレンズ境界までの符号付き距離を計算
2. **法線計算** — `calculateSurfaceGradient()` で SDF から外向き法線を導出
3. **屈折** — 法線方向にサンプリング座標をオフセット。球面曲率モデル: `bendAmount = 1 - sqrt(1 - sphericalFactor^2)`
4. **色収差** — R/G/B を `normalizedPos^3` に比例した異なるオフセットでサンプリング
5. **アンチエイリアシング** — `smoothstep` でレンズ境界をブレンド

#### 追加した uniform

```glsl
// Specular
uniform float specularAmplitude;   // default 1.5
uniform float specularExponent;    // default 5.0

// Fresnel
uniform float fresnelWidth;        // default 6.0
uniform float fresnelBlur;         // default 5.5

// Noise
uniform float noiseIntensity;      // default 0.02

// Light direction (Phase 3 でジャイロ連動)
uniform float2 lightDirection;

// 各効果の ON/OFF フラグ
uniform float enableSpecular;
uniform float enableNoise;
uniform float enableFresnel;
```

### Specular Highlights の実装

iOS の specular は `CASDFGlassHighlightEffect` × 2方向で実現されている。対角方向のグラデーション状ハイライトを模倣するため、レンズ内のローカル座標と光方向の内積で対角成分を計算し、`pow()` でピークを生成した：

```glsl
float2 uv = localPos / halfExtent;
float diag = dot(uv, normalize(lightDirection));
float specBase = clamp(diag * 0.5 + 0.5, 0.0, 1.0);
float specular = pow(specBase, specularExponent) * specularAmplitude;
float interiorMask = clamp(-dist / max(min(halfExtent.x, halfExtent.y) * 0.1, 1.0), 0.0, 1.0);
sampledColor.rgb += half3(specular * interiorMask * 0.15);
```

### Fresnel Edge Glow の実装

#### 物理背景: Fresnel 方程式と Schlick 近似

Fresnel 方程式 (1821-1823) は、異なる屈折率を持つ2つの材質の境界で光がどのように反射・透過するかを記述する [^fresnel-wiki]。

垂直入射 (θ = 0) での反射率：
```
R0 = ((n1 - n2) / (n1 + n2))^2
```

Christophe Schlick が 1994 年に提案した近似式 [^schlick-1994]：
```
R(θ) = R0 + (1 - R0)(1 - cos θ)^5
```

完全な Fresnel 方程式に対する平均誤差は 1% 未満で、リアルタイムレンダリングに広く使われている [^learnopengl-pbr]。GLSL/AGSL での典型的な実装：

```glsl
vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}
```

[^fresnel-wiki]: Wikipedia, [Fresnel equations](https://en.wikipedia.org/wiki/Fresnel_equations)
[^schlick-1994]: Christophe Schlick, "An Inexpensive BRDF Model for Physically-based Rendering", Computer Graphics Forum, Vol. 13, No. 3, pp. 233-246, 1994. [DOI:10.1111/1467-8659.1330233](https://onlinelibrary.wiley.com/doi/10.1111/1467-8659.1330233)
[^learnopengl-pbr]: [LearnOpenGL - PBR Theory](https://learnopengl.com/PBR/Theory)

#### 実装

元の Cloudy は固定方向 `(-1, -1)` の単純な rim lighting。iOS は Schlick 近似で、エッジで強く光る物理ベースの挙動。`pow` の指数を5ではなく3にして、より柔らかい見た目にしている：

```glsl
float NdotL = max(dot(surfaceDir, normalize(lightDirection)), 0.0);
float fresnel = pow(1.0 - NdotL, 3.0);
float edgeMask = smoothstep(-fresnelWidth, -fresnelBlur, dist);
sampledColor.rgb += half3(fresnel * edgeMask * edge);
```

`lightDirection` を uniform 化したことで、Phase 3 のジャイロ連動への布石にもなっている。

### Chromatic Dispersion（色収差）の背景知識

実世界のレンズでは、材質の屈折率が波長によって異なる（分散）。短い波長（青, ~450nm）ほど強く屈折し、長い波長（赤, ~650nm）ほど弱い [^dispersion-wiki]。

Cauchy の式 (1830) [^cauchy-wiki]：
```
n(λ) = A + B/λ^2 + C/λ^4
```

シェーダーでは RGB チャネル別に異なるオフセットでサンプリングすることで近似する [^heckel-dispersion]：

```glsl
float R = texture(uTexture, uv + refractVecR.xy * strength).r;
float G = texture(uTexture, uv + refractVecG.xy * strength).g;
float B = texture(uTexture, uv + refractVecB.xy * strength).b;
```

Cloudy では `normalizedPos^3` に比例してオフセットを計算し、エッジに向かうほど色収差が強くなる。LiquidGlassKit の Metal シェーダーでも同様のアプローチで、R と B を逆方向にオフセットし、G はそのままにしている。

[^dispersion-wiki]: Wikipedia, [Dispersion (optics)](https://en.wikipedia.org/wiki/Dispersion_(optics))
[^cauchy-wiki]: Wikipedia, [Cauchy's equation](https://en.wikipedia.org/wiki/Cauchy%27s_equation)
[^heckel-dispersion]: Maxime Heckel, [Refraction, Dispersion, and Other Shader Light Effects](https://blog.maximeheckel.com/posts/refraction-dispersion-and-other-shader-light-effects/)

### Noise Texture の実装

AGSL ではテクスチャサンプラーを使わずに、hash ベースの擬似乱数でノイズを生成する：

```glsl
float hash(float2 p) {
    float3 p3 = fract(float3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

// 適用
float n = hash(fragCoord) * 2.0 - 1.0;  // [-1, 1]
sampledColor.rgb += half3(n * noiseIntensity);
```

intensity 0.02 で、肉眼でギリギリ見えるレベルの微細なテクスチャ感が出る。Haze も `noise` パラメータでサーフェステクスチャを提供しているが、こちらは AGSL シェーダー内で独立に実装。

### Modifier.enhancedLiquidGlass

`RuntimeShader` + `RenderEffect.createRuntimeShaderEffect()` で AGSL シェーダーを適用する Compose Modifier を実装。API 33+ (Android 13+) 必須。Galaxy S25 Ultra は API 35 なので問題なし。

```kotlin
@Composable
fun Modifier.enhancedLiquidGlass(
    lensCenter: Offset,
    lensSize: Size,
    cornerRadius: Float = 0f,
    params: LiquidGlassParams = LiquidGlassParams.Default,
    lightDirection: Offset = Offset(-0.707f, -0.707f),
    enabled: Boolean = true,
): Modifier
```

### Lab UI の拡張

パラメータパネルに各効果の **ON/OFF チェックボックス** を追加。各層の寄与を個別に確認・比較できるようにした。これにより、Specular だけ、Fresnel だけ、Noise だけの効果を視覚的に確認できる。

---

## Part 4: ジャイロスコープ連動と仕上げ

### ジャイロスコープ連動の実装

iOS の Dynamic Lighting は、デバイスの傾きに応じてスペキュラハイライトとフレネル反射の光方向を変化させる。Android では `TYPE_ROTATION_VECTOR` センサーを使用。

```kotlin
@Composable
fun rememberDeviceMotion(smoothingFactor: Float = 0.6f): State<DeviceMotionData>

data class DeviceMotionData(
    val pitch: Float = 0f, // 前後傾き (radians)
    val roll: Float = 0f,  // 左右傾き (radians)
)
```

`SensorManager.getRotationMatrixFromVector()` + `SensorManager.getOrientation()` で回転行列から pitch/roll を取得。指数移動平均 (EMA) でジッターを除去：

```kotlin
// Exponential moving average
smoothedPitch = smoothingFactor * smoothedPitch + (1f - smoothingFactor) * rawPitch
smoothedRoll = smoothingFactor * smoothedRoll + (1f - smoothingFactor) * rawRoll
```

### lightDirection への変換

```kotlin
val motion by rememberDeviceMotion()
val lightDirection = Offset(
    (motion.roll * 1.5f).coerceIn(-1f, 1f),
    (motion.pitch * 1.5f).coerceIn(-1f, 1f),
)
```

この `lightDirection` が AGSL シェーダーの `uniform float2 lightDirection` にフレーム毎に渡され、specular highlight と Fresnel rim の光方向がリアルタイムに変化する。

### 調整ポイント

- **係数 1.5**: pitch/roll はラジアン (±π/2 ≈ ±1.57) なので、1.5 倍すると full range で ±1 をカバー
- **スムージング 0.6**: 高すぎると反応が鈍い、低すぎるとジッター。0.6 が体感的にちょうど良い
  - 最初は 0.85 にしていたが反応が鈍すぎた
- **オフセット方式の変更**: 最初は `-1f + motion.roll * 0.5f` としていたが、光源が常に左上に偏るため、`motion.roll * 1.5f` にゼロ中心化
- **デバッグ表示**: パネルに pitch/roll/lightDirection のリアルタイム値を表示して調整を容易に

---

## 技術スタック

| 要素 | 技術 |
|------|------|
| UI | Jetpack Compose |
| Backdrop blur | [Haze](https://github.com/chrisbanes/haze) 1.7.1 |
| Base refraction | [Cloudy](https://github.com/skydoves/cloudy) 0.5.0 (`liquidGlass()`) |
| Enhanced effects | カスタム AGSL シェーダー (Cloudy フォーク) |
| Image loading | Coil 3 |
| Sensor | Android SensorManager (`TYPE_ROTATION_VECTOR`) |
| 検証端末 | Galaxy S25 Ultra (API 35) |

---

## ファイル構成

```
androidApp/src/main/kotlin/com/example/liquidglasslab/
├── MainActivity.kt                  -- LabScreen + パラメータ state + gyro
├── LiquidGlassParams.kt            -- 全パラメータの data class
├── theme/Theme.kt
├── components/
│   └── ParameterPanel.kt           -- スライダー + ON/OFF パネル
├── shader/
│   ├── LiquidGlassShader.kt        -- カスタム AGSL シェーダーソース
│   └── EnhancedLiquidGlass.kt      -- Modifier.enhancedLiquidGlass
├── sensor/
│   └── DeviceMotion.kt             -- ジャイロスコープ composable
└── patterns/
    ├── BlurMode.kt                  -- enum + 換算関数
    ├── AppBarBottomBarPattern.kt    -- Haze + enhancedLiquidGlass
    ├── FloatingCardPattern.kt       -- 同上
    └── FullScreenOverlayPattern.kt  -- 同上
```

---

## 現状の再現度まとめ

| iOS 効果層 | 再現 | 備考 |
|-----------|------|------|
| Backdrop blur | ✅ | Haze で実現。スクロール追従する本物の backdrop blur |
| Saturation boost | ✅ | AGSL uniform で調整可能 |
| Refraction | ✅ | Cloudy 由来の SDF + 法線ベース屈折 |
| Chromatic dispersion | ✅ | RGB 個別サンプリング |
| Tint overlay | ⚠️ | 固定色。iOS は背景適応（未実装） |
| Contrast | ✅ | AGSL uniform |
| Fresnel edge glow | ✅ | Schlick 近似 + ジャイロ連動 |
| Specular highlights | ✅ | 対角グラデーション。iOS はもっと複雑 |
| Dynamic lighting | ✅ | ジャイロ連動 |
| Noise texture | ✅ | hash ベース擬似乱数 |
| Adaptive shadow | ❌ | スコープ外 |

---

## 残りの課題・改善余地

### 高インパクト

1. **Blur + Refraction の統合パイプライン化** — 現状 Haze と AGSL が別レイヤー。理想は blur 結果を refraction が直接サンプリングする1パス処理
2. **背景適応ティント** — blur 結果の平均輝度から tint カラーを動的に決定
3. **Squircle SDF** — iOS は continuous corner (squircle)。現状は普通の rounded rect

### 中インパクト

4. **Specular の複数 lobe 化** — GGX / Blinn-Phong ベースに
5. **Inner shadow** — ガラスの厚み表現
6. **マルチレイヤー blur** — 近距離・遠距離で異なるブラー半径を合成

### 低インパクト

7. Adaptive shadow
8. 画面輝度連動
9. ブラー半径のコンテンツ距離適応

---

## 学び・所感

- **Element blur ≠ Backdrop blur**: Cloudy の `.cloudy()` は要素自体をぼかす element blur で、iOS の backdrop blur とは根本的に異なる。見た目の差は大きい
- **AGSL は強力**: Android 13+ 限定だが、RuntimeShader で GPU シェーダーを自由に書ける。iOS の Metal シェーダーとほぼ同等のことが可能
- **Lab アプリ方式が有効**: パラメータをリアルタイム調整できる Lab を先に作ると、各効果層の寄与が可視化できて調整が圧倒的に楽
- **ジャイロの係数調整が重要**: センサー値の range とシェーダー uniform の感度のバランスが悪いと、動いているのか分からない。デバッグ表示は必須
- **iOS の内部構造は公開されていない**: 正確な再現は逆解析と推測に頼らざるを得ない。LiquidGlassKit や ShatteredGlass のようなオープンソースプロジェクトが大きなヒントになった

---

## 参考文献・ソース一覧

### Apple 公式

| 資料 | URL |
|------|-----|
| WWDC25 Session 219: Meet Liquid Glass | https://developer.apple.com/videos/play/wwdc2025/219/ |
| WWDC25 Session 356: Get to know the new design system | https://developer.apple.com/videos/play/wwdc2025/356/ |
| WWDC25 Session 323: Build a SwiftUI app with the new design | https://developer.apple.com/videos/play/wwdc2025/323/ |
| WWDC25 Session 284: Build a UIKit app with the new design | https://developer.apple.com/videos/play/wwdc2025/284/ |
| glassEffect(_:in:) SwiftUI | https://developer.apple.com/documentation/swiftui/view/glasseffect(_:in:) |
| Applying Liquid Glass to custom views | https://developer.apple.com/documentation/SwiftUI/Applying-Liquid-Glass-to-custom-views |
| UIGlassEffect (UIKit) | https://developer.apple.com/documentation/uikit/uiglasseffect |
| GlassEffectContainer | https://developer.apple.com/documentation/swiftui/glasseffectcontainer |

### Android / AGSL 公式

| 資料 | URL |
|------|-----|
| AGSL ドキュメント | https://developer.android.com/develop/ui/views/graphics/agsl |
| AGSL Quick Reference | https://developer.android.com/develop/ui/views/graphics/agsl/agsl-quick-reference |
| AGSL vs GLSL | https://developer.android.com/develop/ui/views/graphics/agsl/agsl-vs-glsl |
| RuntimeShader API Reference | https://developer.android.com/reference/android/graphics/RuntimeShader |

### ライブラリ

| 資料 | URL |
|------|-----|
| Haze (Chris Banes) | https://github.com/chrisbanes/haze |
| Haze ドキュメント | https://chrisbanes.github.io/haze/latest/ |
| Haze 1.0 ブログ | https://chrisbanes.me/posts/haze-1.0/ |
| Cloudy (skydoves) | https://github.com/skydoves/Cloudy |

### 逆解析・技術分析

| 資料 | URL |
|------|-----|
| LiquidGlassKit (DnV1eX) — iOS バックポート | https://github.com/DnV1eX/LiquidGlassKit |
| ShatteredGlass (AlexStrNik) — macOS 逆解析 | https://github.com/AlexStrNik/ShatteredGlass |
| LiquidGlassReference (conorluddy) — Swift/SwiftUI リファレンス | https://github.com/conorluddy/LiquidGlassReference |
| CABackdropLayer 解析 (Vaidyam) | https://aditya.vaidyam.me/blog/2018/02/17/ |
| Xcode 26 UIKit Liquid Glass System Prompts | https://github.com/artemnovichkov/xcode-26-system-prompts/blob/main/AdditionalDocumentation/UIKit-Implementing-Liquid-Glass-Design.md |
| Grow on iOS 26 (fatbobman) — UIKit+SwiftUI 適用実例 | https://fatbobman.com/en/posts/grow-on-ios26/ |
| Adopting Liquid Glass (JuniperPhoton) — 経験と落とし穴 | https://juniperphoton.substack.com/p/adopting-liquid-glass-experiences |
| Apple Liquid Glass private CSS property | https://alastair.is/apple-has-a-private-css-property-to-add-liquid-glass-effects-to-web-content/ |

### CG / シェーダー理論

| 資料 | URL |
|------|-----|
| AGSL: Made in the Shade(r) — Chet Haase | https://medium.com/androiddevelopers/agsl-made-in-the-shade-r-7d06d14fe02a |
| 2D Distance Functions — Inigo Quilez | https://iquilezles.org/articles/distfunctions2d/ |
| Distance+Gradient 2D SDF — Inigo Quilez | https://iquilezles.org/articles/distgradfunctions2d/ |
| Squircle SDF — Inigo Quilez (Shadertoy) | https://www.shadertoy.com/view/fsccz4 |
| Desperately Seeking Squircles — Figma Blog | https://www.figma.com/blog/desperately-seeking-squircles/ |
| Blurred Rounded Rectangles — Raph Levien | https://raphlinus.github.io/graphics/2020/04/21/blurred-rounded-rects.html |
| Schlick 1994 — "An Inexpensive BRDF Model for Physically-based Rendering" | https://onlinelibrary.wiley.com/doi/10.1111/1467-8659.1330233 |
| Fresnel Equations — Wikipedia | https://en.wikipedia.org/wiki/Fresnel_equations |
| Schlick's Approximation — Wikipedia | https://en.wikipedia.org/wiki/Schlick%27s_approximation |
| PBR Theory — LearnOpenGL | https://learnopengl.com/PBR/Theory |
| Dispersion (optics) — Wikipedia | https://en.wikipedia.org/wiki/Dispersion_(optics) |
| Cauchy's Equation — Wikipedia | https://en.wikipedia.org/wiki/Cauchy%27s_equation |
| Refraction, Dispersion, and Other Shader Light Effects — Maxime Heckel | https://blog.maximeheckel.com/posts/refraction-dispersion-and-other-shader-light-effects/ |
| Efficient Gaussian Blur with Linear Sampling — RasterGrid | https://www.rastergrid.com/blog/2010/09/efficient-gaussian-blur-with-linear-sampling/ |
| Video Game Blurs (Dual Kawase) — frost.kiwi | https://blog.frost.kiwi/dual-kawase/ |
| Implementing a Refractive Glass Shader in Metal — Victor Baro | https://medium.com/@victorbaro/implementing-a-refractive-glass-shader-in-metal-3f97974fbc24 |
| SDF in Metal: Adding the Liquid to the Glass — Victor Baro | https://medium.com/@victorbaro/sdf-in-metal-adding-the-liquid-to-the-glass-69abd57e2151 |
| Normals for an SDF — Inigo Quilez | https://iquilezles.org/articles/normalsSDF/ |

### 数式サマリー

| トピック | 数式 |
|---|---|
| Schlick Fresnel | `R(θ) = R0 + (1 - R0)(1 - cos θ)^5` |
| 屈折率から R0 | `R0 = ((n1 - n2) / (n1 + n2))^2` |
| GLSL refract | `η*I + (η*dot(N,I) - sqrt(1 - η^2*(1 - dot(N,I)^2)))*N` |
| 1D ガウス | `G(x) = (1/sqrt(2πσ^2)) * exp(-x^2/(2σ^2))` |
| Rounded Rect SDF | `min(max(q.x, q.y), 0) + length(max(q, 0)) - r` where `q = abs(p) - b + r` |
| SDF 法線（数値） | `n = normalize(vec2(sdf(p+ε,0)-sdf(p-ε,0), sdf(0,p+ε)-sdf(0,p-ε)))` |
| Cauchy 分散式 | `n(λ) = A + B/λ^2 + C/λ^4` |
| Blur 半径換算 | `cloudyRadiusPx = 2 × hazeBlurRadiusDp × density` |
