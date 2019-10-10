package com.github.kr328.clash;

import android.util.Log;

import java.io.IOException;
import java.net.URL;

public class UserDataThread extends Thread {
    private String dataDir;
    private StarterConfigure starterConfigure;
    private ClashConfigure clashConfigure;

    UserDataThread(String dataDir, StarterConfigure starterConfigure, ClashConfigure clashConfigure) {
        this.dataDir = dataDir;
        this.starterConfigure = starterConfigure;
        this.clashConfigure = clashConfigure;
    }

    @Override
    public void run() {
        if ( clashConfigure.externalController == null )
            return;

        int count = 30;
        while ( !isInterrupted() && count > 0 ) {
            try { sleep(5000); } catch (InterruptedException ignored) { }

            try {
                if ( SimpleHttpClient.get(new URL("http://" + clashConfigure)) == null )
                    return;
            } catch (IOException ignored) {}

            count--;
        }

        if ( count <= 0 ) {
            Log.e(Constants.TAG, "Clash start timeout");
            return;
        }


    }
}
