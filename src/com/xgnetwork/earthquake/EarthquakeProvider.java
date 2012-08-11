package com.xgnetwork.earthquake;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class EarthquakeProvider extends ContentProvider {
	

	public static final Uri CONTENT_URI = 
			Uri.parse("content://com.xgnetwork.earthquakeprovider/earthquakes");
	
	public static final String KEY_ID = "_id";
	public static final String KEY_DATE = "date";
	public static final String KEY_DETAILS = "details";
	public static final String KEY_SUMMARY = "summary";
	public static final String KEY_LOCATION_LAT = "latitude";
	public static final String KEY_LOCATION_LNG = "longitude";
	public static final String KEY_MAGNITUDE = "magnitude";
	public static final String KEY_LINK = "link";
	
	private static final int QUAKES = 1;
	private static final int QUAKE_ID = 2;
	private static final int SEARCH = 3;
	
	private static final UriMatcher uriMatcher;
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.xgnetwork.earthquakeprovider", "earthquakes", 
				QUAKES);
		uriMatcher.addURI("com.xgnetwork.earthquakeprovider", "earthquakes/#", 
				QUAKE_ID);
		uriMatcher.addURI("com.xgnetwork.earthquakeprovider", 
				SearchManager.SUGGEST_URI_PATH_QUERY, 
				SEARCH);
		uriMatcher.addURI("com.xgnetwork.earthquakeprovider", 
				SearchManager.SUGGEST_URI_PATH_QUERY + "/*", 
				SEARCH);
		uriMatcher.addURI("com.xgnetwork.earthquakeprovider", 
				SearchManager.SUGGEST_URI_PATH_SHORTCUT, 
				SEARCH);
		uriMatcher.addURI("com.xgnetwork.earthquakeprovider", 
				SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", 
				SEARCH);
	}
	
	private static final HashMap<String,String> SEARCH_PROJECTION_MAP;
	static{
		SEARCH_PROJECTION_MAP = new HashMap<String,String>();
		SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, 
				KEY_SUMMARY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
		SEARCH_PROJECTION_MAP.put("_id", KEY_ID + " AS " + "_id");
	}
	
	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		int count = -1;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		switch(uriMatcher.match(uri)){
			case QUAKES: 
				count = database.delete(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, whereClause, whereArgs);
				break;
			case QUAKE_ID:
				String segment = uri.getPathSegments().get(1);
				whereClause = TextUtils.isEmpty(whereClause) ? "" : " AND (" + whereClause + ")";
				count = database.delete(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, whereClause, whereArgs);
				break;
			default: 
				throw new IllegalArgumentException("Unsupported Uri: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)){
			case QUAKES: return "vnd.android.cursor.dir/vnd.xgnetwork.earthquake";
			case QUAKE_ID: return "vnd.android.cursor.item/vnd.xgnetwork.earthquake";
			case SEARCH: return SearchManager.SUGGEST_MIME_TYPE;
			default: return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		long rowID = database.insert(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE,
				"quake", values);
		
		if (rowID > 0){
			Uri uri_1 = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri_1, null);
			return uri_1;
		}
		
		throw new SQLException("Failed to insert row into" + uri);
	}

	private EarthquakeDatabaseHelper dbHelper;
	
	@Override
	public boolean onCreate() {
		Context context = getContext();
		dbHelper = new EarthquakeDatabaseHelper(context, 
				EarthquakeDatabaseHelper.DATABASE_NAME, null,
				EarthquakeDatabaseHelper.DATABASE_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
		sqb.setTables(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE);
		
		switch(uriMatcher.match(uri)){
				case QUAKE_ID: sqb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
						break;
				case SEARCH: sqb.appendWhere(KEY_SUMMARY + " LIKE \"%" + 
						uri.getPathSegments().get(1) + "%\"");
						sqb.setProjectionMap(SEARCH_PROJECTION_MAP);
						break;
				default: break;
		}
		String sortBy = TextUtils.isEmpty(sortOrder)? KEY_DATE : sortOrder;
		Cursor c = sqb.query(database, projection, selection, selectionArgs, null, null, sortBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String whereClause,
			String[] whereArgs) {
		int count = -1;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		switch(uriMatcher.match(uri)){
			case QUAKES: 
				count = database.update(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, 
						values, whereClause, whereArgs);
				break;
			case QUAKE_ID:
				String segment = uri.getPathSegments().get(1);
				whereClause = TextUtils.isEmpty(whereClause) ? "" : " AND (" + whereClause + ")";
				count = database.update(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, 
						values, whereClause, whereArgs);
				break;
			default: 
				throw new IllegalArgumentException("Unsupported Uri: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	private static class EarthquakeDatabaseHelper extends SQLiteOpenHelper{

		private static final String TAG = "EarthquakeProvider";
		private static final String DATABASE_NAME = "earthquakes.db";
		private static final int DATABASE_VERSION = 1;
		private static final String EARTHQUAKE_TABLE = "earthquakes_table";
		
		private static final String DATABASE_CREATE = "CREATE TABLE " +
				EARTHQUAKE_TABLE + " (" + 
				KEY_ID + " integer primary key autoincrement, " +
				KEY_DATE + " INTEGER, " + 
				KEY_DETAILS + " TEXT, " + 
				KEY_SUMMARY + " TEXT, " +
				KEY_LOCATION_LAT + " FLOAT, " +
				KEY_LOCATION_LNG + " FLOAT, " +
				KEY_MAGNITUDE + " FLOAT, " +
				KEY_LINK + " TEXT); ";
		
		private SQLiteDatabase earthquakeDB;
		
		public EarthquakeDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + EARTHQUAKE_TABLE);
			onCreate(db);
		}
	
	}
	

}
