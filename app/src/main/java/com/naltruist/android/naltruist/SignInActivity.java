package com.naltruist.android.naltruist;

/**
 * Created by viveknarra on 10/25/16.
 */


import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.firebase.geofire.*;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.location.*;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Vivek on 10/24/2016.
 */


public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, LocationListener
        {

            protected Boolean mAddressRequested;



            private FirebaseAuth mFirebaseAuth;

    private static final String TAG = "SignInActivity";
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private static final int RC_FACEBOOK_SIGN_IN = 9002;
    private static final int REQUEST_LOCATION = 2;
    private static final String FIRST_HELP_MESSAGE = "Need Overdose Help Immediately";
    private SignInButton mSignInButton;
    private Button mEmailSignInButton;
    private Button mGetHelpButton;
    private EditText nalEmailUser;
    private EditText nalPass;
    protected String mUserAddress;
    private FirebaseAuth.AuthStateListener mAuthListener;
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private Double mLatit;
    private Double mLongit;

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        mSignInButton = (SignInButton) findViewById(R.id.sign_in_google);
        mGetHelpButton = (Button) findViewById(R.id.get_help_button);
        nalEmailUser = (EditText) findViewById(R.id.nalUserEmail);
        nalPass = (EditText) findViewById(R.id.nalPass);
        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in);

        mGetHelpButton.setOnClickListener(this);
        mEmailSignInButton.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);
