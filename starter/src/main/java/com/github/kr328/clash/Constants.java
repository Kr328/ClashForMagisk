package com.github.kr328.clash;

import java.util.regex.Pattern;

class Constants {
    static final String TAG = "Clash";

    static final String CLASH_UID = String.valueOf(UserGroupIds.USER_RADIO);
    static final String CLASH_GID = String.valueOf(UserGroupIds.GROUP_RADIO);
    static final String CLASH_GROUPS = UserGroupIds.GROUP_INET + "," + UserGroupIds.GROUP_SDCARD_RW;

    static final String STARTER_COMMAND_TEMPLATE = "{BASE_DIR}/setuidgid {UID} {GID} {GROUPS} {BASE_DIR}/clash -d {DATA_DIR} 2>&1";

    static final Pattern PATTERN_CLASH_PID = Pattern.compile("\\s*PID=\\[(\\d+)]\\s*");
}
