package com.github.kr328.clash;

public class UserGroupIds {
    public static final int USER_ROOT = 0;
    public static final int USER_RADIO = android.os.Process.getUidForName("radio");

    public static final int GROUP_RADIO = android.os.Process.getGidForName("radio");
    public static final int GROUP_INET = android.os.Process.getGidForName("inet");
    public static final int GROUP_SDCARD_RW = android.os.Process.getGidForName("sdcard_rw");
}
