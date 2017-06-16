package com.example.sigaev.mrsensor;

import android.Manifest;
import android.app.IntentService;
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
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class TCPSenderService extends IntentService implements SensorEventListener {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FOO = "com.example.sigaev.mrsensor.action.FOO";
    public static final String ACTION_BAZ = "com.example.sigaev.mrsensor.action.BAZ";

    // TODO: Rename parameters
    public static final String EXTRA_PARAM1 = "com.example.sigaev.mrsensor.extra.PARAM1";
    public static final String EXTRA_PARAM2 = "com.example.sigaev.mrsensor.extra.PARAM2";

    public TCPSenderService() {
        super("TCPSenderService");
    }

    // <><><><><><><><><><>><><> MY STUFF STARTS HERE<><><><><><><><><><><><><><><><><><><><>
    // Socket variables
    String serverIP = "209.2.233.234";
    private SensorData sdata;
    private int PORT = 34000;
    private Socket sock;

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

    private double currentLong;
    private double currentLat;


    // Location variables
    private LocationManager locationManager;
    private LocationListener locationListener;


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            // Initialize all the sensors
            initSensors();

            // Initialize the location services
            initLocation();

            // Temporary fix here
            sdata = new SensorData();

            // Update the SensorData object
            while(true) {
                updateSensorData();

                String out = "Lux: " + currentLux + "\n" +
                        "Prox: " + currentProx + "\n" +
                        "XAcc: " + currentXAcc + "\n" +
                        "YAcc: " + currentYAcc + "\n" +
                        "ZAcc: " + currentZAcc + "\n" +
                        "Longitude: " + currentLong + "\n" +
                        "Latitude: " + currentLat + "\n";

                Log.e("MrSensor", out);

                SystemClock.sleep(1000);
            }
            //this.stopSelf();

            // Send the data to the server
//            try {
//                sock = new Socket(serverIP, 34000);
//                Log.e("MrSensor", "USCCECSCSCSSCSCSCSCSCECSCSSCSSECS");
//                ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
//                oos.writeObject(out);
//                oos.flush();
//            } catch (IOException e) {
//                Log.e("MrSensor", "socket failed to open");
//            }


//            final String action = intent.getAction();
//            if (ACTION_FOO.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionFoo(param1, param2);
//            } else if (ACTION_BAZ.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
            //}
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // <><><><><><><><><><><><><><><><><><> MISC <><><><><><><><><><><><><><><><><><><><><>
    private void updateSensorData(){

        // Update the SensorData object
        sdata.currentLux = currentLux;
        sdata.currentProx = currentProx;
        sdata.currentXAcc = currentXAcc;
        sdata.currentYAcc = currentYAcc;
        sdata.currentZAcc = currentZAcc;
        sdata.currentLong = currentLong;
        sdata.currentLat = currentLat;

    }


    // <><><><><><><><><><><><><><><><><> LOCATION <><><><><><><><><><><><><><><><><><><><>
    private void initLocation() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLong = location.getLongitude();
                currentLat = location.getLatitude();

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("MrSensor", "Service: Location permission check failed");
            this.stopSelf();
            return;
        }
        Log.e("MrSensor", "ACTIVATING LOCATION SERVICES");
        Log.e("MrSensor", "ACTIVATING LOCATION SERVICES");
        Log.e("MrSensor", "ACTIVATING LOCATION SERVICES");
        Log.e("MrSensor", "ACTIVATING LOCATION SERVICES");
        Log.e("MrSensor", "ACTIVATING LOCATION SERVICES");
        locationManager.requestLocationUpdates("network", 5000, 0, locationListener);
        Log.e("MrSensor", "CALL RETURNED");
        Log.e("MrSensor", "CALL RETURNED");
        Log.e("MrSensor", "CALL RETURNED");
        Log.e("MrSensor", "CALL RETURNED");


    }


    // <><><><><><><><><><><><><><> SENSORS <><><><><><><><><><><><><><><><><><><><><>
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
            Log.e("MrSensor", "Service: Light listener registered");
        }else{
            Log.e("MrSensor", "Service: LIGHT Sensor not found");
            this.stopSelf();
        }

        // Proximity sensor initiation
        proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        currentProx = 0;

        if(proxSensor != null){
            sensorManager.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.e("MrSensor", "Service: Proximity listener registered");
        }else{
            Log.e("MrSensor", "Service: PROXIMITY Sensor not found");
            this.stopSelf();
        }

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(accSensor != null){
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.e("MrSensor", "Service: Acceleration listener registered");
        }else{
            Log.e("MrSensor", "Service: ACCELERATION Sensor not found");
            this.stopSelf();
        }
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


    // <><><><><><><><><><><><><><><> LOCAL CLASSES <><><><><><><><><><><><><><><><><><><>
    // The serializable container for sensor data
    private class SensorData implements Serializable {
        private float currentLux;
        private float currentProx;
        private float currentXAcc;
        private float currentYAcc;
        private float currentZAcc;
        private double currentLong;
        private double currentLat;
    }

}
