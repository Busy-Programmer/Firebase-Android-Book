package com.silvertrinity.firebasehero;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class SplashActivity extends AppCompatActivity {

    // Declaration of Firebase Remote Config variable
    FirebaseRemoteConfig mRemoteConfig;

    // final variable declaration for Remote Config
    private final String REMOTE_CONFIG_IS_PROMOTION_AVAILABLE = "is_promotion_available";
    // final variable declaration for Splash Duration
    private final int SPLASH_DURATION = 10000;

    // TextView for promotion
    private TextView promotionTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // gets a Remote Config object instance
        // enables developer mode to allow for frequent refreshes of the cache
        mRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings remoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        mRemoteConfig.setConfigSettings(remoteConfigSettings);

        mRemoteConfig.setDefaults(R.xml.default_remote_config_values);

        // Initialize the TextView for promotion
        promotionTv = (TextView) findViewById(R.id.promotion_tv);

        // Calls the method to fetch remote config values.
        fetchRemoteConfigValues();

        // Starts the MainActivity after SPLASH_DURATION duration
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DURATION);

    }

    private void fetchRemoteConfigValues(){
        // cache expiration in seconds
        long cacheExpiration = 3600; // 1 hour in seconds

        // If in developer mode cacheExpiration is set to 0 so each fetch
        // will retrieve values from the server
        if(mRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()){
            cacheExpiration = 0;
        }

        // fetches the values and listen for fails.
        mRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            Log.d("SplashActivity", "Feched Remote Config.");

                            // Once the config is successful, it must be activated
                            mRemoteConfig.activateFetched();
                        }else{

                            Log.d("SplashActivity", "Remote Config Failed.");

                        }

                        //display values
                        setupValue();
                    }
                });

    }

    private void setupValue(){
        // Gets the Remote Config value for promotion
        boolean isPromotionAvailable = mRemoteConfig
                .getBoolean(REMOTE_CONFIG_IS_PROMOTION_AVAILABLE);

        //If the Remote Config value is true show promotion text
        if(isPromotionAvailable){
            promotionTv.setText("50% Off Exclusively For You!");
        }else {
            Log.d("SplashActivity", "No Promotion Available!");
        }
    }



}
