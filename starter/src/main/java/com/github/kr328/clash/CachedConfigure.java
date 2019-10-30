package com.github.kr328.clash;

import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class CachedConfigure {
    StarterConfigure starterConfigure;
    ClashConfigure clashConfigure;
    List<Integer> blacklist;

    CachedConfigure(StarterConfigure starterConfigure, ClashConfigure clashConfigure) {
        this.starterConfigure = starterConfigure;
        this.clashConfigure = clashConfigure;
        this.blacklist = new ArrayList<>();

        buildUidList();
    }

    private void buildUidList() {
        try {
            IPackageManager pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));

            for (String line : starterConfigure.blacklist) {
                String[] split = line.split(":");

                switch (split[0]) {
                    case "package":
                        PackageInfo packageInfo =
                                pm.getPackageInfo(split[1], 0, split.length == 3 ? Integer.parseInt(split[2]) : 0);
                        if (packageInfo == null) {
                            Log.w(Constants.TAG, "Package " + split[1] + " not found. Ignore.");
                            break;
                        }
                        blacklist.add(packageInfo.applicationInfo.uid);
                        break;
                    case "uid":
                        blacklist.add(Integer.parseInt(split[1]));
                        break;
                    case "user":
                        int uid = android.os.Process.getUidForName(split[1]);
                        if (uid < 0) {
                            Log.w(Constants.TAG, "Invalid user " + split[1] + ". Ignore.");
                            break;
                        }
                        blacklist.add(uid);
                        break;
                }
            }
        } catch (RemoteException e) {
            Log.w(Constants.TAG, "Unable to connect package manager", e);
        }
    }
}
