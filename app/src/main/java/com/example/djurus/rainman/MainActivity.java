package com.example.djurus.rainman;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationServices;
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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG= MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather = new CurrentWeather();
    private String currentCity= "Berkeley";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLatitude;
    private double mLongitude;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Bind(R.id.timeLabel) TextView timeLabel;
    @Bind(R.id.temperatureLabel) TextView temperatureLabel;
    @Bind(R.id.humidityValue) TextView humidityValue;
    @Bind(R.id.rainValue) TextView rainValue;
    @Bind(R.id.summaryLabel) TextView summaryLabel;
    @Bind(R.id.weatherIcon) ImageView weatherIcon;
    @Bind(R.id.refreshButton) ImageView refreshButton;
    @Bind(R.id.locationLabel) TextView locationLabel;
    @Bind(R.id.view_progress) View mProgressView;
    @Bind(R.id.fahrenheit) TextView fahrenheitOption;
    @Bind(R.id.celcius) TextView celciusOption;
//    private TextView fahrenheitOption = (TextView)findViewById(R.id.fahrenheit);
//    private TextView celciusOption = (TextView)findViewById(R.id.celcius);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (!mResolvingError){
            mGoogleApiClient.connect();
        }


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
        Log.d("location", String.valueOf(mLastLocation));
        fahrenheitOption.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeToFahrenheit();
            }
        });
        celciusOption.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeToCelcius();
            }
        });
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void getWeather() throws IOException {
        showProgress(true);
        String apiKey="c6f5fae6b130f0b6efd1d198d3bb3bee";
        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = gcd.getFromLocation(mLatitude, mLongitude, 1);
        Log.d("aa", String.valueOf("" + (mLastLocation) == null));
        if (addresses.size() > 0) {
            currentCity=addresses.get(0).getLocality();

        }

        String forecast= "https://api.forecast.io/forecast/"+apiKey+"/"+mLatitude+","+mLongitude;

        OkHttpClient client= new OkHttpClient();
        Request request= new Request.Builder().url(forecast).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {
                        mCurrentWeather = getCurrentDetails(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                                changeToFahrenheit();
                                showProgress(false);
                            }
                        });

                    } else {
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
        weatherIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), mCurrentWeather.getIconId()));
        timeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be:");
        locationLabel.setText(currentCity);
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
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

    @Override
    public void onConnected(Bundle connectionHint)  {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
            Log.d(String.valueOf(mLatitude),"latitude");
            Log.d(String.valueOf(mLongitude),"latitude");
        }
        else{
            mLatitude= 37.8562270;
            mLongitude=-122.2299220;
        }

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

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }
    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "APIerrordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    public void changeToCelcius(){
        int degree =mCurrentWeather.getTemp();
        int cTemp = (degree - 32)* 5/9;
        temperatureLabel.setText(cTemp+"");
        fahrenheitOption.setTextColor(Color.parseColor("#80ffffff"));
        celciusOption.setTextColor(Color.parseColor("#ffffff"));
    }
    public void changeToFahrenheit(){
        int degree =mCurrentWeather.getTemp();
        temperatureLabel.setText(degree+"");
        celciusOption.setTextColor(Color.parseColor("#80ffffff"));
        fahrenheitOption.setTextColor(Color.parseColor("#ffffff"));
    }
}