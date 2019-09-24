package com.github.kr328.clash;

import android.util.Log;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.util.regex.Matcher;

class ClashRunner {
    interface Callback {
        void onStarted(ClashRunner runner, StarterConfigure starter, ClashConfigure clash);
        void onStopped(ClashRunner runner, StarterConfigure starter, ClashConfigure clash);
   }

    private String baseDir;
    private String dataDir;
    private Process process;
    private Callback callback;

    private StarterConfigure starterConfigure;
    private ClashConfigure clashConfigure;
    private int pid;

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
                starterConfigure = StarterConfigure.loadFromFile(new File(dataDir + "/starter.yaml"));

                if ( new File(dataDir + "/config.yaml").exists() ) {
                    clashConfigure = ClashConfigure.loadFromFile(new File(dataDir + "/config.yaml"));
                }
                else if ( new File(dataDir + "/config.yml").exists() ) {
                    clashConfigure = ClashConfigure.loadFromFile(new File(dataDir + "/config.yml"));
                }
                else {
                    throw new FileNotFoundException("Clash config file not found");
                }
            }
            catch (IOException|YAMLException e) {
                Log.e(Constants.TAG, "Unable to start clash", e);
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
                        if ( matcher.matches() ) {
                            pid = Integer.parseInt(matcher.group(1));
                            break;
                        }
                    }

                    while ((line = reader.readLine()) != null)
                        Log.i(Constants.TAG, line);

                    reader.close();

                    synchronized (ClashRunner.this) {
                        process = null;

                        callback.onStopped(this, starterConfigure, clashConfigure);
                    }
                } catch (IOException e) {
                    Log.i(Constants.TAG, "Clash stdout closed");
                }
            }).start();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Start clash process failure", e);
        }

        Log.i(Constants.TAG, "Clash started");
        callback.onStarted(this, starterConfigure, clashConfigure);
    }

    synchronized void stop() {
        if (process == null)
            return;

        android.os.Process.killProcess(pid);
    }

}
