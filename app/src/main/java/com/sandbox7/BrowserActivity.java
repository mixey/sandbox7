package com.sandbox7;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BrowserActivity extends Activity {

    private WebView browser;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        browser = (WebView) findViewById(R.id.webView);
        WebSettings settings = browser.getSettings();
        settings.setJavaScriptEnabled(true);
        browser.setWebViewClient(new CustomWebClient());
        browser.loadUrl(getIntent().getStringExtra("p_link"));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                browser.reload();
            }
        });
    }

    private class CustomWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mSwipeRefreshLayout.setRefreshing(false);

            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mSwipeRefreshLayout.measure(1, 1);
            mSwipeRefreshLayout.setRefreshing(true);

            super.onPageStarted(view, url, favicon);
        }
    }
}
