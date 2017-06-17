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

public class MainActivity extends AppCompatActivity {

    private boolean serviceRunning;

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceRunning = false;

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



    /** Function updates the views */
//    public void updateViews(View view){
//
//        // Set the string as the text of the TextView
//        TextView textView = (TextView) findViewById(R.id.lightView);
//        textView.setText(String.valueOf(currentLux));
//
//        textView = (TextView) findViewById(R.id.proxView);
//        textView.setText(String.valueOf(currentProx));
//
//        textView = (TextView) findViewById(R.id.xView);
//        textView.setText(String.valueOf(currentXAcc));
//
//        textView = (TextView) findViewById(R.id.yView);
//        textView.setText(String.valueOf(currentYAcc));
//
//        textView = (TextView) findViewById(R.id.zView);
//        textView.setText(String.valueOf(currentZAcc));
//
//        textView = (TextView) findViewById(R.id.latView);
//        textView.setText(String.valueOf(currentLocation.getLatitude()));
//
//        textView = (TextView) findViewById(R.id.longView);
//        textView.setText(String.valueOf(currentLocation.getLongitude()));
//
//    }

    /** Function starts the service */
    public void startService(View view){
        if(!serviceRunning) {
            Intent startSendingIntent = new Intent(this, SensorService.class);
            this.startService(startSendingIntent);
            serviceRunning = true;
        }
    }

    public void stopService(View view){
        if(serviceRunning) {
            Intent stopSendingIntent = new Intent(this, SensorService.class);
            this.stopService(stopSendingIntent);
            serviceRunning = false;
        }
    }



//    class AsyncClass extends AsyncTask<Void, Void,Void>
//    {
//        protected Void doInBackground(Void... params)
//        {
//            hardtask();
//            return null;
//        }
//        public void hardtask()
//        {
//            // Update the SensorData object
//            sdata.currentLux = currentLux;
//            sdata.currentProx = currentProx;
//            sdata.currentXAcc = currentXAcc;
//            sdata.currentYAcc = currentYAcc;
//            sdata.currentZAcc = currentZAcc;
//            sdata.currentLong = currentLocation.getLongitude();
//            sdata.currentLat = currentLocation.getLatitude();
//
//            String out = "Lux: " + currentLux + "\n" +
//                    "Prox: " + currentProx + "\n" +
//                    "XAcc: " + currentXAcc + "\n" +
//                    "YAcc: " + currentYAcc + "\n" +
//                    "ZAcc: " + currentZAcc + "\n" +
//                    "Longitude: " + sdata.currentLong + "\n" +
//                    "Latitude: " + sdata.currentLat + "\n";
//
//            // Send the data to the server
//            try {
//                sock = new Socket(serverIP, 34000);
//                Log.e("MrSensor", "USCCECSCSCSSCSCSCSCSCECSCSSCSSECS");
//                ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
//                oos.writeObject(out);
//                oos.flush();
//            }catch (IOException e){
//                Log.e("MrSensor", "socket failed to open");
//            }
//        }
//    }
}
