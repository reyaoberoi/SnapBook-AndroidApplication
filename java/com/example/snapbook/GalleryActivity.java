// GalleryActivity.java - Photo Gallery Screen
package com.example.snapbook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView galleryRecyclerView;
    private GalleryAdapter galleryAdapter;
    private List<GalleryItem> galleryItems;

    private ProgressBar loadingBar;
    private TextView emptyGalleryText;
    private Button backButton;
    private Button clearGalleryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Initialize UI
        galleryRecyclerView = findViewById(R.id.gallery_recycler_view);
        loadingBar = findViewById(R.id.loading_bar);
        emptyGalleryText = findViewById(R.id.empty_gallery_text);
        backButton = findViewById(R.id.back_button);
        clearGalleryButton = findViewById(R.id.clear_gallery_button);

        // Initialize collections
        galleryItems = new ArrayList<>();
        galleryAdapter = new GalleryAdapter(galleryItems, this);

        // Setup RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        galleryRecyclerView.setLayoutManager(layoutManager);
        galleryRecyclerView.setAdapter(galleryAdapter);

        // Button listeners
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GalleryActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        clearGalleryButton.setOnClickListener(v -> clearGallery());

        // Load gallery
        loadGallery();
    }

    private void loadGallery() {
        loadingBar.setVisibility(ProgressBar.VISIBLE);
        galleryItems.clear();

        // Load photos from local storage
        File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir != null && picturesDir.exists()) {
            File[] photoFiles = picturesDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
            
            if (photoFiles != null && photoFiles.length > 0) {
                for (File photoFile : photoFiles) {
                    String photoId = photoFile.getName().replace(".jpg", "").replace(".jpeg", "");
                    String filePath = photoFile.getAbsolutePath();
                    long timestamp = photoFile.lastModified();
                    
                    GalleryItem item = new GalleryItem(photoId, filePath, timestamp);
                    galleryItems.add(item);
                }
                
                // Sort by timestamp (newest first)
                Collections.sort(galleryItems, (a, b) -> Long.compare(b.timestamp, a.timestamp));
                
                galleryAdapter.notifyDataSetChanged();
                emptyGalleryText.setVisibility(TextView.GONE);
                galleryRecyclerView.setVisibility(RecyclerView.VISIBLE);
            } else {
                emptyGalleryText.setText("No photos yet. Start taking some!");
                emptyGalleryText.setVisibility(TextView.VISIBLE);
                galleryRecyclerView.setVisibility(RecyclerView.GONE);
            }
        } else {
            emptyGalleryText.setText("No photos yet. Start taking some!");
            emptyGalleryText.setVisibility(TextView.VISIBLE);
            galleryRecyclerView.setVisibility(RecyclerView.GONE);
        }
        
        loadingBar.setVisibility(ProgressBar.GONE);
    }

    private void clearGallery() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear Gallery")
                .setMessage("Are you sure you want to delete all photos? This cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (picturesDir != null && picturesDir.exists()) {
                        File[] photoFiles = picturesDir.listFiles((dir, name) -> 
                            name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
                        
                        if (photoFiles != null) {
                            int deletedCount = 0;
                            for (File photoFile : photoFiles) {
                                if (photoFile.delete()) {
                                    deletedCount++;
                                }
                            }
                            Toast.makeText(GalleryActivity.this,
                                    deletedCount + " photos deleted!",
                                    Toast.LENGTH_SHORT).show();
                            loadGallery();
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void deletePhoto(String photoId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Photo")
                .setMessage("Delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (position >= 0 && position < galleryItems.size()) {
                        GalleryItem item = galleryItems.get(position);
                        File photoFile = new File(item.downloadUrl); // downloadUrl is actually file path in local version
                        
                        if (photoFile.exists() && photoFile.delete()) {
                            galleryItems.remove(position);
                            galleryAdapter.notifyItemRemoved(position);
                            Toast.makeText(GalleryActivity.this,
                                    "Photo deleted",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GalleryActivity.this,
                                    "Error deleting photo",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void downloadPhoto(String downloadUrl, String photoId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // For local files, just copy to a different location
                File sourceFile = new File(downloadUrl);
                if (sourceFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(downloadUrl);
                    
                    // Save to device downloads
                    File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File photoFile = new File(picturesDir, "downloaded_" + photoId + ".jpg");
                    FileOutputStream fos = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                    fos.close();

                    runOnUiThread(() ->
                            Toast.makeText(GalleryActivity.this,
                                    "Photo saved to " + photoFile.getAbsolutePath(),
                                    Toast.LENGTH_LONG).show()
                    );
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(GalleryActivity.this,
                                    "Photo file not found",
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(GalleryActivity.this,
                                "Download error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // Gallery Item Model
    public static class GalleryItem {
        public String photoId;
        public String downloadUrl;
        public long timestamp;

        public GalleryItem(String photoId, String downloadUrl, long timestamp) {
            this.photoId = photoId;
            this.downloadUrl = downloadUrl;
            this.timestamp = timestamp;
        }
    }
}