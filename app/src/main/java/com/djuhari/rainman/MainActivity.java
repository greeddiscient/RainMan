package com.djuhari.rainman;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    public static final String TAG= MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather = new CurrentWeather();
    private String currentCity;

    @Bind(R.id.timeLabel) TextView timeLabel;
    @Bind(R.id.temperatureLabel) TextView temperatureLabel;
    @Bind(R.id.humidityValue) TextView humidityValue;
    @Bind(R.id.rainValue) TextView rainValue;
    @Bind(R.id.summaryLabel) TextView summaryLabel;
    @Bind(R.id.weatherIcon) ImageView weatherIcon;
    @Bind(R.id.refreshButton) ImageView refreshButton;
    @Bind(R.id.locationLabel) TextView locationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    try {
                        getWeather();
                    } catch (IOException e) {
                        alertUserAboutNetwork();
                    }
                } else {
                    alertUserAboutNetwork();
                }
            }
        });
        if (isNetworkAvailable()){
            try {
                getWeather();
            } catch (IOException e) {
                alertUserAboutNetwork();
            }
        }
        else{
            alertUserAboutNetwork();
        }
    }

    private void getWeather() throws IOException {
        String apiKey="c6f5fae6b130f0b6efd1d198d3bb3bee";
        double latitude = 37.8615830;
        double longitude= -122.2285170;
        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
        if (addresses.size() > 0) {
            currentCity=addresses.get(0).getLocality();
        }

        String forecast= "https://api.forecast.io/forecast/"+apiKey+"/"+latitude+","+longitude;

        OkHttpClient client= new OkHttpClient();
        Request request= new Request.Builder().url(forecast).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String jsonData= response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {
                        mCurrentWeather=getCurrentDetails(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                            }
                        });

                    }
                    else
                    {
                        alertUserAboutError();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception caught:", e);
                }
            }
        });
    }

    private void updateDisplay() {
        temperatureLabel.setText(mCurrentWeather.getTemp()+"");
        humidityValue.setText(mCurrentWeather.getHumidity()+"");
        rainValue.setText(mCurrentWeather.getPrecip()+"%");
        summaryLabel.setText(mCurrentWeather.getSummary());
        weatherIcon.setImageDrawable(getResources().getDrawable(mCurrentWeather.getIconId(), null));
        timeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be:");
        locationLabel.setText(currentCity);
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException{
        JSONObject forecast= new JSONObject(jsonData);
        String timezone= forecast.getString("timezone");
        Log.i(TAG, "From JSON:" +timezone);
        JSONObject currently= forecast.getJSONObject("currently");
        CurrentWeather myRain= new CurrentWeather();
        myRain.setHumidity(currently.getDouble("humidity"));
        myRain.setIcon(currently.getString("icon"));
        myRain.setTemp(currently.getInt("temperature"));
        myRain.setTime(currently.getLong("time"));
        myRain.setSummary(currently.getString("summary"));
        myRain.setPrecip(currently.getDouble("precipProbability"));
        myRain.setTimezone(forecast.getString("timezone"));

        Log.d(TAG, myRain.getFormattedTime());


        return myRain;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo= manager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()){
            return true;
        }
        return false;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    private void alertUserAboutNetwork(){
        NoNetworkDialogFragment dialog= new NoNetworkDialogFragment();
        dialog.show(getFragmentManager(), "network_dialog");

    }
}
