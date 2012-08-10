package com.xgnetwork.earthquake;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class EarthquakeActivity extends Activity {

	public int minimumMagnitude = 0;
	public boolean autoUpdateChecked = false;
	public int updateFreq = 0;
	
	private static final int SHOW_PREFERENCES = 1;
	
	private static final int MENU_PREFERENCES = Menu.FIRST + 1;
	private static final int MENU_UPDATE = Menu.FIRST + 2;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		menu.add(0,MENU_PREFERENCES,Menu.NONE,R.string.menu_preferences);
		return true;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);
    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
    //    getMenuInflater().inflate(R.menu.activity_earthquake, menu);
    //    return true;
    //}
    
    private void updateFromPreferences(){
    	Context context = getApplicationContext();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	minimumMagnitude = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_MIN_MAG, "3"));
    	updateFreq = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60"));
    	autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE,false);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(requestCode == SHOW_PREFERENCES){
    		
    		updateFromPreferences();
	    	FragmentManager fm = getFragmentManager();
	    	final EarthquakeListFragment quakeList1 = 
	    			(EarthquakeListFragment) fm.findFragmentById(R.id.EarthquakeListFragment);
	    	
	    	Thread t = new Thread(new Runnable(){
	    		public void run(){
	    			quakeList1.refreshEarthquakes();
	    		}
	    	});
	    	t.start();
    	}
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	super.onOptionsItemSelected(item);
    	switch(item.getItemId()){
    	case MENU_PREFERENCES: {
    		Intent i = new Intent(this,PreferencesActivity.class);
    		startActivityForResult(i,SHOW_PREFERENCES);
    		return true;
    	}
    	}
    	return false;
    }
}
