# Clipora — Video Slideshow & Editor

> **A production-grade Android video creation app** built with a custom OpenGL ES 2.0 rendering pipeline, real-time GPU shader effects, and FFmpeg-powered encoding — targeting Android 14 (API 34) with Kotlin 2.0.

---

## Why This Project

Most video editor apps on the Play Store are either thin wrappers around FFmpeg or bloated SDKs. Clipora is built ground-up with a **custom GPU rendering pipeline**, where every transition, filter, and effect is a hand-authored GLSL shader that runs entirely on the device GPU — no cloud, no third-party rendering SDK.

This project demonstrates end-to-end ownership of a complex consumer media product: from OpenGL surface management and frame-accurate video encoding, to reactive UI state, dependency injection, and AdMob monetization.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | **Kotlin 2.0.21** |
| UI | **ViewBinding · ConstraintLayout · Material Design 3 · RecyclerView** |
| Architecture | **MVVM · LiveData · ViewModel (AndroidX Lifecycle 2.6.2)** |
| Reactive | **RxJava 2 · RxKotlin · RxAndroid** |
| Dependency Injection | **Kodein 5.2** |
| Video Playback | **ExoPlayer 2.19.1** |
| Video Encoding | **Mobile-FFmpeg** (custom JNI module) |
| GPU Rendering | **OpenGL ES 2.0 · GLSL shaders · GPUImage 2.1** |
| Image Loading | **Glide 4.16** |
| Animations | **Lottie 6.1** |
| Networking | **OkHttp 4.12** |
| Events | **EventBus 3.3** |
| Ads & Analytics | **AdMob (Google Play Ads 22.6) · Firebase Analytics · Crashlytics** |
| Build | **AGP 8.7.3 · Gradle Kotlin DSL · compileSdk 34 · minSdk 21** |

---

## Architecture

```
app/
├── base/                   # BaseActivity · BaseSlideShowActivity (ViewBinding inheritance)
├── ui/
│   ├── edit_video/         # VideoSlideActivity · VideoSlideActivity2
│   ├── pick_media/         # PickMediaActivity · MediaListFragment · MediaFolderFragment
│   ├── select_music/       # SelectMusicActivity + ViewModel
│   ├── trim_video/         # TrimVideoActivity
│   ├── join_video/         # JoinVideoActivity
│   ├── process_video/      # ProcessVideoActivity (encoding pipeline)
│   └── slide_show/         # ImageSlideShowActivity + SlideShowViewModel
├── custom_view/            # AddTextLayout · ChooseStickerLayout · VideoTimelineView
├── modules/
│   └── local_storage/      # LocalStorageData interface + Impl (repository pattern)
├── adapter/                # 15+ RecyclerView adapters
├── models/                 # Data models
└── utils/                  # DimenUtils · MediaUtils · BitmapUtils · Logger
```

**Pattern:** MVVM with a repository layer. ViewModels expose LiveData streams consumed by Activities/Fragments. Side-effects (encoding, I/O) run on background threads via RxJava, reporting progress back through LiveData or EventBus.

**Dependency Injection:** Kodein module defined in `VideoMakerApplication` — singleton scopes for `LocalStorageData`, `AudioManagerV3`, `MusicPlayer`; factory scopes for ViewModels.

---

## Multi-Module Build

| Module | Role |
|---|---|
| `:app` | Main application — UI, ViewModels, business logic |
| `:gpuv2` | GPU video rendering — GLSurfaceView renderers, ExoPlayer integration |
| `:gpufilter` | GLSL effect pipeline — 16+ real-time shader effects |
| `:mobile-ffmpeg1125` | Pre-built FFmpeg JNI AAR — transcoding, muxing, format conversion |

Separating GPU rendering from the app module keeps compile times fast and makes the shader pipeline independently testable.

---

## Engineering Highlights

### 1. Custom OpenGL ES 2.0 Rendering Pipeline
Built a renderer hierarchy from scratch using `GLSurfaceView`:

- **`SlideRenderer`** — manages transition blending between two textures using `GL_ONE_MINUS_SRC_ALPHA`
- **`ImageSlideRenderer`** — frame-locked image-to-video rendering at target FPS
- **`VideoPlayerSlideRenderer`** — synced video playback inside a slide context
- **`TextureRenderer`** — direct texture-to-framebuffer pass for sticker/text compositing

