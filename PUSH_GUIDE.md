# GitHub Actions Workflow 推送指南

## 当前状态

✅ **本地已准备好**：
- Workflow 文件已创建：`.github/workflows/build.yml`
- `.gitignore` 已修复
- `build.gradle` 已修改
- 本地提交已完成：commit `dee80ff`

❌ **推送失败原因**：需要 GitHub 认证

---

## 解决方案

### 方案 1：你手动推送（推荐）

在你的本地终端执行：

```bash
cd /path/to/your/TV
git pull https://github.com/m3325599/TV.git fongmi
git push origin fongmi
```

### 方案 2：手动创建 Workflow 文件

如果推送仍然失败，你可以直接在 GitHub 上创建文件：

1. **访问你的仓库**：https://github.com/m3325599/TV

2. **创建目录结构**：
   - 点击 "Add file" → "Create new file"
   - 输入路径：`.github/workflows/build.yml`

3. **复制以下内容**：

```yaml
name: Build APK

on:
  push:
    branches: [ main, master, fongmi ]
  pull_request:
    branches: [ main, master, fongmi ]
  workflow_dispatch:
    inputs:
      build_type:
        description: 'Build type (debug does not require signing)'
        required: false
        default: 'debug'
        type: choice
        options:
          - debug
          - release

env:
  JAVA_VERSION: '21'

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        flavor: [leanback, mobile]
        abi: [arm64_v8a, armeabi_v7a]
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'gradle'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Create local.properties
        run: |
          echo "sdk.dir=$ANDROID_HOME" > local.properties

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build ${{ matrix.flavor }}-${{ matrix.abi }} APK
        run: |
          ./gradlew assemble${{ matrix.flavor }}${{ matrix.abi }}${{ inputs.build_type || 'debug' }} --no-daemon --stacktrace
        env:
          JAVA_OPTS: -Xmx4g

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: tv-${{ matrix.flavor }}-${{ matrix.abi }}-${{ inputs.build_type || 'debug' }}
          path: |
            app/build/outputs/apk/${{ matrix.flavor }}/${{ matrix.abi }}/${{ inputs.build_type || 'debug' }}/*.apk
          retention-days: 7

  release:
    needs: build
    runs-on: ubuntu-latest
    if: startsWith(github.ref, 'refs/tags/') && (inputs.build_type == 'release' || inputs.build_type == '')
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Download all artifacts
        uses: actions/download-artifact@v4
        with:
          path: ./apks

      - name: Display structure of downloaded files
        run: ls -laR ./apks

      - name: Create Release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ github.ref }}
          name: TV Release ${{ github.ref }}
          draft: false
          prerelease: false
          generate_release_notes: true
          files: |
            ./apks/**/*.apk
```

4. **点击 "Commit new file"**

---

### 方案 3：修复 build.gradle

还需要修改 `app/build.gradle`，让签名配置可选：

在 GitHub 上编辑 `app/build.gradle` 文件，找到 `signingConfigs` 部分，修改为：

```groovy
def localProps = new Properties()
if (rootProject.file('local.properties').exists()) {
    rootProject.file('local.properties').withInputStream {
        localProps.load(it)
    }
}

def hasSigningConfig = localProps['storeFile'] != null

signingConfigs {
    if (hasSigningConfig) {
        release {
            storeFile file(localProps['storeFile'])
            keyAlias localProps['keyAlias']
            keyPassword localProps['storePassword']
            storePassword localProps['storePassword']
        }
    }
}

buildTypes {
    debug {
        minifyEnabled = false
        shrinkResources = false
    }
    release {
        minifyEnabled = true
        shrinkResources = true
        if (hasSigningConfig) {
            signingConfig = signingConfigs.release
        }
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro', 'proguard-rules-media.pro'
    }
}
```

---

## 推送成功后

1. **访问 Actions 页面**：https://github.com/m3325599/TV/actions

2. **查看 workflow 运行状态**

3. **下载构建的 APK**

---

## 需要帮助？

如果仍有问题，请告诉我：
- 你使用的是什么方式推送代码？
- 是否有 GitHub CLI (`gh`)？
- 是否配置了 SSH key？

我可以提供更具体的解决方案。