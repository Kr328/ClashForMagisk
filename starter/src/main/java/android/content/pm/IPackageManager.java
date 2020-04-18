package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Keep;

@Keep
@SuppressWarnings("unused")
public interface IPackageManager {
    abstract class Stub extends Binder implements IPackageManager {
        public static IPackageManager asInterface(IBinder service) {
            throw new IllegalArgumentException("Stub!");
        }
    }

    ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException;
}
