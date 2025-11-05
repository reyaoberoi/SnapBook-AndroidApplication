package com.example.snapbook;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import java.util.*;

public class VintagePhotoBoothActivity extends AppCompatActivity implements
        AuthManager.AuthCallback,
        CameraManager.CaptureCallback {

    private static final String TAG = "VintagePhotoBoothActivity";

    private AuthManager authManager;
    private CameraManager cameraManager;
    private FilterManager filterManager;

    private LinearLayout authContainer;
    private LinearLayout welcomeContainer;
    private RelativeLayout cameraContainer;
    private LinearLayout resultsContainer;

    private PreviewView previewView;
    private ImageView photoStripView;
    private TextView currentFilterText;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText sessionCodeInput;

    private String currentUser;
    private boolean isGuest = false;
    private String currentSession;
    private int selectedPhotoCount = 4;
    private final List<Bitmap> capturedPhotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_vintage_photobooth);
            initializeManagers();
            initializeViews();
            setupClickListeners();
            showAuthScreen();

            Log.d(TAG, "VintagePhotoBoothActivity initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing VintagePhotoBoothActivity", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeManagers() {
        authManager = new AuthManager(this, this);
        filterManager = new FilterManager();
    }

    private void initializeViews() {
        authContainer = findViewById(R.id.auth_container);
        welcomeContainer = findViewById(R.id.welcome_container);
        cameraContainer = findViewById(R.id.camera_container);
        resultsContainer = findViewById(R.id.results_container);

        previewView = findViewById(R.id.preview_view);
        photoStripView = findViewById(R.id.photo_strip_view);
        currentFilterText = findViewById(R.id.current_filter_text);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        sessionCodeInput = findViewById(R.id.session_code_input);

        cameraManager = new CameraManager(this, previewView);
        cameraManager.setCaptureCallback(this);
    }

    private void setupClickListeners() {
        findViewById(R.id.google_sign_in_btn).setOnClickListener(v -> handleGoogleSignIn());
        findViewById(R.id.email_sign_in_btn).setOnClickListener(v -> handleEmailSignIn());
        findViewById(R.id.email_sign_up_btn).setOnClickListener(v -> handleEmailSignUp());
        findViewById(R.id.guest_btn).setOnClickListener(v -> authManager.continueAsGuest());

        findViewById(R.id.start_solo_btn).setOnClickListener(v -> startSoloSession());
        findViewById(R.id.create_collab_btn).setOnClickListener(v -> createCollaborativeSession());
        findViewById(R.id.join_session_btn).setOnClickListener(v -> joinSession());
        findViewById(R.id.view_gallery_btn).setOnClickListener(v -> openGallery());

        findViewById(R.id.capture_btn).setOnClickListener(v -> capturePhoto());
        findViewById(R.id.camera_toggle_btn).setOnClickListener(v -> cameraManager.toggleCamera());
        findViewById(R.id.back_btn).setOnClickListener(v -> returnToWelcome());

        findViewById(R.id.filter_sepia).setOnClickListener(v -> setFilter("sepia"));
        findViewById(R.id.filter_polaroid).setOnClickListener(v -> setFilter("polaroid"));
        findViewById(R.id.filter_kodachrome).setOnClickListener(v -> setFilter("kodachrome"));
        findViewById(R.id.filter_vintage).setOnClickListener(v -> setFilter("vintage"));
        findViewById(R.id.filter_bw).setOnClickListener(v -> setFilter("bw"));

        findViewById(R.id.download_btn).setOnClickListener(v -> downloadPhotoStrip());
        findViewById(R.id.download_all_btn).setOnClickListener(v -> downloadAllPhotos());
        findViewById(R.id.new_session_btn).setOnClickListener(v -> returnToWelcome());
        
        // Back to home button
        findViewById(R.id.home_btn).setOnClickListener(v -> {
            finish(); // Return to MainActivity
        });
    }

    @Override
    public void onAuthSuccess(String userName, boolean isGuest) {
        this.currentUser = userName;
        this.isGuest = isGuest;
        Log.d(TAG, "Auth success: " + userName);
        showWelcomeScreen();
    }

    @Override
    public void onAuthFailure(String errorMessage) {
        Log.e(TAG, "Auth failure: " + errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void handleGoogleSignIn() {
        authManager.signInWithGoogle();
    }

    private void handleEmailSignIn() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        authManager.signInWithEmail(email, password, this);
    }

    private void handleEmailSignUp() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        authManager.signUpWithEmail(email, password, this);
    }

    private void startSoloSession() {
        selectedPhotoCount = 4;
        currentSession = null;
        capturedPhotos.clear();
        showCameraScreen();
    }

    private void createCollaborativeSession() {
        currentSession = generateSessionCode();
        capturedPhotos.clear();
        Toast.makeText(this, "Session Code: " + currentSession, Toast.LENGTH_LONG).show();
        showCameraScreen();
    }

    private void joinSession() {
        String sessionCode = sessionCodeInput.getText().toString().trim().toUpperCase();
        if (sessionCode.isEmpty()) {
            Toast.makeText(this, "Please enter a session code", Toast.LENGTH_SHORT).show();
            return;
        }
        currentSession = sessionCode;
        capturedPhotos.clear();
        showCameraScreen();
    }

    private String generateSessionCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    @Override
    public void onCaptureSuccess(Bitmap bitmap) {
        Log.d(TAG, "Photo captured successfully");

        Bitmap filteredBitmap = filterManager.applyCurrentFilter(bitmap);
        capturedPhotos.add(filteredBitmap);

        if (capturedPhotos.size() >= selectedPhotoCount) {
            showResultsScreen();
        } else {
            String filterName = filterManager.getCurrentFilter().getDisplayName();
            Toast.makeText(this,
                    "Photo " + capturedPhotos.size() + "/" + selectedPhotoCount +
                            " captured with " + filterName + " filter!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCaptureError(String errorMessage) {
        Log.e(TAG, "Capture error: " + errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void capturePhoto() {
        Log.d(TAG, "Capture button clicked");
        cameraManager.capturePhoto();
    }

    private void setFilter(String filterName) {
        filterManager.setCurrentFilter(filterName);
        updateFilterUI();

        String displayName = filterManager.getCurrentFilter().getDisplayName();
        Toast.makeText(this, "Filter: " + displayName, Toast.LENGTH_SHORT).show();
    }

    private void updateFilterUI() {
        if (currentFilterText != null) {
            String displayName = filterManager.getCurrentFilter().getDisplayName();
            currentFilterText.setText("Current Filter: " + displayName);
        }

        int brownColor = ContextCompat.getColor(this, R.color.btn_brown);
        int goldColor = ContextCompat.getColor(this, R.color.btn_gold);

        findViewById(R.id.filter_sepia).setBackgroundColor(brownColor);
        findViewById(R.id.filter_polaroid).setBackgroundColor(brownColor);
        findViewById(R.id.filter_kodachrome).setBackgroundColor(brownColor);
        findViewById(R.id.filter_vintage).setBackgroundColor(brownColor);
        findViewById(R.id.filter_bw).setBackgroundColor(brownColor);

        FilterManager.FilterType currentFilter = filterManager.getCurrentFilter();
        switch (currentFilter) {
            case SEPIA:
                findViewById(R.id.filter_sepia).setBackgroundColor(goldColor);
                break;
            case POLAROID:
                findViewById(R.id.filter_polaroid).setBackgroundColor(goldColor);
                break;
            case KODACHROME:
                findViewById(R.id.filter_kodachrome).setBackgroundColor(goldColor);
                break;
            case VINTAGE:
                findViewById(R.id.filter_vintage).setBackgroundColor(goldColor);
                break;
            case BLACK_AND_WHITE:
                findViewById(R.id.filter_bw).setBackgroundColor(goldColor);
                break;
        }
    }

    private void downloadPhotoStrip() {
        if (photoStripView.getDrawable() == null) {
            Toast.makeText(this, "No photo strip to download", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasStoragePermission()) {
            requestStoragePermission();
            return;
        }

        try {
            // Get bitmap from ImageView
            photoStripView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(photoStripView.getDrawingCache());
            photoStripView.setDrawingCacheEnabled(false);

            // Save to device
            java.io.File picturesDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            java.io.File photoFile = new java.io.File(picturesDir, "vintage_photo_strip_" + System.currentTimeMillis() + ".jpg");
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.close();

            // Add to media store for newer Android versions
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "vintage_photo_strip_" + System.currentTimeMillis());
                values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES);

                android.net.Uri uri = getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    java.io.OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                    outputStream.close();
                }
            } else {
                // For older versions
                android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,
                        "vintage_photo_strip", "Vintage PhotoBooth Strip");
            }

            Toast.makeText(this, "Photo strip saved to gallery!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving photo strip: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadAllPhotos() {
        if (capturedPhotos.isEmpty()) {
            Toast.makeText(this, "No photos to download", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasStoragePermission()) {
            requestStoragePermission();
            return;
        }

        try {
            java.io.File picturesDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            int savedCount = 0;

            for (int i = 0; i < capturedPhotos.size(); i++) {
                Bitmap bitmap = capturedPhotos.get(i);
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    // Use MediaStore for newer Android versions
                    android.content.ContentValues values = new android.content.ContentValues();
                    values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "vintage_photo_" + System.currentTimeMillis() + "_" + (i + 1));
                    values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES);

                    android.net.Uri uri = getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        java.io.OutputStream outputStream = getContentResolver().openOutputStream(uri);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                        outputStream.close();
                        savedCount++;
                    }
                } else {
                    // For older versions
                    java.io.File photoFile = new java.io.File(picturesDir, 
                        "vintage_photo_" + System.currentTimeMillis() + "_" + (i + 1) + ".jpg");
                    
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                    fos.close();

                    android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,
                            "vintage_photo_" + (i + 1), "Vintage PhotoBooth Photo");
                    savedCount++;
                }
            }

            Toast.makeText(this, savedCount + " photos saved to gallery!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving photos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(VintagePhotoBoothActivity.this, GalleryActivity.class);
        startActivity(intent);
    }

    private void returnToWelcome() {
        capturedPhotos.clear();
        currentSession = null;
        cameraManager.stopCamera();
        showWelcomeScreen();
    }

    private void showAuthScreen() {
        authContainer.setVisibility(LinearLayout.VISIBLE);
        welcomeContainer.setVisibility(LinearLayout.GONE);
        cameraContainer.setVisibility(RelativeLayout.GONE);
        resultsContainer.setVisibility(LinearLayout.GONE);
    }

    private void showWelcomeScreen() {
        authContainer.setVisibility(LinearLayout.GONE);
        welcomeContainer.setVisibility(LinearLayout.VISIBLE);
        cameraContainer.setVisibility(RelativeLayout.GONE);
        resultsContainer.setVisibility(LinearLayout.GONE);
    }

    private void showCameraScreen() {
        authContainer.setVisibility(LinearLayout.GONE);
        welcomeContainer.setVisibility(LinearLayout.GONE);
        cameraContainer.setVisibility(RelativeLayout.VISIBLE);
        resultsContainer.setVisibility(LinearLayout.GONE);

        updateFilterUI();
        cameraManager.startCamera();
    }

    private void showResultsScreen() {
        authContainer.setVisibility(LinearLayout.GONE);
        welcomeContainer.setVisibility(LinearLayout.GONE);
        cameraContainer.setVisibility(RelativeLayout.GONE);
        resultsContainer.setVisibility(LinearLayout.VISIBLE);

        cameraManager.stopCamera();
        createAndDisplayPhotoStrip();
    }

    private void createAndDisplayPhotoStrip() {
        Bitmap photoStrip = PhotoStripCreator.createPhotoStrip(capturedPhotos);
        if (photoStrip != null) {
            photoStripView.setImageBitmap(photoStrip);
        } else {
            Toast.makeText(this, "Error creating photo strip", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AuthManager.RC_SIGN_IN) {
            authManager.handleGoogleSignInResult(data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CameraManager.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                cameraManager.startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
                returnToWelcome();
            }
        }
    }

    private boolean hasStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED;
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            return true; // Scoped storage, no permission needed for app-specific directories
        } else {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.app.ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 101);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            androidx.core.app.ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraManager != null) {
            cameraManager.stopCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraManager != null) {
            cameraManager.shutdown();
        }
    }
}