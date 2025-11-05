package com.example.snapbook;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ScrapbookPageEditorActivity extends AppCompatActivity {

    private static final String TAG = "ScrapbookPageEditor";
    private static final int PICK_IMAGE_REQUEST = 2001;

    private ScrapbookPage currentPage;
    private ScrapbookDataManager dataManager;
    private ScrapbookCanvasView canvasView;
    
    private EditText titleInput;
    private Button saveBtn;
    private Button addImageBtn;
    private Button addTextBtn;
    private Button addDoodleBtn;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrapbook_page_editor);

        initializeViews();
        initializeData();
        setupClickListeners();
        loadPage();
    }

    private void initializeViews() {
        canvasView = findViewById(R.id.canvas_view);
        titleInput = findViewById(R.id.title_input);
        saveBtn = findViewById(R.id.save_btn);
        addImageBtn = findViewById(R.id.add_image_btn);
        addTextBtn = findViewById(R.id.add_text_btn);
        addDoodleBtn = findViewById(R.id.add_doodle_btn);
        backBtn = findViewById(R.id.back_btn);
    }

    private void initializeData() {
        dataManager = new ScrapbookDataManager(this);
    }

    private void setupClickListeners() {
        backBtn.setOnClickListener(v -> {
            savePage();
            finish();
        });

        saveBtn.setOnClickListener(v -> {
            savePage();
            Toast.makeText(this, "Page saved!", Toast.LENGTH_SHORT).show();
        });

        addImageBtn.setOnClickListener(v -> addImage());
        addTextBtn.setOnClickListener(v -> addText());
        addDoodleBtn.setOnClickListener(v -> addDoodle());
    }

    private void loadPage() {
        long pageId = getIntent().getLongExtra("page_id", -1);
        if (pageId != -1) {
            currentPage = dataManager.loadPage(pageId);
            if (currentPage != null) {
                titleInput.setText(currentPage.title);
                canvasView.setPage(currentPage);
            }
        }

        if (currentPage == null) {
            // Create new page
            currentPage = new ScrapbookPage();
            currentPage.title = "New Page";
            titleInput.setText(currentPage.title);
            canvasView.setPage(currentPage);
        }
    }

    private void savePage() {
        if (currentPage != null) {
            currentPage.title = titleInput.getText().toString().trim();
            if (currentPage.title.isEmpty()) {
                currentPage.title = "Untitled Page";
            }
            
            currentPage.updateModified();
            long savedId = dataManager.savePage(currentPage);
            if (savedId > 0) {
                currentPage.id = savedId;
                Log.d(TAG, "Page saved with ID: " + savedId);
            }
        }
    }

    private void addImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");
        builder.setMessage("Choose image source:");

        builder.setPositiveButton("Gallery", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("Vintage Photos", (dialog, which) -> {
            // TODO: Show vintage photos picker
            Toast.makeText(this, "Vintage photos picker - coming soon!", Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void addText() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Text");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Enter your text here...");
        input.setLines(3);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                ScrapbookItem textItem = new ScrapbookItem(ScrapbookItem.TYPE_TEXT);
                textItem.text = text;
                textItem.x = 50;
                textItem.y = 200;
                
                currentPage.items.add(textItem);
                canvasView.invalidate();
                
                Toast.makeText(this, "Text added! Tap to edit position.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void addDoodle() {
        // Simple doodle placeholder
        ScrapbookItem doodleItem = new ScrapbookItem(ScrapbookItem.TYPE_DOODLE);
        doodleItem.x = 100;
        doodleItem.y = 300;
        doodleItem.width = 100;
        doodleItem.height = 100;
        
        currentPage.items.add(doodleItem);
        canvasView.invalidate();
        
        Toast.makeText(this, "Doodle area added! (Drawing feature coming soon)", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        // Copy image to app directory and add to page
                        java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        String fileName = "scrapbook_" + System.currentTimeMillis() + ".jpg";
                        java.io.File appDir = new java.io.File(getFilesDir(), "scrapbook_images");
                        if (!appDir.exists()) appDir.mkdirs();
                        
                        java.io.File imageFile = new java.io.File(appDir, fileName);
                        java.io.FileOutputStream outputStream = new java.io.FileOutputStream(imageFile);
                        
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        
                        inputStream.close();
                        outputStream.close();
                        
                        // Add image item to page
                        ScrapbookItem imageItem = new ScrapbookItem(ScrapbookItem.TYPE_IMAGE);
                        imageItem.imagePath = imageFile.getAbsolutePath();
                        imageItem.x = 50;
                        imageItem.y = 100;
                        
                        currentPage.items.add(imageItem);
                        canvasView.invalidate();
                        
                        Toast.makeText(this, "Image added! Tap to move or resize.", Toast.LENGTH_SHORT).show();
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error adding image", e);
                        Toast.makeText(this, "Error adding image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePage(); // Auto-save when leaving
    }
}