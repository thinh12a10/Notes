package com.thinhnguyen.notes.until;

import android.content.Context;
import android.net.ConnectivityManager;

public class ConnectionHelper {
    public static boolean isNetworkConnected(Context context)
    {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
