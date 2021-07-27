package com.zectan.soundroid.Activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.zectan.soundroid.Activities.AuthActivity;
import com.zectan.soundroid.Classes.CrashDebugApplication;
import com.zectan.soundroid.MainActivity;

public class SplashScreenActivity extends CrashDebugApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            intent = new Intent(this, AuthActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
