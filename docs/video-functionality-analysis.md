# Animeko 视频功能分析文档

本文档详细记录了 Animeko 项目中在线视频下载、播放和播放缓存功能的实现位置和架构。

## 目录

- [1. 在线视频下载功能](#1-在线视频下载功能)
- [2. 视频播放功能](#2-视频播放功能)
- [3. 视频播放缓存功能](#3-视频播放缓存功能)
- [4. 支持组件](#4-支持组件)
- [5. 架构总结](#5-架构总结)

---

## 1. 在线视频下载功能

### 1.1 HTTP/HLS 视频下载

#### HttpMediaCacheEngine
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/engine/HttpMediaCacheEngine.kt`

**关键功能** (行 58-354):
- 主要的 HTTP 视频缓存引擎实现
- 处理 HTTP 流媒体文件 (M3U8/HLS) 和直接视频文件下载
- 使用 `HttpDownloader` 下载视频片段
- 支持暂停/恢复功能 (行 311-325)
- 管理下载状态和进度跟踪 (行 206-256)

#### KtorPersistentHttpDownloader
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/engine/KtorPersistentHttpDownloader.kt`

**关键功能** (行 39-121):
- 持久化 HTTP 下载器实现
- 自动从数据库保存/恢复下载状态
- 处理 M3U8 播放列表解析和片段下载
- 支持可恢复的下载与持久化状态

### 1.2 Torrent/BT 视频下载

#### TorrentMediaCacheEngine
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/engine/TorrentMediaCacheEngine.kt`

**关键功能** (行 87-593):
- 基于 Torrent 的视频缓存引擎
- 处理磁力链接和 .torrent 文件下载
- 为每个媒体缓存创建 torrent 会话 (行 85)
- 管理对等连接和做种 (行 264-364)
- 基于剧集元数据自动选择 torrent 中的文件 (行 465-509)

### 1.3 Web 视频提取 (Android)

#### AndroidWebMediaResolver
**位置**: `app/shared/app-data/src/androidMain/kotlin/domain/media/resolver/AndroidWebMediaResolver.kt`

**关键功能** (行 49-122):
- 基于 WebView 的视频 URL 提取
- 拦截网络请求以查找视频资源 (行 226-243)
- 支持不同网站的自定义视频匹配器
- 从网页中提取 M3U8 URL

#### WebViewVideoExtractor
**位置**: `app/shared/app-data/src/androidMain/kotlin/domain/media/resolver/WebViewVideoExtractor.android.kt`

**关键功能** (行 124-253):
- Android WebView 视频提取器实现
- 加载网页并拦截视频资源请求
- 处理 cookies 和自定义 headers (行 143-148)
- 视频提取的超时处理 (行 194)

---

## 2. 视频播放功能

### 2.1 视频播放器核心

#### VideoPlayer (Desktop)
**位置**: `app/shared/video-player/src/desktopMain/kotlin/ui/VideoPlayer.desktop.kt`

**关键功能** (行 19-26):
- 使用 VLC MediampPlayer 的桌面视频播放器
- 集成 VLC 进行视频渲染

#### VideoPlayer (Common)
**位置**: `app/shared/video-player/src/commonMain/kotlin/ui/VideoPlayer.kt`

**关键功能**:
- 通用视频播放器接口和 Composable

#### VideoScaffold
**位置**: `app/shared/video-player/src/commonMain/kotlin/ui/VideoScaffold.kt`

**关键功能**:
- 带控制器和 UI 的视频播放器脚手架

### 2.2 媒体解析与流媒体

#### HttpStreamingMediaResolver
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/resolver/HttpStreamingMediaResolver.kt`

**关键功能** (行 19-44):
- HTTP 流媒体解析器
- 解析 HTTP 流媒体文件 (M3U8/HLS) 用于播放
- 创建带 headers 的 `UriMediaData` 用于流媒体 (行 41)

#### TorrentMediaResolver
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/resolver/TorrentMediaResolver.kt`

**关键功能** (行 40-277):
- 用于 BT 播放的 Torrent 媒体解析器
- 从 torrent 中选择合适的视频文件 (行 118-186)
- 处理磁力链接和 torrent 文件 (行 44-46)
- 基于剧集元数据和黑名单过滤的智能文件选择 (行 99-112)

### 2.3 媒体获取

#### MediaFetcher
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/fetch/MediaFetcher.kt`

**关键功能** (行 80-443):
- 从多个来源并行获取媒体
- 管理带进度跟踪的获取会话 (行 331-430)
- 处理重试和错误状态 (行 242-288)
- 合并来自多个媒体源的结果

### 2.4 视频加载状态

#### VideoLoadingState
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/player/VideoLoadingState.kt`

**关键功能** (行 18-59):
- 视频加载状态定义
- 状态: Initial, ResolvingSource, DecodingData, Succeed, Failed
- 处理不同的失败场景 (超时、网络错误、不支持的媒体)

---

## 3. 视频播放缓存功能

### 3.1 缓存管理

#### MediaCacheManager
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/MediaCacheManager.kt`

**关键功能** (行 29-152):
- 中央缓存管理器
- 按主题/剧集列出和过滤缓存 (行 45-54)
- 跟踪缓存状态 (NotCached, Caching, Cached) 和进度 (行 60-94)
- 管理缓存生命周期 (删除、关闭操作)

#### MediaCache
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/MediaCache.kt`

**关键功能**:
- 媒体缓存接口，包含文件统计和会话统计

### 3.2 缓存进度提供器

#### MediaCacheProgressProvider
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/player/MediaCacheProgressProvider.kt`

**关键功能** (行 21-96):
- 视频播放器进度条缓存可视化
- 提供基于块的进度信息 (行 26-54)
- 块状态: NONE, DOWNLOADING, DONE, NOT_AVAILABLE (行 56-76)
- 用于在播放器 UI 中显示已缓冲/已缓存的片段

#### TorrentMediaCacheProgressProvider
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/player/TorrentMediaCacheProgressProvider.kt`

**关键功能**:
- 用于 BT 下载的 Torrent 特定缓存进度提供器

### 3.3 缓存存储

#### MediaCacheStorage
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/storage/MediaCacheStorage.kt`

**关键功能**:
- 媒体缓存的抽象存储接口

#### HttpMediaCacheStorage
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/storage/HttpMediaCacheStorage.kt`

**关键功能**:
- HTTP 特定的缓存存储实现

#### TorrentMediaCacheStorage
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/storage/TorrentMediaCacheStorage.kt`

**关键功能**:
- Torrent 特定的缓存存储实现

### 3.4 缓存请求器

#### EpisodeCacheRequester
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/requester/EpisodeCacheRequester.kt`

**关键功能**:
- 管理剧集的缓存请求
- 处理缓存创建和生命周期

#### CacheRequestStage
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/cache/requester/CacheRequestStage.kt`

**关键功能**:
- 定义缓存请求阶段

---

## 4. 支持组件

### 4.1 媒体数据提供器

#### MediaDataProvider
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/player/data/MediaDataProvider.kt`

**关键功能**:
- 向播放器提供媒体数据的抽象接口

#### TorrentMediaData
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/player/data/TorrentMediaData.kt`

**关键功能**:
- 用于播放的 Torrent 特定媒体数据

#### DownloadingMediaData
**位置**: `app/shared/app-data/src/commonMain/kotlin/domain/media/player/data/DownloadingMediaData.kt`

**关键功能**:
- 用于下载/流媒体内容的媒体数据

### 4.2 UI 组件

#### EpisodeVideoLoadingIndicator
**位置**: `app/shared/src/commonMain/kotlin/ui/subject/episode/video/loading/EpisodeVideoLoadingIndicator.kt`

**关键功能**:
- 视频播放的加载指示器

#### VideoLoadingIndicator
**位置**: `app/shared/video-player/src/commonMain/kotlin/ui/VideoLoadingIndicator.kt`

**关键功能**:
- 通用视频加载指示器

---

## 5. 架构总结

Animeko 视频系统构建在多层架构之上：

### 5.1 下载层
- 支持 HTTP/HLS (M3U8)、直接视频文件和 BitTorrent 下载
- 具有持久化状态管理
- 使用 Anitorrent 引擎处理 BT，HttpDownloader 处理 HTTP/HLS

### 5.2 解析层
- 将各种媒体源 (HTTP 流媒体、torrents、web 视频) 解析为可播放格式
- 支持自定义视频匹配器和提取器

### 5.3 缓存层
- 管理已下载的内容，具有进度跟踪
- 基于块的缓冲和存储管理
- 支持不同的存储后端 (HTTP、Torrent)

### 5.4 播放层
- 基于 VLC 的视频播放器
- 全面的 UI 控制和状态管理
- 支持多种媒体格式

### 5.5 获取层
- 从多个媒体源并行获取
- 重试逻辑和错误处理
- 会话管理和进度跟踪

### 5.6 关键技术栈
- **Kotlin Multiplatform**: 跨平台代码共享
- **Compose Multiplatform**: UI 框架
- **VLC MediampPlayer**: 视频渲染引擎
- **Ktor**: HTTP 客户端
- **Anitorrent**: BitTorrent 引擎

---

## 6. 相关文档

**位置**: `docs/contributing/code/media/media-cache.md`

该文档解释了三种类型的缓存：
- BT 缓存
- HTTP 视频文件缓存
- HLS 流媒体缓存

---

## 更新日志

- **2026-01-24**: 初始文档创建，记录视频下载、播放和缓存功能的完整架构
