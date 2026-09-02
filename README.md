# code_gps

一款原生 Android（Kotlin）手机版 GPS 软件，目标是提供实时定位、轨迹记录等功能。当前处于早期开发阶段。

## 当前功能（v0.5.2）

- 请求并处理定位运行时权限（`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`），权限被拒绝时给出提示，不崩溃。
- 通过 `FusedLocationProviderClient` 订阅实时位置更新。
- 通过 `LocationManager.registerGnssStatusCallback` 订阅真实 GNSS 卫星状态：
  - 卫星穹顶图（sky plot）：按方位角/仰角把每颗可见卫星画在极坐标图上（天顶在圆心，地平线在外圈），不同卫星系统（GPS/GLONASS/北斗/Galileo 等）用不同颜色区分；参与定位解算的卫星带有醒目的"锁定光环"标记，仅可见未使用的卫星以空心圆显示。
  - 卫星信号强度列表：按信噪比（C/N0，dB-Hz）由强到弱排序，逐颗列出卫星短代号（如 G12/C06）、信号强度条、原始数值，参与定位的卫星带 ✓ 标记。
  - 卫星数量与分类统计：按星座分组的芯片列表（如"北斗 4/6"），顶部状态胶囊同步显示总的"已用/可见"卫星数。
- 深色 HUD／卫星雷达风格界面（Jetpack Compose）：
  - 顶部状态胶囊：搜索卫星中 / 已定位（含卫星数）/ 权限被拒绝，搜索时状态点呈脉冲动画。
  - 经纬度、海拔、精度、最后更新时间以毛玻璃质感卡片展示（API 31+ 有真实背景模糊，以下版本自动降级为半透明纯色，不崩溃、不强制提高 `minSdk`）。
  - 速度以公里/小时、米/秒两张并排的数据卡片显示（不再有半圆速度表，原来的速度表在常见速度下弧线几乎是空的，只占地方不提供信息，改为和其它读数一致的纯数字卡片）。
  - 所有实时数值（经纬度/速度/海拔/精度/最后更新时间）采用逐字符独立动画：只有真正变化的字符位原地淡入淡出，未变化的字符保持不动，不再整段滑动或跳动。
  - 自适应布局：屏幕宽度 < 600dp（手机、折叠屏合上时的外屏）卫星几何信息（状态/分类统计/穹顶图）固定在顶部不滚动，下方的信号列表与各项数值卡片单独滚动；≥ 600dp（平板、折叠屏展开后的内屏）切换为左右双栏布局，左栏同样固定展示卫星几何信息，右栏是信号列表与各项数值卡片并独立滚动，避免宽屏下内容偏窄、底部大片空白。

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
│   ├── GnssRepository.kt               # 封装 LocationManager.registerGnssStatusCallback，暴露 Flow<List<SatelliteInfo>>
│   └── SpeedUnits.kt                    # m/s → km/h 换算（1 m/s = 3.6 km/h）
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
        ├── OdometerText.kt              # 逐字符独立动画的"里程表"数值文本，按字符串末位对齐
        ├── SatelliteSkyPlot.kt          # 卫星穹顶图（极坐标 Canvas 绘制）+ 使用标记图例
        ├── SatelliteSummary.kt          # 按星座分组的卫星数量芯片列表
        ├── SatelliteSignalList.kt       # 按信号强度排序的卫星列表（信噪比条形图）
        ├── SignalStrength.kt            # 信噪比 → 0..1 归一化，穹顶图与信号列表共用同一套换算
        ├── ReadoutCard.kt               # 单项数据卡片（色条 + 标签 + OdometerText 数值）
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

### v0.5.2（2026-09-02）

- 去掉了速度部分的半圆速度表（`SpeedGauge.kt` 已删除）：日常测试速度下弧线经常接近空白，只占版面不提供信息。速度改为和海拔/精度一致的纯数字卡片，公里/小时、米/秒并排展示，动画风格与其它读数统一。

### v0.5.1（2026-09-02）