Each renderer owns its EGL context lifecycle and shader program, avoiding the memory leaks common in GLSurfaceView-based apps.

### 2. Real-Time GLSL Shader Effect System
Designed a `GSEffect` base class with a uniform injection interface. 16+ effects ship as concrete implementations — each a self-contained GLSL program:

`Cross · Glitch · GrayScale · HalfTone · Mirror · Polygon · Rain · Snow · TVShow · Tiles · Wavy · Wisp · ZoomBlur · and more`

Effects are composited at render time, not post-processed — zero additional encode pass required.

### 3. FFmpeg Encoding Pipeline
Layered a type-safe Kotlin API over the FFmpeg JNI layer:

- Encoder variants (`EncodeV4`–`V8`) handle different source types: image sequences, video clips, mixed timelines
- `FFmpegCmd` wraps command construction — aspect ratio, resolution (480p / 720p / 1080p), audio mux
- Progress streamed via `Statistics` callbacks → LiveData → UI progress bar
- `WakeLock` acquired for duration of encode to prevent CPU throttling mid-export

### 4. Kotlin 2.0 Migration
Migrated the entire codebase from `kotlinx.android.synthetic` (removed in Kotlin 2.0) to **ViewBinding** across 10+ files simultaneously:

- Custom views using `XxxBinding.inflate(LayoutInflater.from(context), this, true)`
- Fragments using `XxxBinding.bind(requireView())`
- BaseActivity child activities using `XxxBinding.bind(binding.mainContentLayout.getChildAt(0))`
- Resolved `when` exhaustiveness errors and `Float?` → `Float` type safety gaps introduced by stricter Kotlin 2.0 inference

### 5. Reactive Media Pipeline
`PickMediaViewModel` uses RxJava to scan local storage asynchronously, emitting `MediaDataModel` items through `MutableLiveData` streams. `MediaListFragment` and `MediaFolderFragment` observe the same ViewModel — single source of truth, no data duplication.

---

## Features

- **Slideshow Creator** — images → MP4 with configurable duration, transitions, aspect ratio
- **Video Editor** — trim, join multiple clips, add effects and filters
- **GPU Effects** — 16+ real-time GLSL effects applied during export (no re-encode)
- **Music** — add, trim, and mix background audio tracks
- **Text & Stickers** — drag-to-place, resize, font/color/style customization
- **Export Quality** — 480p / 720p HD / 1080p Full HD; 16:9, 9:16, 1:1 aspect ratios
- **My Studio** — save and resume in-progress projects
- **Monetization** — AdMob banners, interstitials, native ads, rewarded ads; Firebase crash reporting

---

## Project Scale

| Metric | Value |
|---|---|
| Kotlin source files | ~286 |
| Gradle modules | 4 |
| RecyclerView adapters | 15+ |
| GLSL shader effects | 16+ |
| Encoder variants | 5 (V4–V8) |
| Target devices | Android 5.0–14 (API 21–34) |

---

## Getting Started

**Prerequisites:** Android Studio Hedgehog or later · JDK 17 · Android SDK 34

```bash
git clone https://github.com/your-username/video-slide-show-clipora.git
cd video-slide-show-clipora
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

> **AdMob:** Replace the AdMob App ID in `AndroidManifest.xml` and ad unit IDs in `AdsUtils` with your own before running in production.

---

## What I Would Do Next (Roadmap)

- **Migrate to Jetpack Compose** — replace RecyclerView adapters with lazy lists; keep OpenGL surfaces as `AndroidView` interop
- **Replace Kodein with Hilt** — tighter Jetpack integration, compile-time graph validation
- **Room database** for project persistence instead of SharedPreferences
- **Media3** to replace ExoPlayer 2.x (ExoPlayer is now archived upstream)
- **Kotlin Coroutines** to replace RxJava — reduce boilerplate and memory overhead
- **Baseline Profiles** to reduce app startup jank on first run

---

## About

Built by a Senior Android Engineer with experience shipping production apps across media, social, and utility categories.

- **Languages:** Kotlin · Java · GLSL
- **Domains:** Video/media processing · GPU rendering · Real-time graphics · Android architecture
- **Open to:** Senior Android Engineer roles ($120k+) — full-time or contract, remote-friendly

[builwithusman@gmail.com](mailto:builwithusman@gmail.com)
