package com.github.kr328.clash;

import android.util.Log;
import com.github.kr328.clash.configure.ClashConfigure;
import com.github.kr328.clash.configure.StarterConfigure;
import com.github.kr328.clash.runner.ClashRunner;

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
            public void onStarted(ClashRunner runner, StarterConfigure starter , ClashConfigure clash) {
                Utils.deleteFiles(dataDir, "RUNNING", "STOPPED");
                
                proxySetup.execOnStarted(starter, clash);
                
                try {
                    //noinspection ResultOfMethodCallIgnored
                    new File(dataDir, "RUNNING").createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStopped(ClashRunner runner, StarterConfigure starter, ClashConfigure clash) {
                Utils.deleteFiles(dataDir, "RUNNING", "STOPPED");
                
                proxySetup.execOnStop(starter, clash);
                
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
