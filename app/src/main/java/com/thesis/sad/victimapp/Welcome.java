package com.thesis.sad.victimapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.thesis.sad.victimapp.Common.Common;
import com.thesis.sad.victimapp.Helper.CustomInfoWindow;
import com.thesis.sad.victimapp.Model.BottomSheetVictim;
import com.thesis.sad.victimapp.Model.Notification;
import com.thesis.sad.victimapp.Model.FCMResponse;
import com.thesis.sad.victimapp.Model.Sender;
import com.thesis.sad.victimapp.Model.Token;
import com.thesis.sad.victimapp.Model.Victim;
import com.thesis.sad.victimapp.Remote.IFCMService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Welcome extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    SupportMapFragment mapFragment;
    private static final String TAG = "Welcome";
    GoogleMap mMap;
    private GoogleApiClient mgoogleApiClient;

    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICES_RES_REQUEST = 300193;

    private LocationRequest mlocationRequest;
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    public GeoFire geoFire;
    private DatabaseReference databaseReference;
    private Marker mUserMarker;
    private ImageView imgexpdable;
    private BottomSheetVictim mbottomSheet;
    private Button btnPickUp;
    private Location mLastLocation;

    //set a flag for getting the status of the drivers
    boolean isAmbulanceFound = false;
    String ambulanceid="";
    int radius = 1; //1km
    int distance = 1; //3km
    private static final int LIMIT = 3;

    IFCMService mservices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mservices = Common.getFCMService();


       /* databaseReference = FirebaseDatabase.getInstance().getReference("Victim");
        geoFire = new GeoFire(databaseReference);*/

        imgexpdable = findViewById(R.id.expandable);
        mbottomSheet = BottomSheetVictim.newInstance("VictimBottomSheet");
        imgexpdable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mbottomSheet.show(getSupportFragmentManager(),mbottomSheet.getTag());
            }
        });
        btnPickUp = findViewById(R.id.GetHelpRequest);
        btnPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if(!isAmbulanceFound)
                        requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    else
                        SendRequestToAmbulance(ambulanceid);



            }
        });

        setUpLocation();
        updateFirebaseToken();

    }

    private void updateFirebaseToken() {

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference tokens = db.getReference(Common.token_tbl);
            Token token = new Token(FirebaseInstanceId.getInstance().getToken());
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);


    }

    private void SendRequestToAmbulance(String ambulanceid) {
            DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
            tokens.orderByKey().equalTo(ambulanceid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot postsnapshot:dataSnapshot.getChildren()){
                                Token token= postsnapshot.getValue(Token.class);
                                String json_lat_lang = new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                                Notification data = new Notification("BRGYTRACKING",json_lat_lang);
                                Sender content = new Sender(token.getToken(),data);

                                mservices.sendMessage(content)
                                        .enqueue(new Callback<FCMResponse>() {
                                            @Override
                                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                if(response.body().success == 1)
                                                    Toast.makeText(Welcome.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(Welcome.this, "Failed!", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(Call<FCMResponse> call, Throwable t) {
                                                Log.e(TAG,"Error" +t.getMessage());

                                            }
                                        });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

    }


    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void createLocationRequest() {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FATEST_INTERVAL);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RES_REQUEST).show();
            else{
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case  MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                            displayLocation();
                    }
                }
                break;
        }
    }


    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
        if (mLastLocation!=null) {

            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            if (mUserMarker != null)
                mUserMarker.remove();
                mUserMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(String.format("Me")));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 18.0f));

                loadAllAvailableAmbulance();






        }
        else
        {
            Log.d("ERROR","Cannot get your Location");
        }
        }

    private void loadAllAvailableAmbulance() {
            //load all available ambulance in 3km area

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Common.available_Ambulance);
        GeoFire gf = new GeoFire(databaseReference);
        GeoQuery geoq = gf.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),distance);
        geoq.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(Common.barangay_ambulance)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Victim victim = dataSnapshot.getValue(Victim.class);

                                mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.latitude,location.longitude))
                                        .flat(true)
                                        .title(victim.getName())
                                        .snippet("Phone: " +victim.getPhone())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                );
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <=LIMIT){ //find all ambulance with 3km range
                    distance++;
                    loadAllAvailableAmbulance();

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });



    }


    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {

            return;

        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient,mlocationRequest,this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mgoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    private void requestPickupHere(String uid) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.pickup_request);
            GeoFire mGeoFire = new GeoFire(reference);
            mGeoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });

            if (mUserMarker.isVisible())
                mUserMarker.remove();

            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .title("Pickup Here")
                    .snippet("")
                    .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            mUserMarker.showInfoWindow();

            btnPickUp.setText("Getting your Responder...");

            findAmbulance();


    }

    private void findAmbulance() {
        DatabaseReference ambulance = FirebaseDatabase.getInstance().getReference(Common.available_Ambulance);
        GeoFire mmgeofire = new GeoFire(ambulance);
        GeoQuery geoQuery = mmgeofire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //If the nearest barangay amblance is found

                if(!isAmbulanceFound){

                    isAmbulanceFound=true;
                    ambulanceid=key;
                    btnPickUp.setText("Call Ambulance");
                    Toast.makeText(Welcome.this, ""+ key, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //If still not found the brgy ambulance
                if(!isAmbulanceFound){
                    radius++;
                    findAmbulance();

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }


}