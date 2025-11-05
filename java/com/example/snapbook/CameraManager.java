package com.example.snapbook;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraManager {

    private static final String TAG = "CameraManager";
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private final AppCompatActivity activity;
    private final PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean isFrontCamera = true;
    private CaptureCallback captureCallback;
    private boolean isCameraBound = false;

    public interface CaptureCallback {
        void onCaptureSuccess(Bitmap bitmap);
        void onCaptureError(String errorMessage);
    }

    public CameraManager(AppCompatActivity activity, PreviewView previewView) {
        this.activity = activity;
        this.previewView = previewView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void setCaptureCallback(CaptureCallback callback) {
        this.captureCallback = callback;
    }

    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    public void startCamera() {
        Log.d(TAG, "Starting camera...");

        if (!hasPermission()) {
            Log.w(TAG, "Camera permission not granted, requesting...");
            requestPermission();
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(activity);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCamera();
                Log.d(TAG, "Camera started successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(activity, "Camera error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(activity));
    }

    private void bindCamera() {
        if (cameraProvider == null) {
            Log.e(TAG, "CameraProvider is null");
            return;
        }

        try {
            cameraProvider.unbindAll();

            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(previewView.getDisplay().getRotation())
                    .build();

            CameraSelector cameraSelector = isFrontCamera ?
                    CameraSelector.DEFAULT_FRONT_CAMERA :
                    CameraSelector.DEFAULT_BACK_CAMERA;

            try {
                cameraProvider.bindToLifecycle(
                        activity,
                        cameraSelector,
                        preview,
                        imageCapture
                );
                isCameraBound = true;
                Log.d(TAG, "Camera bound to lifecycle successfully. Front: " + isFrontCamera);
            } catch (Exception e) {
                Log.e(TAG, "Error binding camera to lifecycle", e);
                isCameraBound = false;
                throw e;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error binding camera", e);
            Toast.makeText(activity, "Camera binding failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleCamera() {
        isFrontCamera = !isFrontCamera;
        String cameraType = isFrontCamera ? "Front Camera" : "Back Camera";
        Toast.makeText(activity, "Switched to " + cameraType, Toast.LENGTH_SHORT).show();

        if (cameraProvider != null) {
            bindCamera();
        }
    }

    public void capturePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "ImageCapture is null - camera not initialized");
            if (captureCallback != null) {
                captureCallback.onCaptureError("Camera not ready. Please wait...");
            }
            return;
        }

        if (!isCameraBound) {
            Log.e(TAG, "Camera not bound to lifecycle");
            if (captureCallback != null) {
                captureCallback.onCaptureError("Camera binding error. Please restart.");
            }
            return;
        }

        Log.d(TAG, "Capturing photo...");

        // Use file-based capture for better reliability
        java.io.File photoFile = new java.io.File(
            activity.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
            "temp_photo_" + System.currentTimeMillis() + ".jpg"
        );

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(activity),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults output) {
                        Log.d(TAG, "Photo saved successfully: " + photoFile.getAbsolutePath());

                        try {
                            // Load the saved image as bitmap
                            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                            options.inSampleSize = 2; // Reduce size for memory efficiency
                            Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);

                            if (bitmap == null) {
                                Log.e(TAG, "Failed to decode saved image");
                                if (captureCallback != null) {
                                    captureCallback.onCaptureError("Failed to process captured image");
                                }
                                return;
                            }

                            Log.d(TAG, "Bitmap loaded: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                            // Flip if front camera
                            if (isFrontCamera) {
                                bitmap = flipBitmap(bitmap);
                            }

                            // Clean up temp file
                            photoFile.delete();

                            if (captureCallback != null) {
                                captureCallback.onCaptureSuccess(bitmap);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error processing captured image: " + e.getMessage(), e);
                            if (captureCallback != null) {
                                captureCallback.onCaptureError("Error processing image: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        Log.e(TAG, "Capture failed: " + exception.getMessage(), exception);
                        if (captureCallback != null) {
                            captureCallback.onCaptureError("Capture error: " +
                                    (exception.getMessage() != null ? exception.getMessage() : "Unknown"));
                        }
                    }
                }
        );
    }

    // Simplified and more reliable conversion
    private Bitmap convertImageProxyToBitmap(ImageProxy imageProxy) {
        try {
            Image image = imageProxy.getImage();
            if (image == null) {
                Log.e(TAG, "Image is null");
                return null;
            }

            int width = image.getWidth();
            int height = image.getHeight();
            Log.d(TAG, "Converting image: " + width + "x" + height);

            // Get YUV planes
            Image.Plane[] planes = image.getPlanes();
            if (planes.length < 3) {
                Log.e(TAG, "Expected 3 planes, got: " + planes.length);
                return null;
            }

            Image.Plane yPlane = planes[0];
            Image.Plane uPlane = planes[1];
            Image.Plane vPlane = planes[2];

            ByteBuffer yBuffer = yPlane.getBuffer();
            ByteBuffer uBuffer = uPlane.getBuffer();
            ByteBuffer vBuffer = vPlane.getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            Log.d(TAG, "Plane sizes: Y=" + ySize + ", U=" + uSize + ", V=" + vSize);

            // Create NV21 byte array
            byte[] nv21 = new byte[ySize + uSize + vSize];

            // Copy Y plane
            yBuffer.get(nv21, 0, ySize);

            // For NV21 format, we need to interleave U and V
            byte[] uBytes = new byte[uSize];
            byte[] vBytes = new byte[vSize];
            uBuffer.get(uBytes);
            vBuffer.get(vBytes);

            // Interleave V and U for NV21 format
            int uvIndex = ySize;
            for (int i = 0; i < uSize; i++) {
                nv21[uvIndex++] = vBytes[i];
                nv21[uvIndex++] = uBytes[i];
            }

            Log.d(TAG, "YUV data prepared, total size: " + nv21.length);

            // Convert NV21 to RGB using Android's built-in method
            int[] rgb = new int[width * height];
            decodeYUV420SP(rgb, nv21, width, height);

            // Create bitmap from RGB data
            Bitmap bitmap = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);

            Log.d(TAG, "Bitmap created successfully");
            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error in convertImageProxyToBitmap: " + e.getMessage(), e);
            return null;
        }
    }

    // Improved YUV420SP (NV21) to RGB conversion
    private void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & yuv420sp[yp]) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0; else if (r > 262143) r = 262143;
                if (g < 0) g = 0; else if (g > 262143) g = 262143;
                if (b < 0) b = 0; else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    private Bitmap flipBitmap(Bitmap bitmap) {
        try {
            Matrix matrix = new Matrix();
            matrix.setScale(-1, 1);
            Bitmap flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return flipped;
        } catch (Exception e) {
            Log.e(TAG, "Error flipping bitmap", e);
            return bitmap;
        }
    }

    public void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
            isCameraBound = false;
        }
        imageCapture = null;
    }

    public void shutdown() {
        stopCamera();
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
    }

    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    public boolean isCameraBound() {
        return isCameraBound;
    }
}