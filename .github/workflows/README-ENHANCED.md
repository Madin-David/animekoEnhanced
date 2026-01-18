# AnimekoEnhanced GitHub Actions 自动构建配置

本项目已配置 GitHub Actions 自动构建系统，可以自动编译生成各平台的可安装文件。

## 📦 支持的平台

- **Windows** - x64 MSI 安装包
- **macOS** - ARM64 (Apple Silicon) DMG 镜像
- **Linux** - x64 AppImage 便携应用
- **Android** - ARM64 和 Universal APK

## 🚀 工作流说明

### 1. 构建工作流 (build-enhanced.yml)

**触发条件：**
- 推送到 `main` 或 `develop` 分支
- 创建 Pull Request
- 手动触发 (workflow_dispatch)

**功能：**
- 自动编译所有平台的应用程序
- 将构建产物上传为 GitHub Artifacts
- Artifacts 保留 30 天

**查看构建产物：**
1. 进入 GitHub 仓库的 Actions 页面
2. 选择对应的工作流运行
3. 在页面底部的 "Artifacts" 区域下载编译好的文件

### 2. 发布工作流 (release-enhanced.yml)

**触发条件：**
- 推送 tag（格式：`v*`，例如 `v1.0.0`）
- 手动触发

**功能：**
- 自动创建 GitHub Release（草稿状态）
- 编译所有平台的发布版本
- 自动上传到 Release 页面
- 生成下载链接和更新说明

## 📝 使用方法

### 开发构建

每次推送代码到 `main` 或 `develop` 分支时，会自动触发构建：

```bash
git add .
git commit -m "feat: 添加新功能"
git push origin main
```

构建完成后，可以在 Actions 页面下载编译好的文件进行测试。

### 发布新版本

1. **更新版本号**（如果需要）

2. **创建并推送 tag：**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

3. **等待自动构建：**
   - GitHub Actions 会自动开始构建
   - 构建完成后会创建一个草稿 Release
   - 所有平台的安装包会自动上传

4. **发布 Release：**
   - 进入 GitHub 仓库的 Releases 页面
   - 找到自动创建的草稿 Release
   - 编辑更新说明（如果需要）
   - 点击 "Publish release" 发布

### 手动触发构建

如果需要手动触发构建：

1. 进入 GitHub 仓库的 Actions 页面
2. 选择对应的工作流
3. 点击 "Run workflow" 按钮
4. 选择分支并确认

## 🔧 配置说明

### 构建时间

- Windows: 约 30-60 分钟
- Linux + Android: 约 60-90 分钟
- macOS: 约 60-90 分钟

### 资源要求

工作流使用 GitHub 提供的免费 Runner：
- `windows-2022` - Windows 构建
- `ubuntu-24.04` - Linux 和 Android 构建
- `macos-14` - macOS 构建

### 自定义配置

如果需要修改构建配置，可以编辑以下文件：
- `.github/workflows/build-enhanced.yml` - 开发构建配置
- `.github/workflows/release-enhanced.yml` - 发布构建配置

## 📋 构建产物命名规则

### 开发构建 (Artifacts)
- `animekoenhanced-windows-x64` - Windows 便携版
- `animekoenhanced-linux-x64-appimage` - Linux AppImage
- `animekoenhanced-macos-aarch64` - macOS App
- `animekoenhanced-android-arm64-v8a` - Android ARM64 APK
- `animekoenhanced-android-universal` - Android Universal APK

### 发布版本 (Release)
- `animekoenhanced-{version}-windows-x64.msi` - Windows 安装包
- `animekoenhanced-{version}-linux-x64.AppImage` - Linux AppImage
- `animekoenhanced-{version}-macos-aarch64.dmg` - macOS 镜像
- `animekoenhanced-{version}-android-arm64-v8a.apk` - Android ARM64
- `animekoenhanced-{version}-android-universal.apk` - Android Universal

## ⚠️ 注意事项

1. **首次构建**可能需要较长时间，因为需要下载依赖
2. **Android 签名**：发布版本需要配置签名密钥（可选）
3. **macOS 签名**：如果需要分发，建议配置 Apple 开发者证书（可选）
4. **构建失败**：查看 Actions 日志排查问题

## 🔐 密钥配置（可选）

如果需要配置签名等功能，可以在 GitHub 仓库设置中添加以下 Secrets：

### Android 签名
- `SIGNING_RELEASE_STOREFILE` - 签名密钥文件（Base64 编码）
- `SIGNING_RELEASE_STOREPASSWORD` - 密钥库密码
- `SIGNING_RELEASE_KEYALIAS` - 密钥别名
- `SIGNING_RELEASE_KEYPASSWORD` - 密钥密码

### 其他配置
- `DANDANPLAY_APP_ID` - 弹弹play API ID
- `DANDANPLAY_APP_SECRET` - 弹弹play API Secret
- `SENTRY_DSN` - Sentry 错误追踪 DSN

## 📚 参考资料

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Gradle 构建文档](https://docs.gradle.org/)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

## 🤝 贡献

如果发现构建配置有问题或需要改进，欢迎提交 Issue 或 Pull Request。
