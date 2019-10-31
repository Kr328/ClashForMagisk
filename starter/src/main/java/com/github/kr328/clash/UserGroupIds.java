package com.github.kr328.clash;

class UserGroupIds {
    static final int USER_RADIO = android.os.Process.getUidForName("radio");

    static final int GROUP_RADIO = android.os.Process.getGidForName("radio");
    static final int GROUP_INET = android.os.Process.getGidForName("inet");
    static final int GROUP_SDCARD_RW = android.os.Process.getGidForName("sdcard_rw");
}
