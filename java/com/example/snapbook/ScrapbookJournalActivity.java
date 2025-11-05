package com.example.snapbook;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ScrapbookJournalActivity extends AppCompatActivity {

    private static final String TAG = "ScrapbookJournalActivity";
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int VINTAGE_PHOTOS_REQUEST = 1002;

    private RecyclerView scrapbookRecyclerView;
    private ScrapbookAdapter scrapbookAdapter;
    private List<ScrapbookPage> scrapbookPages;
    private ScrapbookDataManager dataManager;

    private Button addPageBtn;
    private Button importFromGalleryBtn;
    private Button importFromVintageBtn;
    private Button homeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrapbook_journal);

        initializeViews();
        initializeData();
        setupClickListeners();
        loadScrapbookPages();
    }

    private void initializeViews() {
        scrapbookRecyclerView = findViewById(R.id.scrapbook_recycler_view);
        addPageBtn = findViewById(R.id.add_page_btn);
        importFromGalleryBtn = findViewById(R.id.import_gallery_btn);
        importFromVintageBtn = findViewById(R.id.import_vintage_btn);
        homeBtn = findViewById(R.id.home_btn);

        // Setup RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        scrapbookRecyclerView.setLayoutManager(layoutManager);

        scrapbookPages = new ArrayList<>();
        scrapbookAdapter = new ScrapbookAdapter(scrapbookPages, this);
        scrapbookRecyclerView.setAdapter(scrapbookAdapter);
    }

    private void initializeData() {
        dataManager = new ScrapbookDataManager(this);
    }

    private void setupClickListeners() {
        homeBtn.setOnClickListener(v -> finish());

        addPageBtn.setOnClickListener(v -> createNewPage());

        importFromGalleryBtn.setOnClickListener(v -> importFromGallery());

        importFromVintageBtn.setOnClickListener(v -> importFromVintagePhotos());
    }

    private void loadScrapbookPages() {
        scrapbookPages.clear();
        scrapbookPages.addAll(dataManager.loadAllPages());
        scrapbookAdapter.notifyDataSetChanged();

        // Show empty state if no pages
        TextView emptyText = findViewById(R.id.empty_scrapbook_text);
        if (scrapbookPages.isEmpty()) {
            emptyText.setVisibility(TextView.VISIBLE);
            scrapbookRecyclerView.setVisibility(RecyclerView.GONE);
        } else {
            emptyText.setVisibility(TextView.GONE);
            scrapbookRecyclerView.setVisibility(RecyclerView.VISIBLE);
        }
    }

    private void createNewPage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Scrapbook Page");
        builder.setMessage("Choose how to start your new page:");

        builder.setPositiveButton("Empty Page", (dialog, which) -> {
            ScrapbookPage newPage = new ScrapbookPage();
            newPage.title = "New Page " + (scrapbookPages.size() + 1);
            newPage.createdDate = System.currentTimeMillis();
            
            long pageId = dataManager.savePage(newPage);
            newPage.id = pageId;
            
            openPageEditor(newPage);
        });

        builder.setNegativeButton("Add Photo First", (dialog, which) -> {
            importFromGallery();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void importFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void importFromVintagePhotos() {
        // Check for vintage photos in app directory
        File picturesDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        if (picturesDir != null && picturesDir.exists()) {
            File[] imageFiles = picturesDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
            
            if (imageFiles != null && imageFiles.length > 0) {
                showVintagePhotosDialog(imageFiles);
            } else {
                Toast.makeText(this, "No vintage photos found. Take some photos first!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No vintage photos directory found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showVintagePhotosDialog(File[] imageFiles) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Vintage Photo");

        String[] fileNames = new String[imageFiles.length];
        for (int i = 0; i < imageFiles.length; i++) {
            fileNames[i] = imageFiles[i].getName();
        }

        builder.setItems(fileNames, (dialog, which) -> {
            File selectedFile = imageFiles[which];
            createPageWithImage(selectedFile.getAbsolutePath(), true);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        // Copy image to app directory
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        String fileName = "imported_" + System.currentTimeMillis() + ".jpg";
                        File appDir = new File(getFilesDir(), "scrapbook_images");
                        if (!appDir.exists()) appDir.mkdirs();
                        
                        File imageFile = new File(appDir, fileName);
                        FileOutputStream outputStream = new FileOutputStream(imageFile);
                        
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        
                        inputStream.close();
                        outputStream.close();
                        
                        createPageWithImage(imageFile.getAbsolutePath(), false);
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error importing image", e);
                        Toast.makeText(this, "Error importing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void createPageWithImage(String imagePath, boolean isVintagePhoto) {
        ScrapbookPage newPage = new ScrapbookPage();
        newPage.title = isVintagePhoto ? "Vintage Memory" : "New Memory";
        newPage.createdDate = System.currentTimeMillis();
        
        // Add the image as first item
        ScrapbookItem imageItem = new ScrapbookItem();
        imageItem.type = ScrapbookItem.TYPE_IMAGE;
        imageItem.imagePath = imagePath;
        imageItem.x = 50;
        imageItem.y = 100;
        imageItem.width = 300;
        imageItem.height = 300;
        
        newPage.items.add(imageItem);
        
        long pageId = dataManager.savePage(newPage);
        newPage.id = pageId;
        
        openPageEditor(newPage);
    }

    private void openPageEditor(ScrapbookPage page) {
        Intent intent = new Intent(this, ScrapbookPageEditorActivity.class);
        intent.putExtra("page_id", page.id);
        startActivity(intent);
    }

    public void editPage(ScrapbookPage page) {
        openPageEditor(page);
    }

    public void deletePage(ScrapbookPage page) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Page");
        builder.setMessage("Are you sure you want to delete \"" + page.title + "\"?");
        
        builder.setPositiveButton("Delete", (dialog, which) -> {
            dataManager.deletePage(page.id);
            loadScrapbookPages();
            Toast.makeText(this, "Page deleted", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScrapbookPages(); // Refresh when returning from editor
    }
}