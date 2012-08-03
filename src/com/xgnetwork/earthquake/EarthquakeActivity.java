package com.xgnetwork.earthquake;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class EarthquakeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_earthquake, menu);
        return true;
    }
}
