package com.github.kr328.clash

import android.os.Process

object UserGroupIds {
    val USER_RADIO = Process.getUidForName("radio")
    val GROUP_RADIO = Process.getGidForName("radio")
    val GROUP_INET = Process.getGidForName("inet")
    val GROUP_SDCARD_RW = Process.getGidForName("sdcard_rw")
}