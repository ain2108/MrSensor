package com.example.sigaev.mrsensor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*;
import java.net.*;


/* TODO List:
1) Fix the battery usage (Add onPause and onResume)
2) Fix the public/private
3) Fix the bug with location being accesse when null
4) Do proper serialization
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    // Socket variables
    String serverIP = "209.2.233.234";
    private SensorData sdata;
    private int PORT = 3400;
    private Socket sock;
    private AsyncClass ac;

    // Sensor variables
    private static SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor proxSensor;
    private Sensor accSensor;

    private float currentLux;
    private float currentProx;

    private float currentXAcc;
    private float currentYAcc;
    private float currentZAcc;


    // Location variables
    private LocationManager locationManager;
    private LocationListener locationListener;

    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all the sensors
        initSensors();

        // Initialize the location services
        initLocation();

        sdata = new SensorData();

    }

    private void initLocation() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                Log.e("MrSensor", "location assigned");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        startLocationService();

    }

    private void startLocationService(){
        // Start the service
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,3);
            }
            return;
        }
        locationManager.requestLocationUpdates("network", 5000, 0, locationListener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 3:
                startLocationService();
                break;
            default:
                break;
        }
    }

    /** Function does Sensor related initialization */
    private void initSensors(){
        // Initialize the sensor
        sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);

        // Light sensor initiation
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        currentLux = 0;

        // Check if sensor was found
        if(lightSensor != null){
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.e("MrSensor", "Light listener registered");
        }else{
            Log.e("MrSensor", "LIGHT Sensor not found");
            finish();
        }

        // Proximity sensor initiation
        proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        currentProx = 0;

        if(proxSensor != null){
            sensorManager.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.e("MrSensor", "Proximity listener registered");
        }else{
            Log.e("MrSensor", "PROXIMITY Sensor not found");
            finish();
        }

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(accSensor != null){
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.e("MrSensor", "Acceleration listener registered");
        }else{
            Log.e("MrSensor", "ACCELERATION Sensor not found");
            finish();
        }
    }

    // The function called when the button is pressed mf
//    public void sendMessage(View view){
//        // Create an intent connecting this activity to the DisplayMessage
//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//
//        EditText editText = (EditText) findViewById(R.id.editText);
//
//        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
//    }

    /** Function updates the views */
    public void updateViews(View view){

        // Set the string as the text of the TextView
        TextView textView = (TextView) findViewById(R.id.lightView);
        textView.setText(String.valueOf(currentLux));

        textView = (TextView) findViewById(R.id.proxView);
        textView.setText(String.valueOf(currentProx));

        textView = (TextView) findViewById(R.id.xView);
        textView.setText(String.valueOf(currentXAcc));

        textView = (TextView) findViewById(R.id.yView);
        textView.setText(String.valueOf(currentYAcc));

        textView = (TextView) findViewById(R.id.zView);
        textView.setText(String.valueOf(currentZAcc));

        textView = (TextView) findViewById(R.id.latView);
        textView.setText(String.valueOf(currentLocation.getLatitude()));

        textView = (TextView) findViewById(R.id.longView);
        textView.setText(String.valueOf(currentLocation.getLongitude()));

        // Send data
        ac = new AsyncClass();
        ac.execute();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
            currentLux = event.values[0];
        }else if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            currentProx = event.values[0];
        }else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            currentXAcc = event.values[0];
            currentYAcc = event.values[1];
            currentZAcc = event.values[2];
        }
    }

    private class SensorData implements Serializable{
        private float currentLux;
        private float currentProx;
        private float currentXAcc;
        private float currentYAcc;
        private float currentZAcc;
        private double currentLong;
        private double currentLat;
    }

    class AsyncClass extends AsyncTask<Void, Void,Void>
    {
        protected Void doInBackground(Void... params)
        {
            hardtask();
            return null;
        }
        public void hardtask()
        {
            // Update the SensorData object
            sdata.currentLux = currentLux;
            sdata.currentProx = currentProx;
            sdata.currentXAcc = currentXAcc;
            sdata.currentYAcc = currentYAcc;
            sdata.currentZAcc = currentZAcc;
            sdata.currentLong = currentLocation.getLongitude();
            sdata.currentLat = currentLocation.getLatitude();

            String out = "Lux: " + currentLux + "\n" +
                    "Prox: " + currentProx + "\n" +
                    "XAcc: " + currentXAcc + "\n" +
                    "YAcc: " + currentYAcc + "\n" +
                    "ZAcc: " + currentZAcc + "\n" +
                    "Longitude: " + sdata.currentLong + "\n" +
                    "Latitude: " + sdata.currentLat + "\n";

            // Send the data to the server
            try {
                sock = new Socket(serverIP, 34000);
                Log.e("MrSensor", "USCCECSCSCSSCSCSCSCSCECSCSSCSSECS");
                ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                oos.writeObject(out);
                oos.flush();
            }catch (IOException e){
                Log.e("MrSensor", "socket failed to open");
            }
        }
    }
}
