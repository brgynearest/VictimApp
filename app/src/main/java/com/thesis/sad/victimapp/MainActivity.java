package com.thesis.sad.victimapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thesis.sad.victimapp.Common.Common;
import com.thesis.sad.victimapp.Model.Victim;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Button signinbtn,registerbtn;

    FirebaseDatabase db;
    FirebaseAuth auth;
    DatabaseReference victimuser;
    RelativeLayout rootLayout;

    TextView forgotpassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signinbtn = findViewById(R.id.btn_sign_in);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        victimuser = db.getReference(Common.victim_information);
        registerbtn = findViewById(R.id.btn_register);
        rootLayout = findViewById(R.id.rootLayout);

        forgotpassword = (TextView) findViewById(R.id.forgot_password);
        forgotpassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialogForgot();
                return false;
            }
        });

        signinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

    }

    private void showDialogForgot() {
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(MainActivity.this);
        alertdialog.setTitle("FORGOT PASSWORD");
        alertdialog.setMessage("Please enter your email address");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forgot = inflater.inflate(R.layout.layout_forgot_password,null);
        final EditText editText = forgot.findViewById(R.id.forgottext);
        alertdialog.setView(forgot);



        alertdialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
auth.sendPasswordResetEmail(editText.getText().toString().trim())
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                Snackbar.make(rootLayout,"Reset password link has been sent",Snackbar.LENGTH_LONG).show();

            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Snackbar.make(rootLayout,"" +e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

            }
        });
                alertdialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alertdialog.show();


    }


    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to Sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final EditText edittext_email = login_layout.findViewById(R.id.edittext_email);
        final EditText edittext_password = login_layout.findViewById(R.id.edittext_password);

        dialog.setView(login_layout);

        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                signinbtn.setEnabled(false);

                if (TextUtils.isEmpty(edittext_email.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter your email address", Snackbar.LENGTH_SHORT).show();
                    return;

                }
                if (TextUtils.isEmpty(edittext_password.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter your password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (edittext_password.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password too short!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                auth.signInWithEmailAndPassword(edittext_email.getText().toString(),edittext_password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                startActivity(new Intent(MainActivity.this,Welcome.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                signinbtn.setEnabled(true);
                            }
                        });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }


    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);

        final EditText edittext_email = register_layout.findViewById(R.id.edittext_email);
        final EditText edittext_password = register_layout.findViewById(R.id.edittext_password);
        final EditText edittext_name = register_layout.findViewById(R.id.edittext_name);
        final EditText edittext_phone = register_layout.findViewById(R.id.edittext_phone);

        dialog.setView(register_layout);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(TextUtils.isEmpty(edittext_email.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edittext_password.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edittext_name.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your Name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edittext_phone.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your phone", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edittext_password.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout,"Password too short!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(edittext_email.getText().toString()
                        ,edittext_password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Victim victim = new Victim();
                                victim.setEmail(edittext_email.getText().toString());
                                victim.setPassword(edittext_password.getText().toString());
                                victim.setName(edittext_name.getText().toString());
                                victim.setPhone(edittext_phone.getText().toString());

                                victimuser.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(victim)
                                        .addOnSuccessListener(new OnSuccessListener<Void>()    {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout,"Registered!", Snackbar.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout,"Failed in Registration" +e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Failed", Snackbar.LENGTH_SHORT).show();
                                Log.d(TAG,"Failed saving on Firebase: "+ e.getMessage());
                            }
                        });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();

    }



}