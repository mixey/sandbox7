package com.sandbox7;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sandbox7.adapters.RequestListAdapter;
import com.sandbox7.parser.FeedReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class MainActivity extends Activity {
    private RequestListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_main);
        else
            setContentView(R.layout.activity_main_landscape);

        adapter = new RequestListAdapter<FeedReader.FeedEntry>(this) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = layoutInflater.inflate(R.layout.list_item_news, null);
                }

                FeedReader.FeedEntry item = listData.get(position);

                ImageView image = (ImageView) convertView.findViewById(R.id.image);
                new DownloadImagesTask(image, item).execute(item.imageUrl);

                TextView createDate = (TextView) convertView.findViewById(R.id.date);
                createDate.setText(item.createDate);

                TextView title = (TextView) convertView.findViewById(R.id.text);
                title.setText(item.title);

                return convertView;
            }

            @Override
            public void onSuccess(List response) {
                super.onSuccess(response);

                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(true);
            }

            @Override
            public void onFail(List response) {
                super.onFail(response);

                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(true);

                Toast.makeText(MainActivity.this, String.valueOf(response.get(1)), Toast.LENGTH_SHORT).show();
            }
        };
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.empty));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                FeedReader.FeedEntry item = (FeedReader.FeedEntry) adapter.getItem(position);

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
                    scroll.setVisibility(View.VISIBLE);
                    scroll.scrollTo(0, 0);

                    TextView details = (TextView) findViewById(R.id.details);
                    details.setText(item.summary);

                    TextView title = (TextView) findViewById(R.id.title);
                    title.setText(item.title);
                    title.setTag(item.link);

                } else if (isOnline()) {
                    Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
                    intent.putExtra("p_link", item.link);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    intent.putExtra("p_title", item.title);
                    intent.putExtra("p_details", item.summary);
                    intent.putExtra("p_link", item.link);
                    startActivity(intent);
                }
            }
        });
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0) return;

                int percent = (firstVisibleItem + visibleItemCount) * 100 / totalItemCount;
                if (percent > 60)
                    adapter.nextPage();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.refreshData();
            }
        });
        adapter.refreshData();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                ? R.layout.activity_main
                : R.layout.activity_main_landscape);
    }

    public void showOriginalPostHandler(View v) {
        TextView title = (TextView) findViewById(R.id.title);
        Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
        intent.putExtra("p_link", String.valueOf(title.getTag()));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        adapter.saveCache();
        super.onDestroy();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    public class DownloadImagesTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView image;
        private final FeedReader.FeedEntry feedItem;

        public DownloadImagesTask(ImageView image, FeedReader.FeedEntry feedItem) {
            this.image = image;
            this.feedItem = feedItem;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            return downloadImage(urls[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) return;

            image.setImageBitmap(result);
            image.setVisibility(View.VISIBLE);
            feedItem.image = result;
        }

        private Bitmap downloadImage(String url) {
            if (feedItem.image != null)
                return feedItem.image;

            Bitmap bm = null;
            try {
                URL aURL = new URL(url);
                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {

            }
            return bm;
        }
    }
}