- 数字动画改回"原地"效果：`OdometerText` 每个字符位仍按字符串末位对齐独立更新，但把 v0.5.0 的上下滑动改成纯淡入淡出（无位移）。原因是多个读数（尤其是卫星信号列表里同时有十几行）在同一时间各自滑动，观感很乱；原地淡入淡出只保留"这一位变了"的信号，视觉上更安静。
- 界面布局调整：卫星几何信息（状态胶囊/分类统计/穹顶图/图例）改为固定在顶部不随内容滚动，下方的信号列表、经纬度、速度、海拔、精度、最后更新时间卡片单独滚动——小屏（`CompactHudLayout`）和宽屏双栏布局（`WideHudLayout` 左栏）都做了同样的调整，穹顶图始终可见，不会因为下滑数据而被卷出屏幕。

### v0.5.0（2026-09-02）

- 速度同时显示公里/小时和米/秒两种单位（不是切换，而是并排常显），换算通过 `Float.metersPerSecondToKmh()`（1 m/s = 3.6 km/h）实现。
- 新增 `OdometerText`：把数值动画从"整块滑动"改成"里程表翻字"效果——每个字符位独立动画，只有真正变化的字符会翻动，没变的字符保持静止。按**字符串末位对齐**（而不是从左数第几位）实现，因为经纬度、速度这类数值更新时长度会变化（如 "9.8" → "10.2"），从左边数索引的话新增的前导位会把后面所有字符的索引都顶偏一位，导致翻动的数字是错的；从右边数距离末尾几位来定位每个字符，才能保证每一位数字的身份跟真实的十进制位置对齐，这正是机械里程表的换字方式。
- 经纬度、海拔、精度、最后更新时间（`ReadoutCard`）与速度（`SpeedSection`）统一改用 `OdometerText`，替换了之前两处各自实现的整块滑入动画（`LocationScreen.kt` 的 `AnimatedReadoutText` 已删除）。

### v0.4.1（2026-09-02）

- 修复：真机上（尤其是支持双频 GNSS 的机型，如 Fold7）打开 App 会立刻闪退。原因是 `SatelliteSignalList` 的 `LazyColumn` 用"星座+卫星编号"拼出的字符串作为列表项 key；而支持 L1+L5 双频定位的手机会把同一颗卫星拆成两条 `GnssStatus` 记录（同一星座、同一编号，只是频段不同），导致 key 重复，Compose 直接抛异常崩溃。由于此前已经授权过定位权限，App 一启动就直接渲染真实卫星数据，正好触发这个重复 key，所以表现为"一打开就退出"。开发机没有真实卫星信号，之前完全测不出来。
- 修复方式：`SatelliteSignalList` 不再使用自定义 key，改用列表默认的位置索引（保证唯一），代价是重排序时单行的滚动位置连续性略有损失，对一个只读的信号强度列表没有实际影响。

### v0.4.0（2026-09-02）

- 新增卫星信号强度列表（`SatelliteSignalList`）：按信噪比（C/N0）由强到弱排序，逐颗展示短代号（如 G12/C06）、强度条、原始 dB-Hz 数值，参与定位的卫星带 ✓ 标记；归一化换算抽到 `SignalStrength.kt`，与穹顶图共用同一套刻度。
- 卫星穹顶图的点调大（约 40%）且更易读，画布尺寸同步从 220dp 增至 248dp；参与定位解算的卫星新增"锁定光环"标记（不再只是实心/空心的细微差别），并新增 `SatelliteUsedLegend` 图例说明这个标记的含义。
- 新增自适应布局：以 600dp 屏幕宽度为断点（与 Material 的 compact/medium 断点一致），< 600dp（手机、折叠屏合上）沿用单列纵向滚动；≥ 600dp（折叠屏展开、平板）切换为左右双栏（左：状态/分类统计/穹顶图，右：信号列表 + 数值卡片），两栏各自独立滚动，解决了 v0.3.0 在宽屏上内容偏窄、底部留白过多的问题。`LocationScreen.kt` 拆分为 `CompactHudLayout` / `WideHudLayout` 两个布局分支，共用同一组数据卡片子组件，保证两种布局展示的数据字段完全一致。
- 已知后续可做但本版本未做：无障碍的"减少动效"开关、浅色主题、卫星穹顶图的位置变化动画（当前每次快照直接刷新，不做插值动画）。

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
