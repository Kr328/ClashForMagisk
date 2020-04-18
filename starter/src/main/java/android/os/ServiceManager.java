package android.os;

import androidx.annotation.Keep;

@SuppressWarnings({"unused", "RedundantThrows"})
@Keep
public class ServiceManager {
    public static IBinder getService(String name) throws RemoteException {
        throw new IllegalArgumentException("Stub!");
    }
}
