package com.zectan.soundroid.Activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.zectan.soundroid.Classes.CrashDebugApplication;
import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Connections.VersionCheckRequest;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Utils.Utils;

public class SplashScreenActivity extends CrashDebugApplication {
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            mIntent = new Intent(this, AuthActivity.class);
        } else {
            mIntent = new Intent(this, MainActivity.class);
        }

        new VersionCheckRequest(new Request.Callback() {
            @Override
            public void onComplete(String version) {
                if (!Utils.versionAtLeast(version)) {
                    mIntent = new Intent(SplashScreenActivity.this, UpdateActivity.class);
                    mIntent.putExtra("version", version);
                }
                startActivity(mIntent);
                finish();
            }

            @Override
            public void onError(String message) {
                startActivity(mIntent);
                finish();
            }
        });


    }
}
