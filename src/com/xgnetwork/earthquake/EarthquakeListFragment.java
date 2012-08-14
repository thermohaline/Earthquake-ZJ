package com.xgnetwork.earthquake;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class EarthquakeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	SimpleCursorAdapter adapter;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		adapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1,
				null, new String[]{EarthquakeProvider.KEY_SUMMARY},
				new int[]{android.R.id.text1}, 0);
		setListAdapter(adapter);
		
		getLoaderManager().initLoader(0, null, this);
		
		refreshEarthquakes();
	}
	
	private static final String TAG = "EARTHQUAKE";
	
	public void refreshEarthquakes(){
		
		getLoaderManager().restartLoader(0, null, EarthquakeListFragment.this);
		getActivity().startService(new Intent(getActivity(),
				EarthquakeUpdateService.class));
		
	}
	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {
				EarthquakeProvider.KEY_ID,
				EarthquakeProvider.KEY_SUMMARY
		};
		EarthquakeActivity mainActivity = (EarthquakeActivity) getActivity();
		String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + mainActivity.minimumMagnitude;
		CursorLoader loader = new CursorLoader(getActivity(),
				EarthquakeProvider.CONTENT_URI,projection, where, null, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}
}
