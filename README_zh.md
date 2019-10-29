# Clash for Magisk

A rule-based tunnel in Go. This module is wrapper for [clash](https://github.com/Dreamacro/clash) 

[English](README.md)

## 需求

* arm64 指令集


## 开源协议

参见 [NOTICE](NOTICE) 和 [LICENSE](LICENSE)  


## 功能

参见 https://github.com/Dreamacro/clash



## 配置

数据目录 `{内置存储根目录}/Android/data/com.github.kr328.clash`

数据目录包含以下文件

* Clash 配置文件 `config.yaml`
* Clash GEOIP 数据库 `Country.mmdb`
* Clash 启动器 配置文件  `starter.yaml` \[[样例](https://github.com/Kr328/ClashForMagisk/blob/master/module/src/main/raw/magisk/core/starter.yaml)\]
* Clash 状态文件 `RUNNING` 或者 `STOPPED`



## 控制

数据目录  `{内置存储根目录}/Android/data/com.github.kr328.clash`

在数据目录创建以下文件以控制

* `START` - 启动 Clash
* `STOP` - 停止 Clash
* `RESTART` - 重新启动 clash 



## 读取日志

* 在 PC 上

  运行命令

  `adb logcat -s Clash`

* 在 Android 上

  运行命令

  `logcat -s Clash`



## 自定义代理模式

自定义代理模式路径 `{内置存储目录}/Android/data/com.github.kr328.clash/mode.d` 

1. 创建名称为 **自定义代理模式** 的目录

2. 创建脚本 `on-start.sh` 和 `on-stop.sh`

   例子 [link](module/src/main/raw/magisk/core/mode.d/)

3. 更改 `mode` 在 `starter.yaml` 




## 构建

1. 安装 JDK ,Gradle ,Android SDK ,Android NDK  

2. 创建 `local.properties` 在工程根目录  
   ```properties
   sdk.dir=/path/to/android-sdk
   ndk.dir=/path/to/android-ndk
   cmake.dir=/path/to/android-cmake/*version*
   ```

3. 运行命令  
   ```bash
   ./gradlew build
   ```

4. 从 module/build/outputs 获取 clash-for-magisk.zip 

## 反馈

Telegram 群组 [Kr328 Magisk Modules](https://t.me/kr328_magisk_modules)

