package com.example.snapbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private List<GalleryActivity.GalleryItem> galleryItems;
    private GalleryActivity context;

    public GalleryAdapter(List<GalleryActivity.GalleryItem> galleryItems, GalleryActivity context) {
        this.galleryItems = galleryItems;
        this.context = context;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_item, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        GalleryActivity.GalleryItem item = galleryItems.get(position);

        // Load image asynchronously
        loadImageAsync(item.downloadUrl, holder.photoImageView, holder.loadingBar);

        // Delete button
        holder.deleteButton.setOnClickListener(v ->
                context.deletePhoto(item.photoId, position)
        );

        // Download button
        holder.downloadButton.setOnClickListener(v ->
                context.downloadPhoto(item.downloadUrl, item.photoId)
        );

        // Click to view full image
        holder.photoImageView.setOnClickListener(v -> {
            showFullImage(item.downloadUrl);
        });
    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    private void loadImageAsync(String imagePath, ImageView imageView, ProgressBar progressBar) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        progressBar.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        
        executor.execute(() -> {
            Bitmap bitmap = null;
            try {
                // Load from local file
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    // Create a scaled down version for memory efficiency
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2; // Scale down by factor of 2
                    bitmap = BitmapFactory.decodeFile(imagePath, options);
                }
            } catch (Exception e) {
                bitmap = null;
            }
            
            final Bitmap finalBitmap = bitmap;
            
            // Update UI on main thread
            if (context != null) {
                context.runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    if (finalBitmap != null) {
                        imageView.setImageBitmap(finalBitmap);
                    } else {
                        imageView.setImageResource(R.drawable.placeholder_image);
                    }
                });
            }
        });
    }

    private void showFullImage(String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(20, 20, 20, 20);

        // Load image directly for full view
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.placeholder_image);
        }

        builder.setView(imageView)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        Button deleteButton;
        Button downloadButton;
        ProgressBar loadingBar;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photo_image);
            deleteButton = itemView.findViewById(R.id.delete_button);
            downloadButton = itemView.findViewById(R.id.download_button);
            loadingBar = itemView.findViewById(R.id.photo_loading_bar);
        }
    }
}