package com.zectan.soundroid.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.ActivityErrorBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ErrorActivity extends AppCompatActivity {
    private static final String TAG = "(SounDroid) ErrorActivity";
    public final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private ActivityErrorBinding B;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = ActivityErrorBinding.inflate(LayoutInflater.from(this));
        setContentView(B.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.red));

        // Listeners
        B.reloadImage.setOnClickListener(this::onReloadImageClicked);
        B.arrowImage.setOnClickListener(this::onArrowImageClicked);

        try {
            Bundle extras = getIntent().getExtras();
            List<String> stack = extras.getStringArrayList("stack");
            String message = extras.getString("message", "Error Message not provided");
            String class_ = extras.getString("class", "class.not.Defined");
            String userId = extras.getString("userId");

            // Set HTML as the text because HTML was easier to format
            B.errorText.setText(Html.fromHtml(String.format("<h1>%s</h1><br /><h5>%s</h5>", message, class_), Html.FROM_HTML_MODE_COMPACT));
            B.stackText.setText(Html.fromHtml(
                stack
                    .stream()
                    .map(line -> {
                        // Use Regex to print the stack with HTML
                        Pattern pattern = Pattern.compile("(.*)\\((.*)\\)");
                        Matcher matcher = pattern.matcher(line);

                        if (matcher.find()) {
                            // If the line matches the Regex
                            String path = matcher.group(1);
                            String file = matcher.group(2);
                            assert path != null;

                            return String.format(
                                "%s<br /> <b>%s</b>",
                                path.replace(".", "<br />"),
                                file
                            );
                        } else {
                            return line;
                        }
                    })
                    .collect(Collectors.joining("<br /><br />")),
                Html.FROM_HTML_MODE_COMPACT
            ));

            // Errors to ignore
            List<String> IgnoredErrors = new ArrayList<>();
            IgnoredErrors.add("Could not check for latest version");
            IgnoredErrors.add("A Runtime Exception was thrown!");

            if (!IgnoredErrors.contains(message)) {
                Map<String, Object> error = new HashMap<>();
                error.put("type", "Uncaught");
                error.put("date", Calendar.getInstance().getTime().toString());
                error.put("message", message);
                error.put("class", class_);
                error.put("stack", stack);
                error.put("userId", userId);

                // Store error in Firestore
                mDb.collection("errors")
                    .add(error)
                    .addOnSuccessListener(__ -> Log.i(TAG, "Error stored successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error stored unsuccessfully: " + e.getMessage()));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void onReloadImageClicked(View view) {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        startActivity(intent);
        finish();
    }

    private void onArrowImageClicked(View view) {
        if (B.parent.getProgress() >= 0.5) {
            B.parent.transitionToStart();
        } else {
            B.parent.transitionToEnd();
        }
    }
}