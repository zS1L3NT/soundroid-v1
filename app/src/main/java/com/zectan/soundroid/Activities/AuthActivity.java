package com.zectan.soundroid.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.Classes.CrashDebugApplication;
import com.zectan.soundroid.Connections.PlaylistsDefaultRequest;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.User;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.ActivityAuthBinding;

public class AuthActivity extends CrashDebugApplication {
    private static final String TAG = "(SounDroid) AuthActivity";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private ActivityAuthBinding B;
    private GoogleSignInClient mGoogleSignInClient;
    private final ActivityResultLauncher<Intent> signInWithGoogleLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            // After Google sign in popup is finished
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Snackbar.make(B.getRoot(), "Google sign in failed", Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, e.getMessage());
                restoreButton();
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = ActivityAuthBinding.inflate(LayoutInflater.from(this));
        setContentView(B.getRoot());

        // Google sign in options object
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Listeners
        B.signInButton.setOnClickListener(this::signInWithGoogle);
    }

    /**
     * Start the google sign in process
     *
     * @param view View
     */
    public void signInWithGoogle(View view) {
        // Animate button to loading state
        B.googleIcon.animate().alpha(0).setDuration(250).start();
        B.signInText.animate().alpha(0).setDuration(250).start();
        B.loadingCircle.animate().alpha(1).setDuration(250).start();
        B.signInButton.setOnClickListener(__ -> {
        });

        // Launch the google sign in popup
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInWithGoogleLauncher.launch(signInIntent);
    }

    /**
     * Google authentication complete, not Firebase authentication
     *
     * @param idToken ID Token...?
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth
            .signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Firebase authentication successful
                    addToFirestore();
                } else {
                    // Firebase authentication failed
                    Snackbar.make(B.getRoot(), "Firebase sign in failed", Snackbar.LENGTH_SHORT).show();
                    if (task.getException() != null) Log.e(TAG, task.getException().getMessage());
                    restoreButton();
                }
            });
    }

    /**
     * Google and Firebase authentication successful
     */
    private void addToFirestore() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        assert firebaseUser != null;
        DocumentReference userRef = mDb.collection("users").document(firebaseUser.getUid());

        userRef.get()
            .addOnSuccessListener(snap -> {
                // If user exists, continue to MainActivity
                if (snap.exists()) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    restoreButton();
                    return;
                }

                Uri photoUri = firebaseUser.getPhotoUrl();
                String photoUrl = photoUri != null ? photoUri.toString() : getString(R.string.default_profile_icon);

                // Create user object with default values
                User user = new User(
                    firebaseUser.getUid(),
                    firebaseUser.getDisplayName(),
                    photoUrl,
                    true,
                    true,
                    true,
                    5,
                    "Dark"
                );

                userRef.set(user.toMap())
                    .addOnSuccessListener(__ -> {
                        // Creation of user successful, inflate default playlists and redirect
                        new PlaylistsDefaultRequest(user.getId());
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        restoreButton();
                    })
                    .addOnFailureListener(error -> {
                        // Failed to create use in Firestore
                        Snackbar.make(B.getRoot(), "Error creating user in database", Snackbar.LENGTH_SHORT).show();
                        error.printStackTrace();
                        restoreButton();
                        mAuth.signOut();
                    });
            })
            .addOnFailureListener(error -> {
                // Failed to find user in Firestore
                Snackbar.make(B.getRoot(), "Error finding user in database", Snackbar.LENGTH_SHORT).show();
                error.printStackTrace();
                restoreButton();
                mAuth.signOut();
            });
    }

    /**
     * Restore button animations to default
     */
    private void restoreButton() {
        B.googleIcon.animate().alpha(1).setDuration(250).start();
        B.signInText.animate().alpha(1).setDuration(250).start();
        B.loadingCircle.animate().alpha(0).setDuration(250).start();
        new Handler().postDelayed(() -> B.signInButton.setOnClickListener(this::signInWithGoogle), 250);
    }
}