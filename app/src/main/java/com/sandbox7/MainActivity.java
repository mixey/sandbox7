package com.sandbox7;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sandbox7.adapters.RequestListAdapter;
import com.sandbox7.parser.FeedReader;

import java.util.List;

public class MainActivity extends Activity {

    private RequestListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new RequestListAdapter<FeedReader.FeedEntry>(this) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = layoutInflater.inflate(R.layout.list_item_news, null);

                    FeedReader.FeedEntry item = listData.get(position);

                    TextView createDate = (TextView) convertView.findViewById(R.id.date);
                    createDate.setText(item.createDate);

                    TextView title = (TextView) convertView.findViewById(R.id.text);
                    title.setText(item.title);
                }

                return convertView;
            }

            @Override
            public void onSuccess(List response) {
                super.onSuccess(response);

                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(true);
            }

            @Override
            public void onFail(Object response) {
                Toast.makeText(MainActivity.this, String.valueOf(response), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(true);
            }
        };
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.empty));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                FeedReader.FeedEntry entry = (FeedReader.FeedEntry) adapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
                intent.putExtra("p_link", entry.link);
                startActivity(intent);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
//                swipeRefreshLayout.setRefreshing(true);
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
}
