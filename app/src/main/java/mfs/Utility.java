package mfs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import mobilefs.seminar.pdfs.service.R;

public class Utility {
    public static void setServiceStarted(Context context, boolean isStarted)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(context.getString(R.string.key_server_isStarted), isStarted);
        editor.apply();
    }

    public static boolean isServiceStarted(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.key_server_isStarted), false);
    }
}
