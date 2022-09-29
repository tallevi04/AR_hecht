package com.Hecht.ar.helpers;

import android.app.Activity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import android.widget.TextView;

/**
 * Helper to manage the sample snackbar. Hides the Android boilerplate code, and exposes simpler methods.
 */
public final class SnackbarHelper {
  private static final int BACKGROUND_COLOR = 0xbf323232;
  private Snackbar messageSnackbar;
  private enum DismissBehavior { HIDE, SHOW, FINISH };
  private int maxLines = 2;
  private String lastMessage = "";

  /** Shows a snackbar with a given message. */
  public void showMessage(Activity activity, String message) {
    if (!message.isEmpty() && (messageSnackbar == null || !lastMessage.equals(message))) {
      lastMessage = message;
      show(activity, message, DismissBehavior.HIDE);
    }
  }

  /**
   * Shows a snackbar with a given error message. When dismissed, will finish the activity.
   * It is Useful for notifying errors, where no further interaction with the activity is possible.
   */
  public void showError(Activity activity, String errorMessage) {
    show(activity, errorMessage, DismissBehavior.FINISH);
  }

  private void show(
      final Activity activity, final String message, final DismissBehavior dismissBehavior) {
    activity.runOnUiThread(() -> {
        messageSnackbar =
            Snackbar.make(
                activity.findViewById(android.R.id.content),
                message, Snackbar.LENGTH_INDEFINITE);
        messageSnackbar.getView().setBackgroundColor(BACKGROUND_COLOR);
        if (dismissBehavior != DismissBehavior.HIDE) {
          messageSnackbar.setAction("Dismiss", v -> messageSnackbar.dismiss());
          if (dismissBehavior == DismissBehavior.FINISH) {
            messageSnackbar.addCallback(
                new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                  @Override
                  public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    activity.finish();
                  }
                });
          }
        }
        ((TextView)
                messageSnackbar
                    .getView()
                    .findViewById(com.google.android.material.R.id.snackbar_text))
            .setMaxLines(maxLines);
        messageSnackbar.show();
      });
  }
  /**
   * Hides the currently showing snackbar, if there is one. Safe to call from any thread.
   * Safe to call even if snackbar is not shown.
   */
  public void hide(Activity activity) {
    if (!isShowing()) {
      return;
    }
    lastMessage = "";
    Snackbar messageSnackbarToHide = messageSnackbar;
    messageSnackbar = null;
    activity.runOnUiThread(messageSnackbarToHide::dismiss);
  }
  public boolean isShowing() {
    return messageSnackbar != null;
  }

  public String getLastMessage() {
    return lastMessage;
  }
}
