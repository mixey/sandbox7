package com.sandbox7.net;


import android.os.AsyncTask;

import com.sandbox7.parser.FeedReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Request extends AsyncTask<String, String, List> {
    protected RequestCallback callback;
    //    protected String targetUrl = "http://4pda.ru/feed";
    protected String targetUrl;
    private boolean isFailed;

    public Request(String url) {
        targetUrl = url;
    }

    public void setCallback(RequestCallback callback) {
        this.callback = callback;
    }

    @Override
    protected List doInBackground(String... uri) {
        HttpURLConnection urlConnection = null;
        InputStream in = null;
        List result = null;
        int responseCode = -1;

        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            responseCode = urlConnection.getResponseCode();
            in = urlConnection.getInputStream();

            result = new FeedReader(new BufferedReader(
                    new InputStreamReader(in, "windows-1251"))).getData();

        } catch (Exception ex) {
            isFailed = true;
            result = new ArrayList();
            result.add(responseCode);
            result.add(ex.getMessage());
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
//            Thread.sleep(2000);

        return result;
    }

    @Override
    protected void onPostExecute(List result) {
        if (callback == null) return;

        if (!isFailed)
            callback.onSuccess(result);
        else
            callback.onFail(result);
    }
}
