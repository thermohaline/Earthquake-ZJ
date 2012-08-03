package com.xgnetwork.earthquake;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.ListFragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

public class EarthquakeListFragment extends ListFragment {
	ArrayAdapter<Quake> quakeArrayAdapter;
	ArrayList<Quake> earthquakes = new ArrayList<Quake>();
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		int layoutId = android.R.layout.simple_list_item_1;
		quakeArrayAdapter = new ArrayAdapter<Quake>(getActivity(),layoutId,earthquakes);
		setListAdapter(quakeArrayAdapter);
		
		Thread t = new Thread(new Runnable(){
			public void run(){
				refreshEarthquakes();
			}
		});
		t.start();
	}
	
	private static final String TAG = "EARTHQUAKE";
	private Handler handler = new Handler();
	
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
				
				this.earthquakes.clear();
				
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
						
						handler.post(new Runnable(){
							public void run(){
								addNewQuake(quake);
							}
						});
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
	
	private void addNewQuake(Quake _quake){
		earthquakes.add(_quake);
		this.quakeArrayAdapter.notifyDataSetChanged();
	}
}
