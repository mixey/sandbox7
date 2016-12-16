package com.sandbox7.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.sandbox7.net.Request;
import com.sandbox7.net.RequestCallback;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public abstract class RequestListAdapter<T> extends BaseAdapter implements RequestCallback {

    protected final Context context;
    protected List<T> listData;
    protected LayoutInflater layoutInflater;
    private List<T> cache;
    private int pageNumber = 0;
    private boolean inProgress;
    private boolean hasConnection;
    private boolean isNextRequest;

    public RequestListAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);

        loadCache();
    }

    @Override
    public int getCount() {
        return listData == null ? 0 : listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    public void refreshData() {
        Request request = new Request("http://4pda.ru/news/page/" + pageNumber);
        request.setCallback(this);
        request.execute();
        inProgress = true;
    }

    @Override
    public void onSuccess(List response) {
        if (listData == null || !isNextRequest)
            listData = response;
        else
            listData.addAll(response);
        cache = listData;

        inProgress = false;
        isNextRequest = false;
        hasConnection = true;
        notifyDataSetChanged();
    }

    @Override
    public void onFail(List response) {
        hasConnection = !response.get(0).toString().equals("-1");
        if (!hasConnection && cache != null) {
            listData = cache;
            notifyDataSetChanged();
        }
        inProgress = false;
    }

    public void saveCache() {
        try {
            FileOutputStream fos = context.openFileOutput("rss.cache", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(cache);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCache() {
        try {
            FileInputStream fis = context.openFileInput("rss.cache");
            ObjectInputStream is = new ObjectInputStream(fis);
            cache = (List<T>) is.readObject();
            is.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void nextPage() {
        if (inProgress || !hasConnection) return;

        pageNumber++;
        isNextRequest = true;
        refreshData();
    }
}
