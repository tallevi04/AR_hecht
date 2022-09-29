package com.Hecht.ar.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/** Helper class for managing on-device storage of cloud anchor IDs. */
public class StorageManager {
  //cloud anchor dir
  private static final String SHARED_PREFS_NAME = "cloud_anchor_short_codes";
  private static final String NEXT_SHORT_CODE = "next_short_code";
  private static final String KEY_PREFIX = "anchor;";
  private static final int INITIAL_SHORT_CODE = 1;

  /** Gets a new short code that can be used to store the anchor ID. */
  public int nextShortCode(Activity activity) {
    SharedPreferences sharedPrefs =
        activity.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    int shortCode = sharedPrefs.getInt(NEXT_SHORT_CODE, INITIAL_SHORT_CODE);
    sharedPrefs.edit().putInt(NEXT_SHORT_CODE, shortCode + 1).apply();
    return shortCode;
  }

  /** Stores the cloud anchor ID in the activity's SharedPrefernces. */
  public void storeUsingShortCode(Activity activity, int shortCode, String cloudAnchorId) {
    SharedPreferences sharedPrefs = activity.getPreferences(Context.MODE_PRIVATE);
    sharedPrefs.edit().putString(KEY_PREFIX + shortCode, cloudAnchorId).apply();
  }

  /**
   * Retrieves the cloud anchor ID using a short code.
   * Returns an empty string if a cloud anchor ID was not stored for this short code.
   */
  public String getCloudAnchorId(Activity activity, int shortCode) {
    SharedPreferences sharedPrefs = activity.getPreferences(Context.MODE_PRIVATE);
    return sharedPrefs.getString(KEY_PREFIX + shortCode, "");
  }
}
