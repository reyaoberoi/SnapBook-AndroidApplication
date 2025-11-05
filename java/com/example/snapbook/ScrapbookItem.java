package com.example.snapbook;

public class ScrapbookItem {
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_TEXT = 2;
    public static final int TYPE_DOODLE = 3;

    public long id;
    public int type;
    public float x, y; // Position
    public float width, height; // Size
    public float rotation = 0f; // Rotation angle
    public float scale = 1f; // Scale factor

    // For images
    public String imagePath;

    // For text
    public String text;
    public int textColor = 0xFF6B4423; // Default vintage brown
    public float textSize = 16f;
    public String fontFamily = "serif";
    public boolean isBold = false;
    public boolean isItalic = false;

    // For doodles
    public String doodlePath; // Path to saved doodle image
    public int strokeColor = 0xFF8B6914; // Default vintage gold
    public float strokeWidth = 3f;

    // General styling
    public int backgroundColor = 0x00000000; // Transparent by default
    public boolean hasBorder = false;
    public int borderColor = 0xFF8B6914;
    public float borderWidth = 2f;
    public float cornerRadius = 0f;

    public ScrapbookItem() {
        // Default constructor
    }

    public ScrapbookItem(int type) {
        this.type = type;
        setDefaultsForType(type);
    }

    private void setDefaultsForType(int type) {
        switch (type) {
            case TYPE_IMAGE:
                this.width = 200f;
                this.height = 200f;
                this.cornerRadius = 8f;
                break;
            case TYPE_TEXT:
                this.width = 150f;
                this.height = 50f;
                this.text = "Add your text here...";
                this.textSize = 16f;
                break;
            case TYPE_DOODLE:
                this.width = 100f;
                this.height = 100f;
                this.strokeWidth = 3f;
                break;
        }
    }

    public boolean isImage() {
        return type == TYPE_IMAGE;
    }

    public boolean isText() {
        return type == TYPE_TEXT;
    }

    public boolean isDoodle() {
        return type == TYPE_DOODLE;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public boolean contains(float touchX, float touchY) {
        return touchX >= x && touchX <= x + width && 
               touchY >= y && touchY <= y + height;
    }

    public void move(float deltaX, float deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }

    public void resize(float newWidth, float newHeight) {
        this.width = Math.max(20f, newWidth); // Minimum size
        this.height = Math.max(20f, newHeight);
    }

    public ScrapbookItem copy() {
        ScrapbookItem copy = new ScrapbookItem();
        copy.type = this.type;
        copy.x = this.x + 20; // Offset copy slightly
        copy.y = this.y + 20;
        copy.width = this.width;
        copy.height = this.height;
        copy.rotation = this.rotation;
        copy.scale = this.scale;
        
        copy.imagePath = this.imagePath;
        copy.text = this.text;
        copy.textColor = this.textColor;
        copy.textSize = this.textSize;
        copy.fontFamily = this.fontFamily;
        copy.isBold = this.isBold;
        copy.isItalic = this.isItalic;
        
        copy.doodlePath = this.doodlePath;
        copy.strokeColor = this.strokeColor;
        copy.strokeWidth = this.strokeWidth;
        
        copy.backgroundColor = this.backgroundColor;
        copy.hasBorder = this.hasBorder;
        copy.borderColor = this.borderColor;
        copy.borderWidth = this.borderWidth;
        copy.cornerRadius = this.cornerRadius;
        
        return copy;
    }
}