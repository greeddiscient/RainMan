package com.example.djurus.rainman;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by djurus on 10/13/16.
 */

public class CurrentWeather {
    private String mIcon;
    private long mTime;
    private double mTemp;
    private double mHumidity;
    private double mPrecip;
    private String mSummary;
    private String mTimezone;

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String mIcon) {
        this.mIcon = mIcon;
    }

    public long getTime() {
        return mTime;
    }

    public int getIconId(){
        int iconId= R.drawable.clear_day;  // cloudy, partly-cloudy-day, or partly-cloudy-night
        if (mIcon.equals("clear-night")){
            iconId= R.drawable.clear_night;
        }
        else if (mIcon.equals("rain")){
            iconId= R.drawable.rain;
        }
        else if (mIcon.equals("snow")){
            iconId= R.drawable.snow;
        }
        else if (mIcon.equals("sleet")){
            iconId= R.drawable.sleet;
        }
        else if (mIcon.equals("wind")){
            iconId=R.drawable.wind;
        }
        else if (mIcon.equals("fog")){
            iconId=R.drawable.fog;
        }
        else if (mIcon.equals("cloudy")){
            iconId=R.drawable.cloudy;
        }
        else if (mIcon.equals("partly-cloudy-day")){
            iconId=R.drawable.partly_cloudy;
        }
        else if (mIcon.equals("partly-cloudy-night")){
            iconId=R.drawable.partly_cloudy;
        }
        return iconId;
    }

    public String getFormattedTime(){
        SimpleDateFormat formatter= new SimpleDateFormat("h:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime= new Date(getTime()*1000);
        String timeString= formatter.format(dateTime);
        return timeString;
    }

    public void setTime(long mTime) {
        this.mTime = mTime;
    }

    public int getTemp() {
        return (int)Math.round(mTemp);
    }

    public void setTemp(double mTemp) {
        this.mTemp = mTemp;
    }

    public double getHumidity() {
        return mHumidity;
    }

    public void setHumidity(double mHumidity) {
        this.mHumidity = mHumidity;
    }

    public int getPrecip() {
        return (int)Math.round(mPrecip*100);
    }

    public void setPrecip(double mPrecip) {
        this.mPrecip = mPrecip;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String mSummary) {
        this.mSummary = mSummary;
    }


    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String timezone) {
        this.mTimezone = timezone;
    }
}