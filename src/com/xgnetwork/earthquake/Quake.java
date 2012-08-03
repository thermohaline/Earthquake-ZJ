package com.xgnetwork.earthquake;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;

public class Quake {
	private Date date;
	private String details;
	private Location location;
	private double magnitude;
	private String link;
	
	
	public Date getDate() {
		return date;
	}
	
	public String getDetails() {
		return details;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public double getMagnitude() {
		return magnitude;
	}
	
	public String getLink() {
		return link;
	}

	public Quake(Date _date, String _details, Location _location,
			double _magnitude, String _link) {
		super();
		this.date = _date;
		this.details = _details;
		this.location = _location;
		this.magnitude = _magnitude;
		this.link = _link;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
		String dateString = sdf.format(date);
		return dateString + ":" + magnitude + " " + details;
	}
	
	
}
