package com.example.snapbook;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoStripCreator {

    private static final String TAG = "PhotoStripCreator";
    private static final int BORDER_WIDTH = 50;
    private static final int SPACING = 15;
    private static final int HEADER_HEIGHT = 80;
    private static final int FOOTER_HEIGHT = 60;

    public static Bitmap createPhotoStrip(List<Bitmap> photos) {
        if (photos == null || photos.isEmpty()) {
            Log.e(TAG, "No photos provided");
            return null;
        }

        try {
            int photoWidth = 350;
            int photoHeight = 280;
            int stripWidth = 400;
            int stripHeight = HEADER_HEIGHT + (photoHeight * photos.size()) + (SPACING * (photos.size() + 1)) + FOOTER_HEIGHT + (BORDER_WIDTH * 2);

            Log.d(TAG, "Creating photo strip: " + stripWidth + "x" + stripHeight);

            Bitmap stripBitmap = Bitmap.createBitmap(stripWidth, stripHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(stripBitmap);

            // Draw background
            canvas.drawColor(Color.parseColor("#faf8f5"));

            // Draw border
            drawVintageBorder(canvas, stripWidth, stripHeight);

            // Draw header
            drawHeader(canvas, stripWidth, BORDER_WIDTH);

            // Draw photos
            int yOffset = HEADER_HEIGHT + BORDER_WIDTH;
            for (int i = 0; i < photos.size(); i++) {
                Bitmap photo = photos.get(i);
                if (photo != null) {
                    int xOffset = (stripWidth - photoWidth) / 2;
                    canvas.drawBitmap(photo, xOffset, yOffset, null);

                    // Draw photo frame
                    Paint framePaint = new Paint();
                    framePaint.setColor(Color.parseColor("#3e2723"));
                    framePaint.setStrokeWidth(2);
                    framePaint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(xOffset, yOffset, xOffset + photoWidth, yOffset + photoHeight, framePaint);

                    yOffset += photoHeight + SPACING;
                }
            }

            // Draw footer
            drawFooter(canvas, stripWidth, stripHeight);

            Log.d(TAG, "Photo strip created successfully");
            return stripBitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error creating photo strip", e);
            return null;
        }
    }

    private static void drawVintageBorder(Canvas canvas, int width, int height) {
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#8b6914"));
        borderPaint.setStrokeWidth(8);
        borderPaint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(10, 10, width - 10, height - 10, borderPaint);

        Paint innerBorderPaint = new Paint();
        innerBorderPaint.setColor(Color.parseColor("#c4a747"));
        innerBorderPaint.setStrokeWidth(3);
        innerBorderPaint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(15, 15, width - 15, height - 15, innerBorderPaint);
    }

    private static void drawHeader(Canvas canvas, int stripWidth, int yOffset) {
        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.parseColor("#6b4423"));
        headerPaint.setTextSize(32);
        headerPaint.setTypeface(Typeface.create("serif", Typeface.BOLD));
        headerPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText("VINTAGE MEMORIES", stripWidth / 2, yOffset + 50, headerPaint);
    }

    private static void drawFooter(Canvas canvas, int stripWidth, int stripHeight) {
        Paint footerPaint = new Paint();
        footerPaint.setColor(Color.parseColor("#8b6914"));
        footerPaint.setTextSize(14);
        footerPaint.setTypeface(Typeface.create("serif", Typeface.ITALIC));
        footerPaint.setTextAlign(Paint.Align.CENTER);

        String dateString = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        canvas.drawText(dateString, stripWidth / 2, stripHeight - 35, footerPaint);
    }
}