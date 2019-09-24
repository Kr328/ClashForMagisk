package com.github.kr328.clash.runner;

import android.util.Log;
import com.github.kr328.clash.Constants;

import java.io.*;
import java.util.regex.Matcher;

public class ClashProcess {
    public enum Status {
        STARTING, STARTED, STOPPED, KILLING
    }

    public interface Callback {
        void onStatusChanged(Status status);
    }

    private Callback callback;
    private Status status;
    private Process process;
    private int pid;

    public ClashProcess(Callback callback) {
        this.callback = callback;
    }

    synchronized public void start(String baseDir, String dataDir) {
        if (process != null)
            return;

        this.setStatus(Status.STARTING);

        try {
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
                            pid = Integer.parseInt(matcher.group(1));
                            break;
                        }
                    }

                    this.setStatus(Status.STARTED);

                    while ((line = reader.readLine()) != null)
                        Log.i(Constants.TAG, line);

                    reader.close();

                    synchronized (ClashProcess.this) {
                        process = null;
                    }
                } catch (IOException e) {
                    Log.i(Constants.TAG, "Clash stdout closed");
                }

                this.setStatus(Status.STOPPED);
            }).start();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Start clash process failure", e);
        }

        Log.i(Constants.TAG, "Clash started");
    }

    synchronized public void kill() {
        if (process == null)
            return;

        this.setStatus(Status.KILLING);

        android.os.Process.killProcess(pid);
    }

    synchronized public Status getStatus() {
        return status;
    }

    private void setStatus(Status status) {
        this.status = status;
        callback.onStatusChanged(status);
    }
}
