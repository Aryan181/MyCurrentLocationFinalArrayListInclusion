package com.example.mycurrentlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.protobuf.StringValue;


import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.lang.String.valueOf;

public class MainActivity<sensorManager> extends AppCompatActivity implements LocationListener, SensorEventListener {
    ArrayList<String> users = new ArrayList<>();
    String collections;
    String documents;
    String latitude;
    String longitude;
    String myTime;
    String finalTime;
    SharedPreferences shared;
    ArrayList<String> arrPackage;
    int i =1;
    SensorManager sensorManager;
    boolean running=false;
    Button button_location;
    TextView textView_location;
    LocationManager locationManager;
    private final String TAG = "MainActivity";
    private FieldValue timestamp;
    String name = "AAA";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        textView_location = findViewById(R.id.text_location);
        button_location = findViewById(R.id.button_location);
        //Runtime permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }


        button_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create method

            }
        });



    }

    @SuppressLint("MissingPermission")
    private void getLocation() {

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5,MainActivity.this);
            Pusher();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude =""+ location.getLatitude();
        longitude = ""+location.getLongitude();
        Toast.makeText(this, ""+location.getLatitude()+","+location.getLongitude(), Toast.LENGTH_SHORT).show();
       // Log.e(TAG, "Latitude = "+location.getLatitude()+" Longitude = "+location.getLongitude());
        String docPt1=latitude.substring(0,latitude.indexOf('.'))+latitude.substring(latitude.indexOf('.')+1,latitude.indexOf('.')+4);
        String docPt2=longitude.substring(0,longitude.indexOf('.'))+longitude.substring(longitude.indexOf('.')+1,longitude.indexOf('.')+4);
        documents=docPt1+docPt2;
       // Log.e(TAG,""+documents);
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            String address = addresses.get(0).getAddressLine(0);
            String localPt1=address.substring(address.indexOf(',')+1);
            String localPt2=localPt1.substring(0,localPt1.indexOf(','));
            collections=localPt2;
         //   Log.e(TAG,"Here is your address = "+localPt2);
            textView_location.setText(address);

        }catch (Exception e){
            e.printStackTrace();
        }

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

    public void Pusher()
    {

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        myTime = ts.substring(0,ts.length()-2);
        Log.e(TAG,"MY TIME = "+myTime);

        // Create a Map to store the data we want to set
        Map<String, Object> docData = new HashMap<>();


        docData.put(name,FieldValue.serverTimestamp());

// Add a new document (asynchronously) in collection "cities" with id "LA"
        Task<Void> future = db.collection(collections).document(documents).update(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
              //  Log.e(TAG,"SUCCESS");

                fetchUpdates();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"fail");

            }
        });
// ...
// future.get() blocks on response




    }





    @Override
    public void onSensorChanged(SensorEvent event) {
        if(running)
        {
            getLocation();

        }


    }


    @Override
    protected void onResume()
    {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor!=null)
        {
            sensorManager.registerListener((SensorEventListener) this, countSensor,SensorManager.SENSOR_DELAY_UI);
        }
        else
        {
            Toast.makeText((this),"not found",Toast.LENGTH_SHORT).show();
        }
    }

    protected void onPause()
    {
        super.onPause();
        running = false;

    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




    public void fetchUpdates() {
         DocumentReference mDocRef = FirebaseFirestore.getInstance().collection(collections).document(documents);
        mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> dataPulled = documentSnapshot.getData();

                Set<Map.Entry<String, Object>> entrySet = dataPulled.entrySet();

                for (Map.Entry<String, Object> entry : entrySet) {
                    String key = entry.getKey();
                    String s =  valueOf( entry.getValue());
                    String unformattedTime = s;
                    if((unformattedTime.contains("=")&&(unformattedTime.contains("="))))
                    {
                      //  Log.e(TAG, "" + unformattedTime);
                        String formatTime = unformattedTime.substring(unformattedTime.indexOf('=') + 1, unformattedTime.indexOf(','));
                        String finalTime = formatTime.substring(0, formatTime.length() - 2);
                       // Log.e(TAG, " time "+finalTime);
                     if(finalTime.equals(myTime))
                     {
                         if(!(users.contains(key))) {
                             users.add(key);
                         }
                     }
                    }

                }

            }




        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "FAIL");
            }
        });

    }










}
