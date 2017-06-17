package com.example.sigaev.mrsensor;

import android.app.Notification;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class SensorService extends Service implements SensorEventListener {

    private final String TAG = "MrSensorService";
    private final String SERVER_IP_TAG = "SERVER_IP";
    private final String SERVER_PORT_TAG = "SERVER_PORT";
    public final String SERVICE_ERR_TAG = "SERVICE_ERR";


    private LocationListener listener;
    private LocationManager locationManager;

    private double longi;
    private double lati;

    // Socket variables
    String serverIP;
    private int serverPORT;
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

    Thread toServerThread;
    private boolean running;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

    public void moveToForward(){

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.flower)
                .setContentTitle("MrSensor")
                .setContentText("Sending coordinates")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startID){

        configure(intent);

        initSensors();

        initLocation();

        moveToForward();

        startSending();

        return START_STICKY;
    }

    public void configure(Intent intent){

        serverIP = intent.getStringExtra(SERVER_IP_TAG);
        String tempPort = intent.getStringExtra(SERVER_PORT_TAG);
        serverPORT = Integer.parseInt(tempPort);
        Log.e(TAG, "Starting service to " + serverIP + " on port " + serverPORT);

    }

    public void startSending(){

        Runnable toServerRunner = new Runnable() {
            @Override
            public void run() {

                while(running){
                    String out = "Lux: " + currentLux + "\n" +
                            "Prox: " + currentProx + "\n" +
                            "XAcc: " + currentXAcc + "\n" +
                            "YAcc: " + currentYAcc + "\n" +
                            "ZAcc: " + currentZAcc + "\n" +
                            "Longitude: " + longi + "\n" +
                            "Latitude: " + lati + "\n";
                    Log.e(TAG, out);

                    try {
                        sock = new Socket(serverIP, serverPORT);
                        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                        oos.writeObject(out);
                        oos.flush();
                    } catch (IOException e) {
                        Log.e(TAG, "socket failed to open");
                        Intent i = new Intent("service_status_update");
                        i.putExtra(SERVICE_ERR_TAG, "ERROR: Could not connect to server");
                        sendBroadcast(i);
                        SensorService.this.stopSelf();
                    }

                    SystemClock.sleep(10000);
                }

            }
        };

        running = true;
        toServerThread = new Thread(toServerRunner);
        toServerThread.start();

    }

    public void initLocation(){

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longi = location.getLongitude();
                lati = location.getLatitude();
                Log.e(TAG, longi + " " + lati);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

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
            Log.e(TAG, "Light listener registered");
        }else{
            Log.e(TAG, "LIGHT Sensor not found");
            this.stopSelf();
        }

        // Proximity sensor initiation
        proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        currentProx = 0;

        if(proxSensor != null){
            sensorManager.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.e(TAG, "Proximity listener registered");
        }else{
            Log.e(TAG, "Sensor not found");
            this.stopSelf();
        }

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(accSensor != null){
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.e("MrSensor", "Acceleration listener registered");
        }else{
            Log.e("MrSensor", "ACCELERATION Sensor not found");
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

        running = false;

        stopForeground(true);

        Log.e(TAG, "Service stopped");
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