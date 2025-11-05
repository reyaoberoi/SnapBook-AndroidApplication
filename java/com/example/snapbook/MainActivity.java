package com.example.snapbook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Vintage Photo Booth button
        findViewById(R.id.vintage_photobooth_btn).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VintagePhotoBoothActivity.class);
            startActivity(intent);
        });

        // Scrapbook Journal button
        findViewById(R.id.scrapbook_journal_btn).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScrapbookJournalActivity.class);
            startActivity(intent);
        });
    }


}