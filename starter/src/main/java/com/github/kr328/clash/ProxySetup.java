package com.github.kr328.clash;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

class ProxySetup {
    private String dataDir;
    private String baseDir;

    ProxySetup(String baseDir, String dataDir) {
        this.dataDir = dataDir;
        this.baseDir = baseDir;
    }

    void execOnPrepare(CachedConfigure cachedConfigure) throws Exception {
        File script = new File(dataDir + "/mode.d/" + cachedConfigure.starterConfigure.mode + "/on-prepare.sh");
        if (!script.exists())
            script = new File(baseDir + "/mode.d/" + cachedConfigure.starterConfigure.mode + "/on-prepare.sh");
        if (!script.exists()) {
            Log.w(Constants.TAG, "Ignore on-prepare.sh for " + cachedConfigure.starterConfigure.mode);
            return;
        }

        execScript(cachedConfigure, script.getAbsolutePath());
    }

    void execOnStarted(CachedConfigure cachedConfigure) throws Exception {
        File script = new File(dataDir + "/mode.d/" + cachedConfigure.starterConfigure.mode + "/on-start.sh");
        if (!script.exists())
            script = new File(baseDir + "/mode.d/" + cachedConfigure.starterConfigure.mode + "/on-start.sh");
        if (!script.exists()) {
            Log.w(Constants.TAG, "Ignore on-start.sh for " + cachedConfigure.starterConfigure.mode);
            return;
        }

        execScript(cachedConfigure, script.getAbsolutePath());
    }

    void execOnStop(CachedConfigure cachedConfigure) throws Exception {
        File script = new File(dataDir + "/mode.d/" + cachedConfigure.starterConfigure.mode + "/on-stop.sh");
        if (!script.exists())
            script = new File(baseDir + "/mode.d/" + cachedConfigure.starterConfigure.mode + "/on-stop.sh");
        if (!script.exists()) {
            Log.w(Constants.TAG, "Ignore on-stop.sh for " + cachedConfigure.starterConfigure.mode);
            return;
        }

        execScript(cachedConfigure, script.getAbsolutePath());
    }

    private void execScript(CachedConfigure cachedConfigure, String script) throws IOException {
        HashMap<String, String> env = new HashMap<>();

        if (cachedConfigure.clashConfigure.portHttp != null)
            env.put("CLASH_HTTP_PORT", cachedConfigure.clashConfigure.portHttp);
        if (cachedConfigure.clashConfigure.portSocks != null)
            env.put("CLASH_SOCKS_PORT", cachedConfigure.clashConfigure.portSocks);
        if (cachedConfigure.clashConfigure.portRedirect != null)
            env.put("CLASH_REDIR_PORT", cachedConfigure.clashConfigure.portRedirect);
        if (cachedConfigure.clashConfigure.dns.enable && cachedConfigure.clashConfigure.dns.listen != null)
            env.put("CLASH_DNS_PORT", cachedConfigure.clashConfigure.dns.listen.split(":")[1]);
        if (cachedConfigure.starterConfigure.blacklist != null)
            env.put("PROXY_BLACKLIST_UID", buildUidList(cachedConfigure));

        env.put("CLASH_UID", Constants.CLASH_UID);
        env.put("CLASH_GID", Constants.CLASH_GID);

        exec("sh " + script, env);
    }

    private void exec(String command, HashMap<String, String> env) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command("/system/bin/sh");
        builder.environment().putAll(env);

        Process process = builder.start();

        process.getOutputStream().write(("exec " + command + " 2>&1 ; exit -1\n").getBytes());
        process.getOutputStream().flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            Log.i(Constants.TAG, "proxy-setup: " + line);
        }

        int result = -1;

        try {
            result = process.waitFor();
        } catch (InterruptedException ignored) {
        }

        if (result != 0)
            throw new IOException("Mode setup script exec failure");

        process.destroy();
    }

    private String buildUidList(CachedConfigure cachedConfigure) {
        StringBuilder sb = new StringBuilder();

        for (int uid : cachedConfigure.blacklist) {
            sb.append(uid).append(' ');
        }

        return sb.toString().trim();
    }
}
