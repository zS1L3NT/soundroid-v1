package com.zectan.soundroid.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.zectan.soundroid.BuildConfig;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.ActivityUpdateBinding;

public class UpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUpdateBinding b = ActivityUpdateBinding.inflate(LayoutInflater.from(this));
        setContentView(b.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.green));

        String version = getIntent().getExtras().getString("version", "?");
        b.messageText.setText(String.format("v%s  ➔  v%s", BuildConfig.VERSION_NAME, version));
        b.openBrowserImage.setOnClickListener(this::onOpenWebsiteClicked);
    }

    private void onOpenWebsiteClicked(View view) {
        Intent browserIntent = new Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://soundroid.zectan.com")
        );
        startActivity(browserIntent);
    }
}