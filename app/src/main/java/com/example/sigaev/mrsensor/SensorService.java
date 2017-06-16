package com.example.sigaev.mrsensor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.Socket;

/**
 * Created by filipp on 6/16/2016.
 */
public class SensorService extends Service implements SensorEventListener {

    private LocationListener listener;
    private LocationManager locationManager;

    private double longi;
    private double lati;


    // Socket variables
    String serverIP = "209.2.233.234";
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        initSensors();
        initLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        startSending();
        return START_STICKY;
    }

    public void startSending(){

        Runnable toServerRunner = new Runnable() {
            @Override
            public void run() {

                while(true){
                    String out = "Lux: " + currentLux + "\n" +
                            "Prox: " + currentProx + "\n" +
                            "XAcc: " + currentXAcc + "\n" +
                            "YAcc: " + currentYAcc + "\n" +
                            "ZAcc: " + currentZAcc + "\n" +
                            "Longitude: " + longi + "\n" +
                            "Latitude: " + lati + "\n";
                    Log.e("MrSensor", out);

                    SystemClock.sleep(1000);
                }

            }
        };
        Thread toServerThread = new Thread(toServerRunner);
        toServerThread.start();

    }



    public void initLocation(){

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
                sendBroadcast(i);

                longi = location.getLongitude();
                lati = location.getLatitude();

                Log.e("MrSensor", longi + " " + lati);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3000,0,listener);
    }








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




    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }

}