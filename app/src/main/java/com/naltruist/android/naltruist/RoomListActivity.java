package com.naltruist.android.naltruist;

import android.*;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoomListActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public static final String ROOMS_CHILD = "helprooms";
    private  FirebaseListAdapter mRoomAdapter;
    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "RoomListActivity";
    public Double mLatit;
    public Double mLongit;
    private FirebaseUser mFirebaseUser;
    protected String mUserAddress;

    private static final int REQUEST_LOCATION = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        ListView mRoomListView = (ListView) findViewById(android.R.id.list) ;
         DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference().child(ROOMS_CHILD);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(RoomListActivity.this)
                    .enableAutoManage(RoomListActivity.this /*Fragment Activity */, RoomListActivity.this /* OnConnectionFailedListener */)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
          FirebaseListAdapter mRoomAdapter = new FirebaseListAdapter<HelpRoom>(this, HelpRoom.class, android.R.layout.two_line_list_item, roomRef) {
            @Override
            protected void populateView(View v, HelpRoom newroom, int position) {
                DatabaseReference messRef = getRef(position);
                final String key = messRef.getKey();
                if (ActivityCompat.checkSelfPermission(RoomListActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RoomListActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(RoomListActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                    } else {
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        ActivityCompat.requestPermissions(RoomListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION);

                    }

                } else {
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        Double mLatit = mLastLocation.getLatitude();
                        Double mLongit = mLastLocation.getLongitude();
                    }
                }

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View iv) {
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra("roomId", key);
                        startActivity(intent);
                    }
                });
                /*TextView name = (TextView) v.findViewById(android.R.id.text1);
                if (newroom.getHelprequestor() != null) {
                    name.setText(newroom.getHelprequestor().toString()+" @ : " + mLatit + " " + mLongit);

                } */

                TextView name = (TextView) v.findViewById(android.R.id.text1);
                if (mFirebaseUser.getDisplayName() != null) {
                    name.setText(mFirebaseUser.getDisplayName().toString());

                } else {
                    if (newroom.getLocation() != null) {
                        name.setText("Anonymous Help Seeker at :" + newroom.getLocation());
                    } else {
                        name.setText("Anonymous Help Seeker at : unknown location" );
                    }
                }
                TextView message = (TextView) v.findViewById(android.R.id.text2);
                if (newroom.getTitle() != null) {
                    message.setText(newroom.getTitle().toString());
                }


            }


        };
        mRoomListView.setAdapter(mRoomAdapter);





    }

    private void updateMessage(String uid, String message, String location) {
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference().child("helprooms");

        String key = roomRef.child("helprooms").child(uid).getKey();
        HelpRoom updateRoom = new HelpRoom(uid, message,location);


        Map<String, Object> newRoomValues = updateRoom.roomMap();

        Map<String, Object> roomUpdates = new HashMap<>();
        roomUpdates.put("/helprooms/" + uid + "/" + key, newRoomValues);
        roomUpdates.put("/user-helprooms/"  + key, newRoomValues);

        roomRef.updateChildren(roomUpdates);
    }

    @Override
    public void onConnected(Bundle connectionloc) {

        final FirebaseUser naluser = mFirebaseAuth.getCurrentUser();
        //String uuid = naluser.getUid();
        naluser.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    if (ActivityCompat.checkSelfPermission(RoomListActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RoomListActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(RoomListActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                        } else {
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            ActivityCompat.requestPermissions(RoomListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION);

                        }

                    } else {
                        // permission has already been granted.

                        String uuid = naluser.getUid();
                        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        final Double mLatit = mLastLocation.getLatitude();
                        Double mLongit = mLastLocation.getLongitude();
                        DatabaseReference geofireref = FirebaseDatabase.getInstance().getReference().child("geofire");

                        GeoFire geoFire = new GeoFire(geofireref);
                        geoFire.setLocation(uuid, new GeoLocation(mLatit, mLongit), new GeoFire.CompletionListener() {

                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error != null) {
                                    System.err.println("Error" + error);
                                } else {
                                    //Toast.makeText(RoomListActivity.this, "Geo : "+mLatit, Toast.LENGTH_SHORT).show();
                                    System.out.println("Saved successfully");

                                }
                            }
                        });

                    }

                } else {
                    Log.e(TAG, "No token for this user: ");
                }
            }
        });


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"ConnectionSuspended: " + i) ;
    }

    @Override
    public void onLocationChanged(final Location location) {
        FirebaseUser naluser = mFirebaseAuth.getCurrentUser();
        naluser.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    if (ActivityCompat.checkSelfPermission(RoomListActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RoomListActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        if (ActivityCompat.shouldShowRequestPermissionRationale(RoomListActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                        } else {
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            ActivityCompat.requestPermissions(RoomListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION);

                        }

                    } else {
                        // permission has already been granted.


                        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        Double mLatit = mLastLocation.getLatitude();
                        Double mLongit = mLastLocation.getLongitude();
                        DatabaseReference geofireref = FirebaseDatabase.getInstance().getReference().child("geofire");
                        GeoFire geoFire = new GeoFire(geofireref);
                        geoFire.setLocation(idToken, new GeoLocation(mLatit,mLongit));

                        Toast.makeText(RoomListActivity.this, "Geo : "+idToken, Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Log.e(TAG, "No token for this user: ");
                }
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void  onDestroy() {
        super.onDestroy();

    }

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            //   displayAddressOutput();
            FirebaseUser naluser = mFirebaseAuth.getCurrentUser();
            String uid = naluser.getUid();
            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                mUserAddress = mAddressOutput;
                updateMessage(uid, "New Location of anonymous help seeker", mUserAddress);
                Toast.makeText(RoomListActivity.this,getString(R.string.address_found),Toast.LENGTH_SHORT).show();
            }

        }
    }



}
