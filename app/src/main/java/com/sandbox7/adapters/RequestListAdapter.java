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

    public void update(List<T> response) {
        this.listData = response;
        notifyDataSetChanged();
    }

    public void refreshData() {
        if (cache != null) {
            onSuccess(cache);
            return;
        }

        Request request = new Request();
        request.setCallback(this);
        request.execute();
    }

    @Override
    public void onSuccess(List response) {
        listData = response;
        cache = listData;

        notifyDataSetChanged();
    }

    @Override
    public void onFail(Object response) {
    }

    @Override
    protected void finalize() throws Throwable {
        saveCache();

        super.finalize();
//        SharedPreferences settings = context.getSharedPreferences("rss.cache", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = settings.edit();
//        JSONArray arr = new JSONArray();
//        arr.put(cache);
//        editor.putString("data", arr.toString());
//        editor.commit();
    }

    private void saveCache() {
//        JSONObject obj = new JSONObject();
//        try {
//            obj.put("data", listData);
//            SharedPreferences settings = context.getSharedPreferences("rss.cache", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putString("data", obj.toString());
//            editor.commit();
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
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
//        try {
//            SharedPreferences settings = context.getSharedPreferences("rss.cache", Context.MODE_PRIVATE);
//            String obj = settings.getString("data", "");
//            JSONObject qwe = new JSONObject(obj);
//            if (qwe != null && qwe.has("data"))
//                listData = (List<T>) qwe.get("data");
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
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
}
