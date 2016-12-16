package com.sandbox7;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DetailsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getIntent().getStringExtra("p_title"));
        title.setTag(getIntent().getStringExtra("p_link"));

        TextView details = (TextView) findViewById(R.id.details);
        details.setText(getIntent().getStringExtra("p_details"));
    }

    public void showOriginalPostHandler(View v) {
        TextView title = (TextView) findViewById(R.id.title);
        Intent intent = new Intent(this, BrowserActivity.class);
        intent.putExtra("p_link", String.valueOf(title.getTag()));
        startActivity(intent);
    }
}
