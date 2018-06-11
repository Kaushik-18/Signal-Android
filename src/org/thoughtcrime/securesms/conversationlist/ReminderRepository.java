package org.thoughtcrime.securesms.conversationlist;

import android.content.Context;
import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.components.reminder.DefaultSmsReminder;
import org.thoughtcrime.securesms.components.reminder.DozeReminder;
import org.thoughtcrime.securesms.components.reminder.ExpiredBuildReminder;
import org.thoughtcrime.securesms.components.reminder.OutdatedBuildReminder;
import org.thoughtcrime.securesms.components.reminder.PushRegistrationReminder;
import org.thoughtcrime.securesms.components.reminder.Reminder;
import org.thoughtcrime.securesms.components.reminder.ServiceOutageReminder;
import org.thoughtcrime.securesms.components.reminder.ShareReminder;
import org.thoughtcrime.securesms.components.reminder.SystemSmsImportReminder;
import org.thoughtcrime.securesms.components.reminder.UnauthorizedReminder;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.concurrent.Executor;

public class ReminderRepository {

  private final Context  context;
  private final Executor executor;

  public ReminderRepository(@NonNull Context context, @NonNull Executor executor) {
    this.context  = context.getApplicationContext();
    this.executor = executor;
  }

  public void getReminder(@NonNull Callback callback) {
    executor.execute(() -> {
      Optional<Reminder> reminder = Optional.absent();

      if (UnauthorizedReminder.isEligible(context)) {
        reminder = Optional.of(new UnauthorizedReminder(context));
      } else if (ExpiredBuildReminder.isEligible()) {
        reminder = Optional.of(new ExpiredBuildReminder(context));
      } else if (OutdatedBuildReminder.isEligible()) {
        reminder = Optional.of(new OutdatedBuildReminder(context));
      } else if (ServiceOutageReminder.isEligible(context)) {
        reminder = Optional.of(new ServiceOutageReminder(context));
      } else if (DefaultSmsReminder.isEligible(context)) {
        reminder = Optional.of(new DefaultSmsReminder(context));
      } else if (Util.isDefaultSmsProvider(context) && SystemSmsImportReminder.isEligible(context)) {
        reminder = Optional.of(new SystemSmsImportReminder(context));
      } else if (PushRegistrationReminder.isEligible(context)) {
        reminder = Optional.of(new PushRegistrationReminder(context));
      } else if (ShareReminder.isEligible(context)) {
        reminder = Optional.of(new ShareReminder(context));
      } else if (DozeReminder.isEligible(context)) {
        reminder = Optional.of(new DozeReminder(context));
      }

      callback.onComplete(reminder);
    });
  }

  public interface Callback {
    void onComplete(Optional<Reminder> reminder);
  }
}
