package com.tech.thrithvam.bakeryapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NetworkError extends AppCompatActivity {
    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_error);
        getSupportActionBar().setElevation(0);
        db=DatabaseHandler.getInstance(this);
        Button retry=(Button)findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline()){
                        if(db.GetCategories().size()>0){
                            Intent intentHome = new Intent(NetworkError.this, Home.class);
                            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intentHome);
                            finish();
                            overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                        }
                        else {
                            Intent intentHome = new Intent(NetworkError.this, SplashScreen.class);
                            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intentHome);
                            finish();
                        }
                }
            }
        });
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
