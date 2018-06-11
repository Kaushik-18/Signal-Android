package org.thoughtcrime.securesms.conversationlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.thoughtcrime.securesms.components.reminder.Reminder;
import org.thoughtcrime.securesms.events.ReminderUpdateEvent;
import org.whispersystems.libsignal.util.guava.Optional;

public class ConversationListViewModel extends ViewModel {

  private final MutableLiveData<Optional<Reminder>> events;
  private final ReminderRepository                  reminderRepository;
  private final EventBus                            eventBus;

  private ConversationListViewModel(@NonNull ReminderRepository reminderRepository, @NonNull EventBus eventBus) {
    this.reminderRepository = reminderRepository;
    this.eventBus           = eventBus;
    this.events             = new MutableLiveData<>();

    eventBus.register(this);
  }

  public @NonNull LiveData<Optional<Reminder>> getReminders() {
    return events;
  }

  public void refreshReminders() {
    reminderRepository.getReminder(events::postValue);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEvent(ReminderUpdateEvent event) {
    refreshReminders();
  }

  @Override
  protected void onCleared() {
    EventBus.getDefault().unregister(this);
  }

  public static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final ReminderRepository statusRepository;
    private final EventBus           eventBus;

    public Factory(@NonNull ReminderRepository statusRepository, @NonNull EventBus eventBus) {
      this.statusRepository = statusRepository;
      this.eventBus         = eventBus;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return modelClass.cast(new ConversationListViewModel(statusRepository, eventBus));
    }
  }
}
