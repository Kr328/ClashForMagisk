# Clash for Magisk

A rule-based tunnel in Go. This module is wrapper for [clash](https://github.com/Dreamacro/clash) 

[中文说明](README_zh.md)

## Requirements

* arm64 ABI

## License

See also [NOTICE](NOTICE) and [LICENSE](LICENSE)  

## Feature

See also https://github.com/Dreamacro/clash



## Configure

Data Path  `{InternalStorage}/Android/data/com.github.kr328.clash`

In data directory

* Clash configure file `config.yaml`
* Clash GEOIP database `Country.mmdb`
* Clash starter configure file `starter.yaml` \[[example](https://github.com/Kr328/ClashForMagisk/blob/master/module/src/main/raw/magisk/core/starter.yaml)\]
* Clash status file `RUNNING` or `STOPPED`
* Custom proxy mode directory `mode.d`


## Control

Data Path  `{InternalStorage}/Android/data/com.github.kr328.clash`

Create the following file to control clash

* `START` - Start clash if stopped
* `STOP` - Stop clash if running
* `RESTART` - Restart clash 



## Read logs

* On PC

  Run command

  `adb logcat -s Clash`

* On Android

  Run command

  `logcat -s Clash`



## Custom Proxy Mode

Custom Mode Directory `{InternalStorage}/Android/data/com.github.kr328.clash/mode.d` 

1. Create directory with mode name

2. Create script `on-start.sh` and `on-stop.sh`

   Example for thus script [link](module/src/main/raw/magisk/core/mode.d/)

3. Change `mode` in `starter.yaml` 



## Build

1. Install JDK, Android SDK, Android NDK, Go, Git   

2. Clone repo
   
   ```bash
   git clone https://github.com/Kr328/ClashForMagisk && cd ClashForMagisk
   ```

3. Initialize git submodule

   ```bash
   git submodule init
   git submodule upate
   ```

4. Create `local.properties` on project root directory  
   ```properties
   sdk.dir=/path/to/android-sdk
   ndk.dir=/path/to/android-ndk
   ```

5. Run command   
   ```bash
   ./gradlew build --parallel
   ```

6. Get clash-for-magisk.zip on module/build/outputs  
