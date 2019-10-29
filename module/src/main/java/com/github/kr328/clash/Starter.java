package com.github.kr328.clash;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Starter {
    private String baseDir;
    private String dataDir;

    private Starter(String baseDir, String dataDir) {
        this.baseDir = baseDir;
        this.dataDir = dataDir;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private void exec() {
        //noinspection ResultOfMethodCallIgnored
        new File(dataDir).mkdirs();

        AtomicBoolean restart = new AtomicBoolean(false);
        
        ProxySetup proxySetup = new ProxySetup(baseDir, dataDir);

        ClashRunner runner = new ClashRunner(baseDir, dataDir, new ClashRunner.Callback() {
            @Override
            public boolean onPrepare(ClashRunner runner, StarterConfigure starter, ClashConfigure clash) {
                try {
                    proxySetup.execOnPrepare(starter, clash);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Run prepare script failure " + e.getMessage());
                    return true; // blocking start
                }
                return false; // start now
            }

            @Override
            public void onStarted(ClashRunner runner, StarterConfigure starter ,ClashConfigure clash) {
                Utils.deleteFiles(dataDir, "RUNNING", "STOPPED");

                try {
                    proxySetup.execOnStarted(starter, clash);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Run start script failure " + e.getMessage());
                    runner.stop();
                    return;
                }

                try {
                    //noinspection ResultOfMethodCallIgnored
                    new File(dataDir, "RUNNING").createNewFile();
                } catch (Exception ignored) {}
            }

            @Override
            public void onStopped(ClashRunner runner, StarterConfigure starter, ClashConfigure clash) {
                Utils.deleteFiles(dataDir, "RUNNING", "STOPPED");

                try {
                    proxySetup.execOnStop(starter, clash);
                } catch (IOException e) {
                    Log.w(Constants.TAG, "Run stop script failure " + e.getMessage());
                }

                try {
                    //noinspection ResultOfMethodCallIgnored
                    new File(dataDir, "STOPPED").createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if ( restart.getAndSet(false) ) {
                    runner.start();
                }
            }
        });

        ControlObserver observer = new ControlObserver(dataDir, type -> {
            switch (type) {
                case "START":
                    runner.start();
                    break;
                case "STOP":
                    runner.stop();
                    break;
                case "RESTART":
                    restart.set(true);
                    runner.stop();
                    break;
           }
        });

        Utils.deleteFiles(dataDir, "RUNNING", "STOPPED");

        try {
            //noinspection ResultOfMethodCallIgnored
            new File(dataDir, "STOPPED").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        observer.start();
        runner.start();

        try {
            synchronized (this) {
                this.wait();
            }
        }
        catch (InterruptedException ignored) {}
    }

    public static void main(String[] args) {
        if ( args.length != 2 ) {
            System.err.println("Usage: app_process /system/bin com.github.kr328.clash.Starter [CORE-DIR] [DATA-DIR]");

            System.exit(1);
        }

        Log.i(Constants.TAG, "Starter started");

        Utils.waitForUserUnlocked();

        new Starter(args[0], args[1]).exec();
    }
}
