package com.zectan.soundroid.classes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.zectan.soundroid.ErrorActivity;

import java.util.ArrayList;

public abstract class CrashDebugApplication extends AppCompatActivity {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            ArrayList<String> stack = new ArrayList<>();
            for (StackTraceElement el : e.getStackTrace()) stack.add(el.toString());

            String message = e.getMessage();
            String class_ = e.getClass().getName();
            String userId = auth.getUid();

            Intent intent = new Intent(CrashDebugApplication.this, ErrorActivity.class);
            intent.putExtra("stack", stack);
            intent.putExtra("message", message);
            intent.putExtra("class", class_);
            intent.putExtra("userId", userId);

            startActivity(intent);
            Runtime.getRuntime().exit(1);
        });

    }
}
