package com.github.kr328.clash;

import android.util.Log;

import java.io.File;

class Utils {
    static void waitForUserUnlocked() {
        File file = new File("/sdcard/Android");

        while ( !file.isDirectory() ) {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            Log.i(Constants.TAG, "Wait 1s for user unlock");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void deleteFiles(String baseDir, String... files) {
        for ( String f : files )
            new File(baseDir, f).delete();
    }
}