//        findViewById(R.id.sign_out_menu).setOnClickListener(this);
        findViewById(R.id.create_new_account).setOnClickListener(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SignInActivity.this.getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

    if (mGoogleApiClient == null) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /*Fragment Activity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(LocationServices.API)
                .build();
    }
        //Get location of the user


        DatabaseReference geofireref = FirebaseDatabase.getInstance().getReference("geofire");
        GeoFire geoFire = new GeoFire(geofireref);
        mFirebaseAuth = FirebaseAuth.getInstance();


        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser naluser = firebaseAuth.getCurrentUser();

                DatabaseReference geofireref = FirebaseDatabase.getInstance().getReference().child("geofire");
                final GeoFire geoFire = new GeoFire(geofireref);
                if (naluser != null) {
                    if (naluser.getEmail() != null) {
                        Toast.makeText(SignInActivity.this, "Welcome back " + naluser.getEmail(), Toast.LENGTH_SHORT).show();
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
                            startIntentService();
                        }
                        mAddressRequested = true;
                        // Go back to the list activity
                        startActivity(new Intent(SignInActivity.this, RoomListActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignInActivity.this, "Welcome back Anonymous user. Sign up to be a Naltruist", Toast.LENGTH_SHORT).show();
                        String userId = naluser.getUid();
                        //get the correct location from geocoding and location listener
                        if (ActivityCompat.checkSelfPermission(SignInActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SignInActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            if (ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                            } else {
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                ActivityCompat.requestPermissions(SignInActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION);

                            }

                        } else {

                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            /*if (mLastLocation != null) {
                                Double mLatit = mLastLocation.getLatitude();
                                Double mLongit = mLastLocation.getLongitude();
                            }*/

                            if (mGoogleApiClient.isConnected() && mLastLocation != null) {
                                startIntentService();
                            }
                            mAddressRequested = true;
                            //updateUI(naluser);
                        }



                       //String location = "Folsom";//displayAddressOutput();
                        String roomKey = createNewRoom(userId,FIRST_HELP_MESSAGE,mUserAddress);

                        NalMessage nalMessage = new NalMessage();
                        createNewMessage(nalMessage, roomKey,userId);


                    // Go back to the List activity
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra("roomId", roomKey);
                        startActivity(intent);


                    }

                } else {
                    //startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    //Toast.makeText(SignInActivity.this, "Please sign in.",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User logged out.");
                }
                updateUI(naluser);
            }


        };

    }


            protected void startIntentService() {
                Intent intent = new Intent(this, FetchAddressService.class);
                intent.putExtra(Constants.RECEIVER, mResultReceiver);
                intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
                startService(intent);
            }

    private String createNewRoom(String uid, String message, String location) {
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference().child("helprooms");
        HelpRoom newRoom = new HelpRoom(uid,message,location);
        Map<String, Object> newRoomValues = newRoom.roomMap();
        String roomKey = roomRef.push().getKey();
        roomRef.child(roomKey).setValue(newRoom);
        return roomKey;


    }

    private void createNewMessage(NalMessage nalMessage, String roomKey, String uid) {
        DatabaseReference messRef = FirebaseDatabase.getInstance().getReference().child("helpmessages/"+roomKey);
        nalMessage.setText("Need Help Immediately");
        nalMessage.setName(uid);
        messRef.push().setValue(nalMessage);
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
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //getHelp();
    }




   /* private void handleFirebaseAuthResult(AuthResult authResult) {
        if (authResult != null) {
            // Welcome the user
            FirebaseUser user = authResult.getUser();
            Toast.makeText(this, "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();

            // Go back to the main activity
            startActivity(new Intent(this, RoomListActivity.class));
        }
    } */

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
            else {

            }
        }
    }


                @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_help_button:

                call911();
                getHelp();


                break;
            case R.id.sign_in_google:
                signInWithGoogle();
                break;
            case R.id.email_sign_in:
                signIn(nalEmailUser.getText().toString(), nalPass.getText().toString());
                break;
            case R.id.create_new_account:
                createAccount(nalEmailUser.getText().toString(), nalPass.getText().toString());
                break;
            default:
                return;
        }
    }


    private void call911() {

    Intent dialint = new Intent(Intent.ACTION_CALL);
        dialint.setData(Uri.parse("tel: 9163808378" ));
        if (dialint.resolveActivity(getPackageManager()) != null) {
            startActivity(dialint);
        }

        }

    private void getHelp() {
        if (mFirebaseAuth.getCurrentUser() == null) {
            mFirebaseAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "signIn Anonymously:onComple: " + task.isSuccessful());

                    if (!task.isSuccessful()) {
                        Toast.makeText(SignInActivity.this, "Anonymous verification failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        Toast.makeText(this, "Naltruists are on the way. Talk to 911 now", Toast.LENGTH_SHORT).show();
        try {
            synchronized (this) {
                wait(5000);
            }
        } catch (InterruptedException e) {
            Log.d(TAG,"Did not wait");
        }


        //TODO Implement logic for 911 receiver and responder to join chat for this user:
        //Intent newIntent = new Intent(this, RoomListActivity.class);
       // startActivity(newIntent);

    }


    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "Email SignIn: " + email);
        if(!validateForm()){
            return;
        }

       // showProgressDialog();

        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "sign in with email:onComplete: " + task.isSuccessful());

                if (!task.isSuccessful()) {
                    Log.w(TAG, "Signing in with email failed", task.getException());
                    Toast.makeText(SignInActivity.this, "Username/Password did not match", Toast.LENGTH_SHORT).show();
                }

               // hideProgressDialog();
            }
        });


    }



    private void createAccount(String email, String password) {
        Log.d(TAG, "newaccount: " + email);
        if(!validateForm()) {
            return;
        }
        //showProgressDialog();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "onComplete: "+ task.isSuccessful());
                if (!task.isSuccessful()) {
                    Toast.makeText(SignInActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                }

                startActivity(new Intent(SignInActivity.this, RoomListActivity.class));
                finish();

               // hideProgressDialog;
            }
        });
    }



    private boolean validateForm() {
        boolean valid = true;

        String email = nalEmailUser.getText().toString();
        if (TextUtils.isEmpty(email)) {
            nalEmailUser.setError("Email Required.");
            valid = false;
        } else {
            nalEmailUser.setError(null);
        }
        String password = nalPass.getText().toString();
        if(TextUtils.isEmpty(password)) {
            nalPass.setError("Password can't be blank");
            valid = false;

        } else {
            nalPass.setError(null);
        }

        return valid;
    }


    private void updateUI(FirebaseUser user) {
        //hideProgressDialog();
        if (user != null) {
            findViewById(R.id.sign_in_google).setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount gaccount = result.getSignInAccount();
                firebaseAuthWithGoogle(gaccount);
                AuthCredential googlecredential = GoogleAuthProvider.getCredential(gaccount.getIdToken(), null);
            }
            else {
                Log.e(TAG, "Google Sign In Failed.");

                Toast.makeText(SignInActivity.this, "Sign In Failed.",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (requestCode == RC_FACEBOOK_SIGN_IN) {


            } else {


            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential credential  = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());


                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithCredential", task.getException());
                    Toast.makeText(SignInActivity.this, "Sign In Failed.",Toast.LENGTH_SHORT).show();
                }
                else {
                    startActivity(new Intent(SignInActivity.this, RoomListActivity.class));
                    finish();
                }
            }
        });
    }

            @Override
            public void onConnected(Bundle connectionloc) {

                FirebaseUser naluser = mFirebaseAuth.getCurrentUser();
                naluser.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            if (ActivityCompat.checkSelfPermission(SignInActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SignInActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                if (ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                                } else {
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    ActivityCompat.requestPermissions(SignInActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                            REQUEST_LOCATION);

                                }

                            } else {
                                // permission has already been granted.


                                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                if (mLastLocation != null) {
                                    Double mLatit = mLastLocation.getLatitude();
                                    Double mLongit = mLastLocation.getLongitude();

                                }


                                    if (mAddressRequested) {
                                        startIntentService();
                                    }

                                DatabaseReference geofireref = FirebaseDatabase.getInstance().getReference().child("geofire");

                                GeoFire geoFire = new GeoFire(geofireref);
                                geoFire.setLocation(idToken, new GeoLocation(mLatit, mLongit), new GeoFire.CompletionListener()
                                {

                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        if (error != null) {
                                            System.err.println("Error" + error);
                                        } else {
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
                            if (ActivityCompat.checkSelfPermission(SignInActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SignInActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                if (ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                                } else {
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    ActivityCompat.requestPermissions(SignInActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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

                                Toast.makeText(SignInActivity.this, "Geo : "+idToken, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SignInActivity.this,getString(R.string.address_found),Toast.LENGTH_SHORT).show();
            }

        }
    }


        }

