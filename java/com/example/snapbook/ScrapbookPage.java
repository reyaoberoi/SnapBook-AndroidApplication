package com.example.snapbook;

import java.util.ArrayList;
import java.util.List;

public class ScrapbookPage {
    public long id;
    public String title;
    public long createdDate;
    public long lastModified;
    public List<ScrapbookItem> items;
    public String backgroundImagePath;
    public int backgroundColor = 0xFFFAF8F5; // Default cream color

    public ScrapbookPage() {
        this.items = new ArrayList<>();
        this.createdDate = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
    }

    public ScrapbookPage(long id, String title, long createdDate, long lastModified) {
        this.id = id;
        this.title = title;
        this.createdDate = createdDate;
        this.lastModified = lastModified;
        this.items = new ArrayList<>();
    }

    public void updateModified() {
        this.lastModified = System.currentTimeMillis();
    }

    public int getItemCount() {
        return items.size();
    }

    public int getImageCount() {
        int count = 0;
        for (ScrapbookItem item : items) {
            if (item.type == ScrapbookItem.TYPE_IMAGE) {
                count++;
            }
        }
        return count;
    }

    public int getTextCount() {
        int count = 0;
        for (ScrapbookItem item : items) {
            if (item.type == ScrapbookItem.TYPE_TEXT) {
                count++;
            }
        }
        return count;
    }

    public String getPreviewText() {
        for (ScrapbookItem item : items) {
            if (item.type == ScrapbookItem.TYPE_TEXT && item.text != null && !item.text.trim().isEmpty()) {
                String preview = item.text.trim();
                return preview.length() > 50 ? preview.substring(0, 50) + "..." : preview;
            }
        }
        return "No text added yet";
    }

    public String getFirstImagePath() {
        for (ScrapbookItem item : items) {
            if (item.type == ScrapbookItem.TYPE_IMAGE && item.imagePath != null) {
                return item.imagePath;
            }
        }
        return null;
    }
}