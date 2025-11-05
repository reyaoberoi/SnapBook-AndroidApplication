package com.example.snapbook;

import android.graphics.Bitmap;
import android.util.Log;

public class FilterManager {

    private static final String TAG = "FilterManager";

    public enum FilterType {
        SEPIA("Classic Sepia"),
        POLAROID("1970s Polaroid"),
        KODACHROME("1950s Kodachrome"),
        VINTAGE("Vintage Fade"),
        BLACK_AND_WHITE("Black & White"),
        CYANOTYPE("Cyanotype"),
        NONE("None");

        private final String displayName;

        FilterType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static FilterType fromString(String filterName) {
            switch (filterName.toLowerCase()) {
                case "sepia": return SEPIA;
                case "polaroid": return POLAROID;
                case "kodachrome": return KODACHROME;
                case "vintage": return VINTAGE;
                case "bw":
                case "black_and_white": return BLACK_AND_WHITE;
                case "cyanotype": return CYANOTYPE;
                default: return NONE;
            }
        }
    }

    private FilterType currentFilter = FilterType.SEPIA;

    public FilterType getCurrentFilter() {
        return currentFilter;
    }

    public void setCurrentFilter(FilterType filter) {
        this.currentFilter = filter;
        Log.d(TAG, "Filter: " + filter.getDisplayName());
    }

    public void setCurrentFilter(String filterName) {
        this.currentFilter = FilterType.fromString(filterName);
        Log.d(TAG, "Filter: " + currentFilter.getDisplayName());
    }

    public Bitmap applyCurrentFilter(Bitmap bitmap) {
        return applyFilter(bitmap, currentFilter);
    }

    public Bitmap applyFilter(Bitmap bitmap, FilterType filterType) {
        if (bitmap == null) return null;

        switch (filterType) {
            case SEPIA: return applySepiaFilter(bitmap);
            case POLAROID: return applyPolaroidFilter(bitmap);
            case KODACHROME: return applyKodachromeFilter(bitmap);
            case VINTAGE: return applyVintageFilter(bitmap);
            case BLACK_AND_WHITE: return applyBlackAndWhiteFilter(bitmap);
            case CYANOTYPE: return applyCyanotypeFilter(bitmap);
            case NONE:
            default: return bitmap;
        }
    }

    private Bitmap applySepiaFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            int newR = Math.min(255, (int)(r * 0.393 + g * 0.769 + b * 0.189));
            int newG = Math.min(255, (int)(r * 0.349 + g * 0.686 + b * 0.168));
            int newB = Math.min(255, (int)(r * 0.272 + g * 0.534 + b * 0.131));

            pixels[i] = (0xFF << 24) | (newR << 16) | (newG << 8) | newB;
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private Bitmap applyPolaroidFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            int newR = Math.min(255, (int)(r * 0.9 + 20));
            int newG = Math.min(255, (int)(g * 0.85 + 25));
            int newB = Math.min(255, (int)(b * 0.95 + 15));

            pixels[i] = (0xFF << 24) | (newR << 16) | (newG << 8) | newB;
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private Bitmap applyKodachromeFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            int newR = Math.min(255, (int)(r * 1.2));
            int newG = Math.min(255, (int)(g * 1.1));
            int newB = Math.min(255, (int)(b * 0.9));

            pixels[i] = (0xFF << 24) | (newR << 16) | (newG << 8) | newB;
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private Bitmap applyVintageFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            int newR = Math.min(255, (int)(r * 1.15 + 15));
            int newG = Math.min(255, (int)(g * 0.95 + 10));
            int newB = Math.min(255, (int)(b * 0.75));

            pixels[i] = (0xFF << 24) | (newR << 16) | (newG << 8) | newB;
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private Bitmap applyBlackAndWhiteFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            pixels[i] = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private Bitmap applyCyanotypeFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            int avg = (r + g + b) / 3;
            int newR = Math.min(255, (int)(avg * 0.3));
            int newG = Math.min(255, (int)(avg * 0.6));
            int newB = Math.min(255, (int)(avg * 1.1));

            pixels[i] = (0xFF << 24) | (newR << 16) | (newG << 8) | newB;
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
}