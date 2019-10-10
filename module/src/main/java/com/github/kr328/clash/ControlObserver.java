package com.github.kr328.clash;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;

public class ControlObserver {
    public interface Callback {
        void onUserControl(String type);
    }

    private Callback callback;
    private File dataDir;
    private FileObserver fileObserver;

    ControlObserver(String data, Callback callback) {
        this.dataDir = new File(data);
        this.callback = callback;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if ( fileObserver != null )
            fileObserver.stopWatching();
    }

    void start() {
        restart();
    }

    private void restart() {
        //noinspection ResultOfMethodCallIgnored
        dataDir.mkdirs();

        fileObserver = new FileObserver(dataDir.getAbsolutePath(), FileObserver.CREATE | FileObserver.DELETE_SELF | FileObserver.MOVED_FROM | FileObserver.MOVED_TO ) {
            @Override
            public void onEvent(int event, String file) {
                Log.d(Constants.TAG, "Control Directory Changed " + file);

                if (( event & FileObserver.DELETE_SELF ) != 0 ) {
                    restart();
                }
                else {
                    switch (file.toUpperCase()) {
                        case "STOP":
                        case "START":
                        case "RESTART":
                            Utils.deleteFiles(dataDir.getAbsolutePath(), "STOP", "START", "RESTART");
                            callback.onUserControl(file);
                        }
                }
            }
        };

        fileObserver.startWatching();
    }
}
