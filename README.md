# TV Android 应用

基于 CatVod 的开源 Android 影音应用程序，同时支持 **Android TV 大屏幕**与**手机**两种使用场景。

## GitHub Actions 自动构建

本项目配置了 GitHub Actions Workflow，可以自动编译 Release 版本的 APK。

### 触发构建的方式

1. **推送到 main/master 分支**：每当代码推送到 main 或 master 分支时，会自动触发构建
2. **创建 Tag**：创建 Git Tag 时会自动构建并发布 Release
3. **手动触发**：在 GitHub Actions 页面手动运行 workflow

### 构建产物

Workflow 会构建以下 APK：

- **TV (Leanback)**:
  - `leanback-arm64_v8a-release.apk` - 适用于 arm64 架构设备
  - `leanback-armeabi_v7a-release.apk` - 适用于 armeabi 架构设备

- **Mobile**:
  - `mobile-arm64_v8a-release.apk` - 适用于 arm64 架构设备
  - `mobile-armeabi_v7a-release.apk` - 适用于 armeabi 架构设备

### 配置签名密钥（可选）

如果需要发布签名的 Release APK，需要配置以下 GitHub Secrets：

1. 在 GitHub 仓库 Settings → Secrets and variables → Actions 中添加以下 Secrets：

   | Secret 名称 | 说明 | 示例 |
   |------------|------|------|
   | `SIGNING_STORE_FILE` | Base64 编码的 keystore 文件 | `$(base64 -w0 your-keystore.jks)` |
   | `SIGNING_KEY_ALIAS` | Key alias 名称 | `your-key-alias` |
   | `SIGNING_STORE_PASSWORD` | Keystore 密码 | `your-store-password` |
   | `SIGNING_KEY_PASSWORD` | Key 密码 | `your-key-password` |

2. 生成 Secrets 的方法：

   ```bash
   # 将 keystore 文件转为 Base64
   base64 -w0 your-keystore.jks | pbcopy  # macOS
   base64 -w0 your-keystore.jks           # Linux

   # 在 GitHub Secrets 中填入生成的 Base64 字符串
   ```

### 本地构建

```bash
# 克隆项目
git clone https://github.com/m3325599/TV.git
cd TV

# 构建 TV 版本（arm64_v8a）
./gradlew assembleLeanbackArm64_v8aRelease

# 构建 TV 版本（armeabi_v7a）
./gradlew assembleLeanbackArmeabi_v7aRelease

# 构建手机版本（arm64_v8a）
./gradlew assembleMobileArm64_v8aRelease

# 构建手机版本（armeabi_v7a）
./gradlew assembleMobileArmeabi_v7aRelease
```

### 发布版本

1. 创建并推送 Tag：

   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. GitHub Actions 会自动：
   - 构建所有 APK
   - 创建 GitHub Release
   - 上传所有 APK 作为 Release 附件

### 注意事项

- 编译 SDK 版本：37
- 最低支持 Android 版本：7.0 (API 24)
- Java 版本：21
- 项目使用 Chaquopy 支持 Python 爬虫
- Release 构建需要签名配置，否则生成的 APK 将是未签名状态
