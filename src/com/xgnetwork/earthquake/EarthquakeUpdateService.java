package com.xgnetwork.earthquake;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class EarthquakeUpdateService extends Service {
	public static String TAG = "EARTHQUAKE_UPDATE_SERVICE";
	
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	private void addNewQuake(Quake _quake){
		ContentResolver cr = getContentResolver();
		String w = EarthquakeProvider.KEY_DATE + " = " + _quake.getDate().getTime();
		Cursor query = cr.query(EarthquakeProvider.CONTENT_URI, null, w, 
				null, null);
		if(query.getCount() == 0){
			ContentValues values = new ContentValues();
			values.put(EarthquakeProvider.KEY_DATE, _quake.getDate().getTime());
			values.put(EarthquakeProvider.KEY_DETAILS, _quake.getDetails());
			values.put(EarthquakeProvider.KEY_SUMMARY, _quake.toString());
			values.put(EarthquakeProvider.KEY_LOCATION_LAT, _quake.getLocation().getLatitude());
			values.put(EarthquakeProvider.KEY_LOCATION_LNG, _quake.getLocation().getLongitude());
			values.put(EarthquakeProvider.KEY_MAGNITUDE, _quake.getMagnitude());
			cr.insert(EarthquakeProvider.CONTENT_URI, values);
		}
		query.close();
	}
	
	public void refreshEarthquakes(){
		URL url;
		try{
			String quakeFeed = getString(R.string.quake_feed);
			url = new URL(quakeFeed);
			
			URLConnection conn;
			conn = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			int responseCode = httpConn.getResponseCode();
			
			if (responseCode == HttpURLConnection.HTTP_OK){
				
				InputStream inStrm = httpConn.getInputStream();
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				
				Document doc = db.parse(inStrm);
				Element docElement = doc.getDocumentElement();
				
				NodeList ndList = docElement.getElementsByTagName("entry");
				if(ndList != null && ndList.getLength() > 0){
					for(int i = 0; i < ndList.getLength(); i++){
						int _index = 0;
						Element entry = (Element) ndList.item(i);
						Element title = (Element) entry.getElementsByTagName("title").item(_index);
						Element geo = (Element) entry.getElementsByTagName("georss:point").item(_index);
						Element when = (Element) entry.getElementsByTagName("updated").item(_index);
						Element link = (Element) entry.getElementsByTagName("link").item(_index);
					
						String details = title.getFirstChild().getNodeValue();
						String hostName = "http://earthquake.usgs.gov";
						String linkString = hostName + link.getAttribute("href");
						
						String point = geo.getFirstChild().getNodeValue();
						String whenString = when.getFirstChild().getNodeValue();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
						Date quakeDate = new GregorianCalendar(0,0,0).getTime();
						try{
							quakeDate = sdf.parse(whenString);
						}catch(ParseException pe){
							Log.d(TAG,"Date Parsing Exception", pe);
						}
						
						String[] locations = point.split(" ");
						Location location = new Location("dummyGPS");
						location.setLatitude(Double.parseDouble(locations[0]));
						location.setLongitude(Double.parseDouble(locations[1]));
						
						String magString = details.split(" ")[1];
						int end = magString.length() - 1;
						double magnitude = Double.parseDouble(magString.substring(0,end));
						
						details = details.split(",")[1].trim();
						final Quake quake = new Quake(quakeDate,details,location,
								magnitude,linkString);
						
						addNewQuake(quake);
					}
				}
			
			}
		} catch(MalformedURLException e){
			Log.d(TAG,"Malformed URL Exception");
		} catch(IOException e){
			Log.d(TAG,"IOException");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		finally{
			
		}
	}

	private Timer updateTimer;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Context context = getApplicationContext();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		
		int updateFreq = Integer.parseInt(sp.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60"));
		boolean autoUpdateChecked = sp.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);
		updateTimer.cancel();
		
		if(autoUpdateChecked){
			updateTimer = new Timer("earthquakeUpdates");
			updateTimer.scheduleAtFixedRate(doRefresh, 0, updateFreq * 60*1000);
		}else{
			Thread t = new Thread(new Runnable(){
				public void run(){
					refreshEarthquakes();
				}
			});
			t.start();
		}
		
		return Service.START_STICKY;
	}
	
	private TimerTask doRefresh = new TimerTask(){
		public void run(){
			refreshEarthquakes();
		}
	};
	
	@Override
	public void onCreate(){
		super.onCreate();
		updateTimer = new Timer("earthquakeUpdates");
	}
	
	
}
