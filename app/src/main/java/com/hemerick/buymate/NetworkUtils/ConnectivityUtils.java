package com.hemerick.buymate.NetworkUtils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectivityUtils {
    public interface InternetCheckListener {
        void onInternetCheckComplete(boolean isInternetAvailable);
    }

    public static void checkInternetConnectivity(Context context, InternetCheckListener listener) {
        new InternetCheckAsyncTask(listener).execute(context);
    }

    private static class InternetCheckAsyncTask extends AsyncTask<Context, Void, Boolean> {
        private final InternetCheckListener listener;

        InternetCheckAsyncTask(InternetCheckListener listener) {
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Context... contexts) {
            if (isNetworkAvailable(contexts[0])) {
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                    urlConnection.setRequestProperty("User-Agent", "ConnectionTest");
                    urlConnection.setRequestProperty("Connection", "close");
                    urlConnection.setConnectTimeout(1500);
                    urlConnection.connect();
                    return (urlConnection.getResponseCode() == 200);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean isInternetAvailable) {
            if (listener != null) {
                listener.onInternetCheckComplete(isInternetAvailable);
            }
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return (mobileNetworkInfo != null && mobileNetworkInfo.isConnectedOrConnecting()) ||
                    (wifiNetworkInfo != null && wifiNetworkInfo.isConnectedOrConnecting());
        }
        return false;
    }
}
