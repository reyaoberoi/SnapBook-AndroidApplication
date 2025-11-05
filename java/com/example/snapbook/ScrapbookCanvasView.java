package com.example.snapbook;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;

public class ScrapbookCanvasView extends View {

    private static final String TAG = "ScrapbookCanvasView";
    
    private ScrapbookPage page;
    private Paint backgroundPaint;
    private Paint itemPaint;
    private Paint textPaint;
    private Paint borderPaint;
    private Paint selectionPaint;
    
    private ScrapbookItem selectedItem;
    private boolean isDragging = false;
    private float lastTouchX, lastTouchY;
    
    public ScrapbookCanvasView(Context context) {
        super(context);
        init();
    }

    public ScrapbookCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrapbookCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFFAF8F5); // Cream background
        
        itemPaint = new Paint();
        itemPaint.setAntiAlias(true);
        
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(48f);
        textPaint.setColor(0xFF6B4423);
        textPaint.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        
        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(0xFF8B6914);
        
        selectionPaint = new Paint();
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(4f);
        selectionPaint.setColor(0xFF4CAF50);
        selectionPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
    }

    public void setPage(ScrapbookPage page) {
        this.page = page;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (page == null) return;

        // Draw background
        canvas.drawColor(page.backgroundColor);

        // Draw background image if exists
        if (page.backgroundImagePath != null) {
            drawBackgroundImage(canvas, page.backgroundImagePath);
        }

        // Draw all items
        for (ScrapbookItem item : page.items) {
            drawItem(canvas, item);
        }

        // Draw selection border for selected item
        if (selectedItem != null) {
            drawSelectionBorder(canvas, selectedItem);
        }
    }

    private void drawBackgroundImage(Canvas canvas, String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    Rect destRect = new Rect(0, 0, getWidth(), getHeight());
                    canvas.drawBitmap(bitmap, null, destRect, itemPaint);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error drawing background image", e);
        }
    }

    private void drawItem(Canvas canvas, ScrapbookItem item) {
        canvas.save();
        
        // Apply transformations
        canvas.translate(item.x + item.width/2, item.y + item.height/2);
        canvas.rotate(item.rotation);
        canvas.scale(item.scale, item.scale);
        canvas.translate(-item.width/2, -item.height/2);

        RectF itemRect = new RectF(0, 0, item.width, item.height);

        // Draw background if set
        if (item.backgroundColor != 0x00000000) {
            itemPaint.setColor(item.backgroundColor);
            canvas.drawRoundRect(itemRect, item.cornerRadius, item.cornerRadius, itemPaint);
        }

        // Draw item content based on type
        switch (item.type) {
            case ScrapbookItem.TYPE_IMAGE:
                drawImageItem(canvas, item, itemRect);
                break;
            case ScrapbookItem.TYPE_TEXT:
                drawTextItem(canvas, item, itemRect);
                break;
            case ScrapbookItem.TYPE_DOODLE:
                drawDoodleItem(canvas, item, itemRect);
                break;
        }

        // Draw border if enabled
        if (item.hasBorder) {
            borderPaint.setColor(item.borderColor);
            borderPaint.setStrokeWidth(item.borderWidth);
            canvas.drawRoundRect(itemRect, item.cornerRadius, item.cornerRadius, borderPaint);
        }

        canvas.restore();
    }

    private void drawImageItem(Canvas canvas, ScrapbookItem item, RectF rect) {
        if (item.imagePath != null) {
            try {
                File imageFile = new File(item.imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(item.imagePath);
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, rect, itemPaint);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error drawing image item", e);
                // Draw placeholder
                drawImagePlaceholder(canvas, rect);
            }
        } else {
            drawImagePlaceholder(canvas, rect);
        }
    }

    private void drawImagePlaceholder(Canvas canvas, RectF rect) {
        itemPaint.setColor(0xFFE0E0E0);
        canvas.drawRoundRect(rect, 8f, 8f, itemPaint);
        
        textPaint.setTextSize(24f);
        textPaint.setColor(0xFF999999);
        String placeholder = "📷";
        float textWidth = textPaint.measureText(placeholder);
        canvas.drawText(placeholder, 
                       rect.centerX() - textWidth/2, 
                       rect.centerY() + textPaint.getTextSize()/2, 
                       textPaint);
    }

    private void drawTextItem(Canvas canvas, ScrapbookItem item, RectF rect) {
        if (item.text != null && !item.text.trim().isEmpty()) {
            textPaint.setColor(item.textColor);
            textPaint.setTextSize(item.textSize);
            
            // Set font style
            int style = Typeface.NORMAL;
            if (item.isBold && item.isItalic) {
                style = Typeface.BOLD_ITALIC;
            } else if (item.isBold) {
                style = Typeface.BOLD;
            } else if (item.isItalic) {
                style = Typeface.ITALIC;
            }
            textPaint.setTypeface(Typeface.create(item.fontFamily, style));

            // Simple text drawing (could be enhanced for multi-line)
            String[] lines = item.text.split("\n");
            float lineHeight = textPaint.getTextSize() * 1.2f;
            float startY = rect.top + textPaint.getTextSize();
            
            for (int i = 0; i < lines.length && startY < rect.bottom; i++) {
                canvas.drawText(lines[i], rect.left + 8, startY, textPaint);
                startY += lineHeight;
            }
        }
    }

    private void drawDoodleItem(Canvas canvas, ScrapbookItem item, RectF rect) {
        if (item.doodlePath != null) {
            // TODO: Load and draw doodle bitmap
            try {
                File doodleFile = new File(item.doodlePath);
                if (doodleFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(item.doodlePath);
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, rect, itemPaint);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error drawing doodle", e);
            }
        }
        
        // Draw placeholder doodle
        Paint doodlePaint = new Paint();
        doodlePaint.setColor(item.strokeColor);
        doodlePaint.setStrokeWidth(item.strokeWidth);
        doodlePaint.setStyle(Paint.Style.STROKE);
        doodlePaint.setAntiAlias(true);
        
        // Simple doodle placeholder - a wavy line
        Path path = new Path();
        path.moveTo(rect.left + 10, rect.centerY());
        path.quadTo(rect.centerX(), rect.top + 10, rect.right - 10, rect.centerY());
        path.quadTo(rect.centerX(), rect.bottom - 10, rect.left + 10, rect.centerY());
        canvas.drawPath(path, doodlePaint);
    }

    private void drawSelectionBorder(Canvas canvas, ScrapbookItem item) {
        RectF selectionRect = new RectF(item.x - 5, item.y - 5, 
                                       item.x + item.width + 5, item.y + item.height + 5);
        canvas.drawRoundRect(selectionRect, item.cornerRadius + 5, item.cornerRadius + 5, selectionPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (page == null) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectedItem = findItemAt(x, y);
                if (selectedItem != null) {
                    isDragging = true;
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging && selectedItem != null) {
                    float deltaX = x - lastTouchX;
                    float deltaY = y - lastTouchY;
                    
                    selectedItem.move(deltaX, deltaY);
                    
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    private ScrapbookItem findItemAt(float x, float y) {
        // Search in reverse order to get topmost item
        for (int i = page.items.size() - 1; i >= 0; i--) {
            ScrapbookItem item = page.items.get(i);
            if (item.contains(x, y)) {
                return item;
            }
        }
        return null;
    }

    public ScrapbookItem getSelectedItem() {
        return selectedItem;
    }

    public void clearSelection() {
        selectedItem = null;
        invalidate();
    }

    public void deleteSelectedItem() {
        if (selectedItem != null && page != null) {
            page.items.remove(selectedItem);
            selectedItem = null;
            invalidate();
        }
    }
}