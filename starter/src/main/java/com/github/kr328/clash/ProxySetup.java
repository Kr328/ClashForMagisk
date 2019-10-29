package com.github.kr328.clash;

import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.HashMap;

class ProxySetup {
    private String dataDir;
    private String baseDir;

    ProxySetup(String baseDir, String dataDir) {
        this.dataDir = dataDir;
        this.baseDir = baseDir;
    }

    void execOnPrepare(StarterConfigure starterConfigure, ClashConfigure clashConfigure) throws Exception {
        File script = new File(dataDir + "/mode.d/" + starterConfigure.mode + "/on-prepare.sh");
        if (!script.exists())
            script = new File(baseDir + "/mode.d/" + starterConfigure.mode + "/on-prepare.sh");
        if (!script.exists()) {
            Log.w(Constants.TAG, "Ignore on-prepare.sh for " + starterConfigure.mode);
            return;
        }

        HashMap<String, String> env = new HashMap<>();

        if (clashConfigure.portHttp != null)
            env.put("CLASH_HTTP_PORT", clashConfigure.portHttp);
        if (clashConfigure.portSocks != null)
            env.put("CLASH_SOCKS_PORT", clashConfigure.portSocks);
        if (clashConfigure.portRedirect != null)
            env.put("CLASH_REDIR_PORT", clashConfigure.portRedirect);
        if (clashConfigure.dns != null && clashConfigure.dns.enable && clashConfigure.dns.listen != null)
            env.put("CLASH_DNS_PORT", clashConfigure.dns.listen.split(":")[1]);
        if (starterConfigure.blacklist != null)
            env.put("PROXY_BLACKLIST_UID", buildUidList(starterConfigure));

        env.put("CLASH_UID", Constants.CLASH_UID);
        env.put("CLASH_GID", Constants.CLASH_GID);

        exec("sh " + script.getAbsolutePath(), env);
    }

    void execOnStarted(StarterConfigure starterConfigure, ClashConfigure clashConfigure) throws Exception {
        File script = new File(dataDir + "/mode.d/" + starterConfigure.mode + "/on-start.sh");
        if (!script.exists())
            script = new File(baseDir + "/mode.d/" + starterConfigure.mode + "/on-start.sh");
        if (!script.exists()) {
            Log.w(Constants.TAG, "Ignore on-start.sh for " + starterConfigure.mode);
            return;
        }

        HashMap<String, String> env = new HashMap<>();

        if (clashConfigure.portHttp != null)
            env.put("CLASH_HTTP_PORT", clashConfigure.portHttp);
        if (clashConfigure.portSocks != null)
            env.put("CLASH_SOCKS_PORT", clashConfigure.portSocks);
        if (clashConfigure.portRedirect != null)
            env.put("CLASH_REDIR_PORT", clashConfigure.portRedirect);
        if (clashConfigure.dns != null && clashConfigure.dns.enable && clashConfigure.dns.listen != null)
            env.put("CLASH_DNS_PORT", clashConfigure.dns.listen.split(":")[1]);
        if (starterConfigure.blacklist != null)
            env.put("PROXY_BLACKLIST_UID", buildUidList(starterConfigure));

        env.put("CLASH_UID", Constants.CLASH_UID);
        env.put("CLASH_GID", Constants.CLASH_GID);

        exec("sh " + script.getAbsolutePath(), env);
    }

    void execOnStop(StarterConfigure starterConfigure, ClashConfigure clashConfigure) throws Exception {
        File script = new File(dataDir + "/mode.d/" + starterConfigure.mode + "/on-stop.sh");
        if (!script.exists())
            script = new File(baseDir + "/mode.d/" + starterConfigure.mode + "/on-stop.sh");
        if (!script.exists()) {
            Log.w(Constants.TAG, "Ignore on-stop.sh for " + starterConfigure.mode);
            return;
        }

        HashMap<String, String> env = new HashMap<>();

        if (clashConfigure.portHttp != null)
            env.put("CLASH_HTTP_PORT", clashConfigure.portHttp);
        if (clashConfigure.portSocks != null)
            env.put("CLASH_SOCKS_PORT", clashConfigure.portSocks);
        if (clashConfigure.portRedirect != null)
            env.put("CLASH_REDIR_PORT", clashConfigure.portRedirect);
        if (clashConfigure.dns != null && clashConfigure.dns.enable && clashConfigure.dns.listen != null)
            env.put("CLASH_DNS_PORT", clashConfigure.dns.listen.split(":")[1]);
        if (starterConfigure.blacklist != null)
            env.put("PROXY_BLACKLIST_UID", buildUidList(starterConfigure));

        env.put("CLASH_UID", Constants.CLASH_UID);
        env.put("CLASH_GID", Constants.CLASH_GID);

        exec("sh " + script.getAbsolutePath(), env);
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
        process.destroyForcibly();
    }

    private String buildUidList(StarterConfigure starterConfigure) throws RemoteException {
        IPackageManager pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        StringBuilder result = new StringBuilder();

        for ( String line : starterConfigure.blacklist ) {
            String[] split = line.split(":");

            switch (split[0]) {
                case "package":
                    PackageInfo packageInfo =
                            pm.getPackageInfo(split[1], 0, split.length == 3 ? Integer.parseInt(split[2]) : 0);
                    if ( packageInfo == null ) {
                        Log.w(Constants.TAG, "Package " + split[1] + " not found. Ignore.");
                        break;
                    }
                    result.append(packageInfo.applicationInfo.uid).append(' ');
                    break;
                case "uid":
                    result.append(split[1]).append(' ');
                    break;
                case "user":
                    int uid = android.os.Process.getUidForName(split[1]);
                    if ( uid < 0 ) {
                        Log.w(Constants.TAG, "Invalid user " + split[1] + ". Ignore.");
                        break;
                    }
                    result.append(uid).append(' ');
                    break;
            }
        }

        return result.toString();
    }
}
