package com.github.kr328.clash;

import android.util.Log;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.util.Objects;
import java.util.regex.Matcher;

class ClashRunner {
    private String baseDir;
    private String dataDir;
    private Process process;
    private Callback callback;
    private CachedConfigure cachedConfigure;
    private int pid;
    private boolean restart;

    ClashRunner(String baseDir, String dataDir, Callback callback) {
        this.baseDir = baseDir;
        this.dataDir = dataDir;
        this.callback = callback;
    }

    synchronized void start() {
        if (process != null)
            return;

        try {
            try {
                StarterConfigure starterConfigure = Utils.loadOrRenderStarter(baseDir, dataDir);

                ClashConfigure clashConfigure;
                if (new File(dataDir + "/config.yaml").exists()) {
                    clashConfigure = ClashConfigure.loadFromFile(new File(dataDir + "/config.yaml"));
                } else if (new File(dataDir + "/config.yml").exists()) {
                    clashConfigure = ClashConfigure.loadFromFile(new File(dataDir + "/config.yml"));
                } else {
                    throw new FileNotFoundException("Clash config file not found");
                }

                cachedConfigure = new CachedConfigure(starterConfigure, clashConfigure);
            } catch (IOException | YAMLException e) {
                Log.e(Constants.TAG, "Unable to start clash", e);
                restart = false;
                return;
            }

            if (callback.onPrepare(this, cachedConfigure)) {
                restart = false;
                return;
            }

            String command = Constants.STARTER_COMMAND_TEMPLATE
                    .replace("{BASE_DIR}", baseDir)
                    .replace("{DATA_DIR}", dataDir)
                    .replace("{UID}", Constants.CLASH_UID)
                    .replace("{GID}", Constants.CLASH_GID)
                    .replace("{GROUPS}", Constants.CLASH_GROUPS);

            Log.d(Constants.TAG, "Starting clash " + command);

            process = Runtime.getRuntime().exec("/system/bin/sh");

            process.getOutputStream().write(("echo PID=[$$]\n").getBytes());
            process.getOutputStream().write(("exec " + command + "\n").getBytes());
            process.getOutputStream().flush();

            new Thread(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                try {
                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = Constants.PATTERN_CLASH_PID.matcher(line);
                        if (matcher.matches()) {
                            pid = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                            break;
                        }
                    }

                    Log.i(Constants.TAG, "Clash started");
                    callback.onStarted(this, cachedConfigure);

                    while ((line = reader.readLine()) != null)
                        Log.i(Constants.TAG, line);

                    reader.close();

                    synchronized (ClashRunner.this) {
                        process = null;

                        callback.onStopped(this, cachedConfigure);

                        if (restart) {
                            restart = false;
                            this.start();
                        }
                    }
                } catch (IOException e) {
                    Log.i(Constants.TAG, "Clash stdout closed");

                    restart = false;
                }
            }).start();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Start clash process failure", e);

            restart = false;
        }
    }

    synchronized void stop() {
        if (process == null)
            return;

        android.os.Process.killProcess(pid);
    }

    synchronized void restart() {
        if (process == null)
            start();
        else {
            restart = true;
            stop();
        }
    }

    interface Callback {
        boolean onPrepare(ClashRunner runner, CachedConfigure cachedConfigure);

        void onStarted(ClashRunner runner, CachedConfigure cachedConfigure);

        void onStopped(ClashRunner runner, CachedConfigure cachedConfigure);
    }
}
