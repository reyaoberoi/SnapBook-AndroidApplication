package com.example.snapbook;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

// Stub version of AuthManager without Firebase dependencies
public class AuthManager {
    
    private AppCompatActivity activity;
    
    public AuthManager(AppCompatActivity activity) {
        this.activity = activity;
    }
    
    public AuthManager(AppCompatActivity activity, Object callback) {
        this.activity = activity;
        // Ignore callback for stub
    }
    
    public void signInWithEmail(String email, String password, AuthCallback callback) {
        // Stub implementation
        if (callback != null) {
            callback.onAuthSuccess("Stub User", false);
        }
    }
    
    public void signUpWithEmail(String email, String password, AuthCallback callback) {
        // Stub implementation
        if (callback != null) {
            callback.onAuthSuccess("New User", false);
        }
    }
    
    public void signInWithGoogle() {
        // Stub implementation - do nothing
    }
    
    public void handleGoogleSignInResult(Intent data) {
        // Stub implementation - do nothing
    }
    
    public Object getCurrentUser() {
        // Return null for stub
        return null;
    }
    
    public void signOut() {
        // Stub implementation - do nothing
    }
    
    public void continueAsGuest() {
        // Stub implementation - do nothing
    }
    
    // Add missing constant
    public static final int RC_SIGN_IN = 9001;
    
    public interface AuthCallback {
        void onAuthSuccess(String userName, boolean isGuest);
        void onAuthFailure(String errorMessage);
    }
}