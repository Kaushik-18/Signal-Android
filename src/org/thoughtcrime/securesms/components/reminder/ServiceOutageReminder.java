package org.thoughtcrime.securesms.components.reminder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.thoughtcrime.securesms.BuildConfig;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class ServiceOutageReminder extends Reminder {

  private static final String TAG = ServiceOutageReminder.class.getSimpleName();

  private static final String IP_SUCCESS = "127.0.0.1";
  private static final String IP_FAILURE = "127.0.0.2";

  private static final long OUTAGE_CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(1);

  public ServiceOutageReminder(@NonNull Context context) {
    super(context.getString(R.string.reminder_header_service_unavailable_title),
          context.getString(R.string.reminder_header_service_unavailable_text));
  }

  @Override
  public boolean isDismissable() {
    return false;
  }

  @WorkerThread
  public static boolean isEligible(@NonNull Context context) {
    if (!TextSecurePreferences.getNeedsOutageCheck(context)) {
      return false;
    }

    long timeSinceLastCheck = System.currentTimeMillis() - TextSecurePreferences.getLastOutageCheckTime(context);
    if (timeSinceLastCheck < OUTAGE_CHECK_INTERVAL) {
      return true;
    }

    TextSecurePreferences.setLastOutageCheckTime(context, System.currentTimeMillis());

    try {
      InetAddress address = InetAddress.getByName(BuildConfig.SIGNAL_SERVICE_STATUS_URL);

      if (IP_SUCCESS.equals(address.getHostAddress())) {
        Log.w(TAG, "Service is available.");
        TextSecurePreferences.setNeedsOutageCheck(context, false);
        return false;
      } else if (IP_FAILURE.equals(address.getHostAddress())) {
        Log.w(TAG, "Service is down.");
        TextSecurePreferences.setNeedsOutageCheck(context, true);
        return true;
      } else {
        Log.w(TAG, "Service status check returned an unrecognized IP address. Assuming outage.");
        TextSecurePreferences.setNeedsOutageCheck(context, true);
        return true;
      }
    } catch (UnknownHostException e) {
      Log.w(TAG, "Service status check could not reach the host. Assuming success to avoid false positives due to bad network.");
      TextSecurePreferences.setNeedsOutageCheck(context, false);
      return false;
    }
  }

  @NonNull
  @Override
  public Importance getImportance() {
    return Importance.ERROR;
  }
}
