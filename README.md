# Crosshair Studio 悬浮准心 / Crosshair Studio — External Game Crosshair Overlay

**中文** | **English** (scroll down for the English section)

> 一款 Android 外置游戏准心悬浮窗工具。不修改游戏、不注入、不读取数据、无自动瞄准——纯显示层。
> An Android external game-crosshair overlay. No game modification, no injection, no data reading, no auto-aim — a pure display overlay.

---

## 中文文档

### 简介

Crosshair Studio 是一款 Android 游戏外置准心工具，通过系统悬浮窗在游戏画面上方显示一个可完全自定义的准心。它基于 Kotlin + Jetpack Compose 构建，采用 WindowManager 悬浮窗 + 前台服务（Foreground Service）实现，支持 9 种预设准心、自由图层叠加、全参数调节、实时预览与配置持久化。

**声明**：本工具为纯显示层，不修改游戏 APK/数据/内存，不注入，不 Hook，不自动点击、不自动瞄准，也不绕过任何游戏安全机制。

### 功能特性

- **9 种预设准心**：经典四段线条、线条+中心点、纯中心圆点、空心圆环、圆环+点、十字线、四角括号、菱形、组合
- **图层系统**：9 种图层类型（线条 / 十字线 / 圆点 / 圆环 / 实心圆 / 菱形 / 方形 / 四角括号 / 三角形）任意叠加
- **全参数可调**：长度、粗细、间隙、大小、旋转、透明度、颜色、描边、外发光阴影、整体缩放/旋转/位移
- **中心点独立设置**：大小、颜色、透明度、描边
- **所见即所得**：编辑页实时预览与悬浮窗使用同一渲染引擎，效果完全一致
- **配置持久化**：DataStore 保存，重启不丢失；支持收藏、图库管理、预设只读保护
- **悬浮窗控制**：触摸穿透（锁定）、拖动、隐藏、前台服务常驻
- **深色游戏工具风 UI**：Material 3，暗色主题

### 技术栈

- Kotlin + Jetpack Compose（Material 3）
- WindowManager 悬浮窗（TYPE_APPLICATION_OVERLAY）
- Foreground Service（specialUse，适配 Android 14/15 前台服务限制）
- DataStore Preferences 持久化
- 统一渲染引擎：Compose 预览与悬浮窗共用 `CrosshairRenderer`

### 环境要求

- Android 8.0（API 26）及以上
- 编译环境：Android Studio（AGP 8.7.3、Kotlin 2.0.21、compileSdk 35）

### 构建

```bash
./gradlew assembleDebug
# 输出: app/build/outputs/apk/debug/app-debug.apk
```

### 安装与使用

1. 安装 APK，允许「安装未知来源应用」
2. 首次打开授予「悬浮窗」权限
3. 选择预设或在编辑器里自定义准心
4. 点击「显示悬浮窗」开启，进入游戏即可在画面上看到准心
5. 通知栏通知可快速隐藏/显示悬浮窗

### 目录结构

```
app/src/main/java/com/crosshair/studio/
├── core/          # 扩展工具
├── data/          # 配置仓库、预设、校验（DataStore）
│   └── model/     # 数据模型
├── render/        # 统一渲染引擎
├── overlay/       # 悬浮窗会话/视图/前台服务/控制器
├── ui/            # ViewModel、主界面
│   ├── components/# 通用组件
│   ├── editor/    # 编辑器
│   ├── home/      # 首页
│   ├── library/   # 图库
│   └── settings/  # 设置
```

### 许可证

[MIT](./LICENSE)

---

## English

### Overview

Crosshair Studio is an Android external game-crosshair overlay. It draws a fully customizable crosshair on top of your game via a system overlay window. Built with Kotlin + Jetpack Compose, it uses a WindowManager overlay plus a Foreground Service, offering 9 presets, free layer stacking, full parameter control, a live preview, and persistent configuration.

**Disclaimer**: This is a display-only overlay. It does not modify the game APK/data/memory, does not inject or hook anything, and has no auto-click, auto-aim, or anti-cheat bypass of any kind.

### Features

- **9 presets**: classic 4-line, lines+dot, center dot, hollow ring, ring+dot, cross, corner brackets, diamond, combo
- **Layer system**: 9 layer types (lines / cross / dot / ring / filled circle / diamond / rect / bracket / triangle), freely stacked
- **Full parameter control**: length, thickness, gap, size, rotation, opacity, color, stroke, outer glow/shadow, global scale/rotation/offset
- **Independent center point**: size, color, opacity, stroke
- **WYSIWYG**: the editor's live preview and the overlay share the same rendering engine
- **Persistent config**: stored via DataStore, survives restart; favorites, library, read-only presets
- **Overlay controls**: touch-through (lock), drag, hide, persistent foreground service
- **Dark game-tool UI**: Material 3 dark theme

### Tech Stack

- Kotlin + Jetpack Compose (Material 3)
- WindowManager overlay (TYPE_APPLICATION_OVERLAY)
- Foreground Service (specialUse, adapted for Android 14/15 FGS restrictions)
- DataStore Preferences
- Shared renderer: Compose preview and overlay both use `CrosshairRenderer`

### Requirements

- Android 8.0 (API 26)+
- Build: Android Studio (AGP 8.7.3, Kotlin 2.0.21, compileSdk 35)

### Build

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Install & Usage

1. Install the APK and allow "Install unknown apps"
2. Grant the overlay permission on first launch
3. Pick a preset or customize your own in the editor
4. Tap "Show overlay" and launch your game
5. Use the notification to quickly show/hide the overlay

### Project Structure

```
app/src/main/java/com/crosshair/studio/
├── core/          # utilities
├── data/          # config repository, presets, validation (DataStore)
│   └── model/     # data models
├── render/        # unified renderer
├── overlay/       # overlay session/view/foreground service/controller
├── ui/            # ViewModel, main screen
│   ├── components/# shared components
│   ├── editor/    # editor
│   ├── home/      # home
│   ├── library/   # library
│   └── settings/  # settings
```

### License

[MIT](./LICENSE)
