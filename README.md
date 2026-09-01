# code_gps

一款原生 Android（Kotlin）手机版 GPS 软件，目标是提供实时定位、轨迹记录等功能。当前处于早期开发阶段。

## 当前功能（v0.3.0）

- 请求并处理定位运行时权限（`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`），权限被拒绝时给出提示，不崩溃。
- 通过 `FusedLocationProviderClient` 订阅实时位置更新。
- 通过 `LocationManager.registerGnssStatusCallback` 订阅真实 GNSS 卫星状态：
  - 卫星穹顶图（sky plot）：按方位角/仰角把每颗可见卫星画在极坐标图上（天顶在圆心，地平线在外圈），参与定位解算的卫星实心显示、仅可见未使用的卫星空心显示，不同卫星系统（GPS/GLONASS/北斗/Galileo 等）用不同颜色区分。
  - 卫星数量与分类统计：按星座分组的芯片列表（如"北斗 4/6"），顶部状态胶囊同步显示总的"已用/可见"卫星数。
- 深色 HUD／卫星雷达风格界面（Jetpack Compose）：
  - 顶部状态胶囊：搜索卫星中 / 已定位（含卫星数）/ 权限被拒绝，搜索时状态点呈脉冲动画。
  - 经纬度、海拔、精度、最后更新时间以毛玻璃质感卡片展示（API 31+ 有真实背景模糊，以下版本自动降级为半透明纯色，不崩溃、不强制提高 `minSdk`）。
  - 半圆形速度表（Canvas 绘制的渐变弧线，0–30 m/s）。
  - 所有数值变化都有滑入/淡入动画，不会生硬跳变。
  - 内容整体可竖向滚动，适配不同屏幕高度（含折叠屏内外屏两种尺寸）。

## 技术栈

- 语言：Kotlin
- UI：Jetpack Compose + Material 3，自定义 Canvas 绘制（卫星穹顶图、速度表）
- 定位：Google Play Services `play-services-location`（`FusedLocationProviderClient`）
- 卫星状态：Android 平台 `LocationManager` / `GnssStatus`（无需额外权限，`ACCESS_FINE_LOCATION` 已覆盖）
- 构建：Gradle 8.7（Kotlin DSL），Android Gradle Plugin 8.4.0
- `minSdk` 26，`targetSdk` / `compileSdk` 34

## 项目结构

```
app/src/main/kotlin/com/codegps/app/
├── MainActivity.kt                    # 入口 Activity：权限请求 + edge-to-edge + 生命周期感知的位置/卫星订阅
├── location/
│   ├── GpsReading.kt                   # 位置数据的不可变快照（与平台 API 解耦）
│   ├── LocationRepository.kt           # 封装 FusedLocationProviderClient，暴露 Flow<GpsReading>
│   ├── GnssConstellation.kt            # 卫星系统枚举（GPS/GLONASS/北斗/Galileo…），与平台常量解耦
│   ├── SatelliteInfo.kt                # 单颗卫星状态快照（方位角/仰角/信噪比/是否参与定位）
│   └── GnssRepository.kt               # 封装 LocationManager.registerGnssStatusCallback，暴露 Flow<List<SatelliteInfo>>
└── ui/
    ├── LocationScreen.kt               # 组合根：权限提示 / HUD 主界面布局
    ├── theme/
    │   ├── Color.kt                     # HUD 配色（深空背景、霓虹青/紫、状态色）
    │   ├── GpsStatusColor.kt            # GPS 精度 → 状态色的统一映射
    │   ├── GnssConstellationColor.kt    # 卫星系统 → 颜色 / 显示名称的统一映射
    │   ├── Type.kt                      # 等宽数字读数样式 + 常规排版
    │   └── Theme.kt                     # CodeGpsTheme：固定深色 MaterialTheme
    └── components/
        ├── GlassSurface.kt              # 毛玻璃卡片容器（API 31+ 真实模糊，以下版本降级）
        ├── SatelliteSkyPlot.kt          # 卫星穹顶图（极坐标 Canvas 绘制）
        ├── SatelliteSummary.kt          # 按星座分组的卫星数量芯片列表
        ├── SpeedGauge.kt                # 半圆速度表
        ├── ReadoutCard.kt               # 单项数据卡片（色条 + 标签 + 动画数值）
        └── StatusChip.kt                # 顶部状态胶囊（脉冲动画）
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

### v0.3.0（2026-09-01）

- 新增真实 GNSS 卫星数据：基于 `LocationManager.registerGnssStatusCallback` / `GnssStatus`，无需新增权限。
- 用真实的卫星穹顶图（sky plot）替换了 v0.2.0 中纯装饰性的雷达动画（`RadarIndicator` 已删除）：按方位角/仰角在极坐标图上绘制每颗可见卫星，参与定位解算的实心显示、仅可见的空心显示，不同卫星系统用不同颜色区分。
- 新增按星座分组的卫星数量/分类展示（`SatelliteSummary`，如"北斗 4/6"），顶部状态胶囊同步显示总的"已用/可见"卫星数。
- 原先叠加在雷达图上的经纬度大字读数，改为独立的一组数据卡片，信息不丢失、布局更清晰。
- 界面内容改为可竖向滚动，适配折叠屏内外屏等不同高度的屏幕（此前在较小屏幕上可能出现内容被截断）。
- 已知后续可做但本版本未做：无障碍的"减少动效"开关、浅色主题、卫星穹顶图的位置变化动画（当前每次快照直接刷新，不做插值动画）。

### v0.2.0（2026-09-01）

- UI 全面重做为深色 HUD／卫星雷达风格，替换原先的白色 Material 表单式卡片。
- 新增：雷达风格定位指示器（精度圈随 GPS 精度变色/变半径）、半圆速度表、顶部脉冲状态胶囊。
- 新增：毛玻璃质感数据卡片（`GlassSurface`，API 31+ 真实背景模糊，低版本自动降级）。
- 新增：所有实时数值（经纬度/速度/海拔/精度/时间）变化时的滑入动画，替代原来的瞬间跳变文本。
- 代码新增 `ui/theme/`（配色、字体、GPS 状态色映射）与 `ui/components/`（雷达、速度表、数据卡片、状态胶囊）两个包，`LocationScreen.kt` 收敛为纯粹的布局组合根。
- 权限提示 / 权限被拒绝界面同步改为一致的深色玻璃卡片风格。
- 已知后续可做但本版本未做：无障碍的“减少动效”开关、浅色主题。

### v0.1.0（2026-09-01）

- 项目初始化脚手架。
- 实现实时 GPS 坐标展示（纬度/经度/精度/海拔/速度），基于 `FusedLocationProviderClient`。
- Compose UI，含运行时定位权限的请求与拒绝态处理。
