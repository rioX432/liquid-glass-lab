# Liquid Glass Lab

A hands-on comparison project for blur and glass effect libraries on Android and iOS.

## Purpose

- Compare **Haze** vs **Cloudy** on Android (visual quality, API ergonomics, gotchas)
- Reference native **Liquid Glass** on iOS 26 as a baseline
- Validate compatibility with the latest toolchain (Kotlin 2.3, AGP 9.0, Compose BOM 2026.01)
- Inform library choice for production KMP projects

## Tech Stack

| Component | Version | Notes |
|---|---|---|
| Kotlin | 2.3.0 | |
| AGP | 9.0.0 | Built-in Kotlin, `com.android.kotlin.multiplatform.library` for shared module |
| Gradle | 9.1.0 | |
| Compose BOM | 2026.01.00 | |
| Compose Multiplatform | 1.10.0 | Compiler plugin only |
| Haze | 1.7.1 | Built for Kotlin 2.2.20 — ABI-compatible with 2.3.0 |
| Cloudy | 0.5.0 | Built for Kotlin 2.3.0 |
| iOS Deployment Target | 26.0 | Required for `.glassEffect()` |

## Project Structure

```
liquid-glass-lab/
├── shared/            # KMP shared module (sample image URLs)
├── androidApp/        # Jetpack Compose app
│   └── screens/
│       ├── HazeScreen.kt        # Haze library demo
│       ├── CloudyScreen.kt      # Cloudy library demo
│       └── ComparisonScreen.kt  # Side-by-side comparison
└── iosApp/            # SwiftUI app (standalone Xcode project)
    └── LiquidGlassScreen.swift  # Native Liquid Glass demo
```

## Android Screens

### HazeScreen

Demonstrates [Haze](https://github.com/chrisbanes/haze) with `hazeSource` / `hazeEffect` pattern:

- **Material variants**: `ultraThin()`, `thin()`, `regular()` side by side
- **Progressive blur**: vertical gradient from full blur to transparent
- Image grid as the blur source, floating cards as the effect layer

### CloudyScreen

Demonstrates [Cloudy](https://github.com/skydoves/Cloudy) with three tabs:

- **Blur** — `Modifier.cloudy(radius)` with adjustable radius slider
- **Liquid Glass** — `Modifier.liquidGlass()` with sliders for refraction, curve, dispersion, corner radius + draggable lens position
- **Combined** — both `.cloudy()` and `.liquidGlass()` applied together

### ComparisonScreen

Side-by-side comparison using the same content:

- Floating blurred card over an image grid (Haze vs Cloudy)
- Single image full blur (Haze `regular()` vs Cloudy `radius=20`)

## iOS Screen

### LiquidGlassScreen (iOS 26+)

Native SwiftUI Liquid Glass as a reference baseline, with three tabs:

- **Glass Effects** — `.glassEffect(.regular)` / `.clear`, shape variants (capsule, circle, rounded rect, ellipse), tinted glass
- **Glass Cards** — `GlassEffectContainer` with grouped glass elements and `.interactive()` buttons
- **Interactive** — expand/collapse animation with `glassEffectID` morphing transitions

## Getting Started

### Android

```bash
./gradlew :androidApp:installDebug
```

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode 26+, select an iOS 26 simulator or device, and run.

## Compatibility Findings

| Library | Built with | Tested with | Result |
|---|---|---|---|
| Haze 1.7.1 | Kotlin 2.2.20, CMP 1.9.3 | Kotlin 2.3.0, AGP 9.0 | Builds and runs (ABI compatible) |
| Cloudy 0.5.0 | Kotlin 2.3.0, CMP 1.10.0 | Kotlin 2.3.0, AGP 9.0 | Fully compatible |

## License

MIT
