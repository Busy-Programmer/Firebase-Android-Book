package com.silvertrinity.firebasehero;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Isuru on 03/09/2017.
 */

public class FirebaseHeroApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}
