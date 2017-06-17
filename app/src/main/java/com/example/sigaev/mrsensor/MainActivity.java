package com.example.sigaev.mrsensor;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
5) Sanitize the input
6) Ensure cleanup on stop of the service
 */

public class MainActivity extends AppCompatActivity {

    private boolean serviceRunning;

    private final String TAG = "MrSensor";
    public final String SERVER_IP_TAG = "SERVER_IP";
    public final String SERVER_PORT_TAG = "SERVER_PORT";
    public final String SERVICE_ERR_TAG = "SERVICE_ERR";

    // Error Messages
    private String ERR_MSG_INVALID_IP = "ERROR: invalid IP";
    private String ERR_MSG_INVALID_PORT = "ERROR: invalid PORT";
    private String ERR_MSG_ALREADY_RUNNING = "ERROR: Service already running";
    private String ERR_MSG_ALREADY_STOPPED = "ERROR: Service already stopped";

    // General Messages
    private String INFO_MSG_SERVICE_STARTED = "Starting the service...";
    private String INFO_MSG_SERVICE_STOPPED = "Stopping the service.";

    private BroadcastReceiver broadcastReceiver;


    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceRunning = false;

        startLocationService();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupReceiver();
    }

    public void setupReceiver(){
        final TextView errView = (TextView) findViewById(R.id.errorView);
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    errView.setText(intent.getStringExtra(SERVICE_ERR_TAG));
                    serviceRunning = false;
                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("service_status_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
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

        TextView errView = (TextView) findViewById(R.id.errorView);

        if(!serviceRunning) {

            // Fetch the configurations from the UI
            EditText editIP = (EditText)findViewById(R.id.ipText);
            EditText editPORT = (EditText)findViewById(R.id.portNumber);

            if(isEmpty(editIP)){
                errView.setText(ERR_MSG_INVALID_IP);
                return;
            }

            if(isEmpty(editPORT)){
                errView.setText(ERR_MSG_INVALID_PORT);
                return;
            }

            String ip = ((EditText)findViewById(R.id.ipText)).getText().toString();
            String port = ((EditText)findViewById(R.id.portNumber)).getText().toString();

            // TODO: Check the validity of the port and ip

            Log.e(TAG, ip + " " + port);

            // Prepare the intent
            Intent startSendingIntent = new Intent(this, SensorService.class);
            startSendingIntent.putExtra(SERVER_IP_TAG, ip);
            startSendingIntent.putExtra(SERVER_PORT_TAG, port);

            // Start the service
            this.startService(startSendingIntent);
            serviceRunning = true;
            errView.setText(INFO_MSG_SERVICE_STARTED);
            return;
        }
        errView.setText(ERR_MSG_ALREADY_RUNNING);
    }

    public void stopService(View view){

        TextView errView = (TextView) findViewById(R.id.errorView);

        if(serviceRunning) {
            Intent stopSendingIntent = new Intent(this, SensorService.class);
            this.stopService(stopSendingIntent);
            serviceRunning = false;
            errView.setText(INFO_MSG_SERVICE_STOPPED);
            return;
        }
        errView.setText(ERR_MSG_ALREADY_STOPPED);
    }


    private boolean isEmpty(EditText text) {
        return text.getText().toString().trim().length() == 0;
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
