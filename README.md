# code_gps

一款原生 Android（Kotlin）手机版 GPS 软件，目标是提供实时定位、轨迹记录等功能。当前处于早期开发阶段。

## 当前功能（v0.1.0）

- 请求并处理定位运行时权限（`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`），权限被拒绝时给出提示，不崩溃。
- 通过 `FusedLocationProviderClient` 订阅实时位置更新。
- 界面（Jetpack Compose）实时展示：纬度、经度、精度（米）、海拔（米）、速度（米/秒）、最后更新时间。

## 技术栈

- 语言：Kotlin
- UI：Jetpack Compose + Material 3
- 定位：Google Play Services `play-services-location`（`FusedLocationProviderClient`）
- 构建：Gradle 8.7（Kotlin DSL），Android Gradle Plugin 8.4.0
- `minSdk` 26，`targetSdk` / `compileSdk` 34

## 项目结构

```
app/src/main/kotlin/com/codegps/app/
├── MainActivity.kt              # 入口 Activity：权限请求 + 生命周期感知的位置订阅
├── location/
│   ├── GpsReading.kt             # 位置数据的不可变快照（与平台 API 解耦）
│   └── LocationRepository.kt     # 封装 FusedLocationProviderClient，暴露 Flow<GpsReading>
└── ui/
    └── LocationScreen.kt         # Compose 界面：权限提示 / 实时数据卡片
```

## 构建与运行

**依赖环境**：JDK 17，Android SDK（`compileSdk 34` / `build-tools 34.0.0`）。

```bash
# 命令行构建 Debug APK
./gradlew assembleDebug

# 产物路径
app/build/outputs/apk/debug/app-debug.apk
```

也可以直接用 Android Studio 打开项目根目录运行。

首次构建前需要在项目根目录创建 `local.properties`（该文件已被 `.gitignore` 忽略，不会提交），内容示例：

```properties
sdk.dir=/path/to/your/Android/Sdk
```

## 权限说明

| 权限 | 用途 |
|---|---|
| `ACCESS_FINE_LOCATION` | 获取高精度 GPS 坐标，这是本应用的核心功能 |
| `ACCESS_COARSE_LOCATION` | GPS 不可用时的网络定位兜底 |

## 版本历史

### v0.1.0（2026-09-01）

- 项目初始化脚手架。
- 实现实时 GPS 坐标展示（纬度/经度/精度/海拔/速度），基于 `FusedLocationProviderClient`。
- Compose UI，含运行时定位权限的请求与拒绝态处理。
