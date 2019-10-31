#!/system/bin/sh
# Please don't hardcode /magisk/modname/... ; instead, please use $MODDIR/...
# This will make your scripts compatible even if Magisk change its mount point in the future
MODDIR=${0%/*}

# This script will be executed in late_start service mode
# More info in the main Magisk thread

ROOT="/dev/clash_root"

CORE_INTERNAL_DIR="$MODDIR/core"

CORE_DIR="$ROOT/core"
DATA_DIR="/sdcard/Android/data/com.github.kr328.clash"

mkdir -p "$ROOT"
mkdir -p "$CORE_DIR"

mount -o bind "$CORE_INTERNAL_DIR" "$CORE_DIR"

while [[ ! -d "/sdcard/Android" ]];do
    sleep 1
done

mkdir -p "$DATA_DIR"

CLASSPATH="$CORE_DIR/starter.dex" ${CORE_DIR}/daemonize /system/bin/app_process /system/bin --nice-name=clash_starter com.github.kr328.clash.Starter "$CORE_DIR" "$DATA_DIR"
