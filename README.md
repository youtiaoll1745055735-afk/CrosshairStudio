# Crosshair Studio — Android 外置游戏准心

一款 Android 原生外置游戏准心应用。通过系统悬浮窗在其他应用（游戏）上方显示自定义准心，
内置完整准心编辑器、图层系统、实时预览、预设管理与配置导入导出。

**不修改游戏文件、不读取游戏进程、不进行自动瞄准或自动触控。**

## 功能总览

### 首页
- 当前准心实时预览（与悬浮窗共用同一渲染引擎）
- 悬浮准心开关（真实 WindowManager 悬浮层 + 前台服务）
- 悬浮窗权限 / 服务运行状态
- 快速切换准心、一键进入编辑器

### 编辑器（实时生效，悬浮窗同步刷新）
- 顶部实时预览：深色 / 浅色 / 棋盘三种背景，预览缩放 50%–200%
- **样式**：9 种内置预设（经典四段式、四段式+中心点、中心圆点、空心圆、圆环+中心点、十字线、四角括号、菱形、几何组合）+ 空白准心
- **基础参数**：整体缩放、线条长度、线条粗细、中心间距、整体透明度、旋转角度、水平/垂直偏移
  （每个参数支持拖动、点击数值精确输入、一键恢复默认）
- **中心点**：独立组件，6 种样式（圆点/空心圆/方点/菱点/十字点/小圆环），独立大小、颜色、透明度、描边、偏移
- **颜色与描边**：24 色预设、RGB 滑块、HEX 输入、透明度；全局描边（颜色/宽度）；阴影（颜色/模糊半径）
- **图层系统**：9 种图层（线条/十字线/圆点/圆环/实心圆/菱形/方形/四角括号/三角形），
  每个图层支持显示/隐藏、独立颜色、透明度、尺寸、粗细、长度、间距、旋转、偏移、描边，可复制、删除
- 保存 / 预设编辑后自动引导"另存为副本"

### 准心库
- 内置预设与用户配置统一管理，筛选（全部/预设/我的/收藏）
- 使用、编辑、收藏、复制、重命名、删除
- JSON 配置导入 / 导出（带版本号，导入时全字段校验与范围收敛）

### 设置
- 悬浮窗权限管理（缺失时引导授权并给出明确提示）
- 位置校准（水平/垂直滑块、重置）、锁定位置开关
- 默认准心、主题（深色 / AMOLED 纯黑）、使用说明、隐私说明、恢复默认配置

## 技术要点

| 项 | 实现 |
|---|---|
| 语言/UI | Kotlin + Jetpack Compose (Material 3) |
| 绘制 | 自研矢量渲染引擎 `CrosshairRenderer`（Canvas/Path，抗锯齿），预览与悬浮层共用 |
| 悬浮窗 | `TYPE_APPLICATION_OVERLAY` + 前台服务（API 34+ `specialUse` 类型，已声明 `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`） |
| 持久化 | DataStore Preferences + kotlinx.serialization（带版本号 JSON） |
| 最低/目标 | minSdk 26 / targetSdk 35 |
| 触摸穿透 | 锁定后 `FLAG_NOT_TOUCHABLE`，游戏触摸不受影响；解锁可拖动、长按锁定、双击打开应用 |
| 性能 | 仅参数变化时 invalidate，无持续高频绘制；全屏单 View + 简单矢量图元 |

### 悬浮层交互
- 未锁定时：**拖动**移动准心位置，**长按**快速锁定，**双击**打开应用
- 通知栏提供常驻通知与"关闭准心"快捷操作

## 项目结构

```
CrosshairStudio/
├── app/src/main/java/com/crosshair/studio/
│   ├── App.kt / MainActivity.kt        # 应用入口（权限请求、主题）
│   ├── core/                            # JSON 实例、颜色工具
│   ├── data/
│   │   ├── model/                       # CrosshairConfig / Layer / CenterPoint / AppSettings
│   │   ├── Presets.kt                   # 9 种内置预设
│   │   ├── ConfigRepository.kt          # 配置仓库（DataStore + StateFlow）
│   │   ├── SettingsRepository.kt        # 设置仓库（防抖持久化）
│   │   └── ConfigValidator.kt           # 导入校验/导出封装
│   ├── render/CrosshairRenderer.kt      # 矢量绘制引擎（预览/悬浮共用）
│   ├── overlay/                         # OverlayService / OverlayView / Session / Controller
│   └── ui/
│       ├── AppViewModel.kt              # 全局共享状态（配置+悬浮窗桥接）
│       ├── MainScreen.kt                # 底部四栏导航
│       ├── home/ editor/ library/ settings/
│       └── components/                  # 预览画布、参数滑块、颜色选择器、对话框等
└── app/build/outputs/apk/debug/app-debug.apk
```

