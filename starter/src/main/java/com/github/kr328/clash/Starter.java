package com.github.kr328.clash;

import android.util.Log;

import java.io.File;
import java.io.IOException;

public class Starter {
    private String baseDir;
    private String dataDir;

    private Starter(String baseDir, String dataDir) {
        this.baseDir = baseDir;
        this.dataDir = dataDir;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: app_process /system/bin com.github.kr328.clash.Starter [CORE-DIR] [DATA-DIR]");

            System.exit(1);
        }

        Log.i(Constants.TAG, "Starter started");

        new Starter(args[0], args[1]).exec();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private void exec() {
        //noinspection ResultOfMethodCallIgnored
        new File(dataDir).mkdirs();

        ProxySetup proxySetup = new ProxySetup(baseDir, dataDir);

        ClashRunner runner = new ClashRunner(baseDir, dataDir, new ClashRunner.Callback() {
            @Override
            public boolean onPrepare(ClashRunner runner, CachedConfigure cachedConfigure) {
                try {
                    proxySetup.execOnPrepare(cachedConfigure);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Run prepare script failure " + e.getMessage());
                    return true; // blocking start
                }
                return false; // start now
            }

            @Override
            public void onStarted(ClashRunner runner, CachedConfigure cachedConfigure) {
                Utils.deleteFiles(dataDir, "RUNNING", "STOPPED");

                try {
                    proxySetup.execOnStarted(cachedConfigure);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Run start script failure " + e.getMessage());
                    runner.stop();
                    return;
                }

                try {
                    //noinspection ResultOfMethodCallIgnored
                    new File(dataDir, "RUNNING").createNewFile();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onStopped(ClashRunner runner, CachedConfigure cachedConfigure) {
                Utils.deleteFiles(dataDir, "RUNNING", "STOPPED");

                try {
                    proxySetup.execOnStop(cachedConfigure);
                } catch (Exception e) {
                    Log.w(Constants.TAG, "Run stop script failure " + e.getMessage());
                }

                try {
                    //noinspection ResultOfMethodCallIgnored
                    new File(dataDir, "STOPPED").createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ControlObserver observer = new ControlObserver(dataDir, type -> {
            Utils.deleteFiles(dataDir, "STOP", "START", "RESTART");
            switch (type) {
                case "START":
                    runner.start();
                    break;
                case "STOP":
                    runner.stop();
                    break;
                case "RESTART":
                    runner.restart();
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
        } catch (InterruptedException ignored) {
        }
    }
}