## 构建与安装

### 常规开发机（Linux/macOS/Windows + Android Studio）
```bash
cd CrosshairStudio
./gradlew assembleDebug
# 产物: app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 本容器（Alpine aarch64 无 Android Studio）的特殊说明
本次交付环境为 Alpine Linux (aarch64) 容器，官方 Android SDK 主机工具仅有 x86_64 版，做了如下适配
（普通开发机**不需要**这些步骤）：

1. `local.properties` 指向 `/opt/android-sdk`（platforms;android-35 + build-tools;35.0.0）。
2. **aapt2**：使用静态编译的 aarch64 主机工具（lzhiyong/android-sdk-tools 35.0.2），
   通过 `gradle.properties` 的 `android.aapt2FromMavenOverride=/opt/aapt2-native/build-tools/aapt2` 指定。
3. **apkzlib 删除补丁**：本容器内核下 musl `remove()` 对目录失效（`File.delete()` 返回 false，
   而 NIO `unlinkat` 正常），导致 AGP `mergeDebugJavaResource` 清理临时目录失败。
   已用 ASM 字节码补丁把缓存中 `com.android.tools.build:apkzlib:8.7.3` 的
   `TemporaryFile.deleteFile` 内 `File.delete()` 替换为 `Files.deleteIfExists()`。
   若 Gradle 缓存被清理后重新构建报 "Failed to delete '/tmp/tempdir_*'"，重跑：
   ```bash
   javac -cp "$GRADLE_HOME/lib/asm-9.7.jar:$GRADLE_HOME/lib/asm-tree-9.7.jar" /tmp/patch/PatchApkzlib.java
   # 再将输出 jar 覆盖到对应缓存路径（脚本 /tmp/patch/PatchApkzlib.java 已保留）
   ```

### 首次使用
1. 打开应用 → 首页 → 打开「悬浮准心」→ 按提示授予悬浮窗权限。
2. 进入编辑器选择样式、调节参数（悬浮层实时刷新）。
3. 「设置 → 位置校准」精确放置；或解锁后直接在悬浮层上拖动、长按锁定。
4. 建议关闭游戏内置准心，避免重叠。

## 已完成功能清单

- [x] 完整项目架构（数据/渲染/悬浮窗/UI 解耦）
- [x] 四栏导航：首页 / 编辑器 / 准心库 / 设置
- [x] 9 种准心预设 + 空白准心
- [x] 矢量绘制引擎（预览与悬浮窗共用，效果一致）
- [x] 参数滑块 + 数值精确输入 + 恢复默认（8 项基础参数）
- [x] 中心点独立组件（6 样式、颜色/描边/偏移独立）
- [x] 颜色系统（预设色板、RGB、HEX、透明度）
- [x] 描边与阴影
- [x] 图层系统（9 类型，显示/隐藏、独立参数、复制、删除，可扩展）
- [x] 实时预览（3 种背景 + 缩放）
- [x] 真实悬浮窗（前台服务、通知、权限处理、失败提示）
- [x] 位置校准 / 拖动 / 锁定（触摸穿透）/ 长按锁定 / 双击打开
- [x] 横竖屏与多分辨率适配（全屏布局 + 归一化坐标）
- [x] 配置持久化（DataStore，重启恢复）+ 当前配置记忆
- [x] 新建/保存/另存/复制/重命名/删除/收藏
- [x] JSON 导入导出（版本号 + 校验防崩溃）
- [x] 深色游戏工具风格 UI（Material 3，AMOLED 主题）
- [x] Debug APK 构建通过

## 未完成 / 已知限制

- [ ] **自定义 SVG 图案**（第三阶段功能，未实现；图层系统已预留扩展位）
- [ ] 配置分享（可先通过"导出/导入"文件互传实现）
- [ ] 图层拖拽排序（可复制+删除+参数调整）
- [ ] 真机自动化测试（本容器无法安装 APK，逻辑经编译验证、渲染参数全链路走同一引擎）
- [ ] 部分国产 ROM 需在系统设置中额外允许"后台弹出界面/悬浮窗"（系统限制，应用已做权限缺失提示）

## 隐私

- 不申请网络权限（Manifest 中无 INTERNET），配置仅存本机。
- 悬浮层仅做视觉显示：不模拟触控、不读取游戏数据、不修改游戏文件。
