package com.example.snapbook;

import java.util.List;

// Stub version of CollaborationManager without Firebase dependencies
public class CollaborationManager {
    
    public CollaborationManager() {
        // Stub constructor
    }
    
    public void sharePhoto(String photoId, String photoUrl, List<String> userIds) {
        // Stub implementation - do nothing
    }
    
    public void getSharedPhotos(SharedPhotosCallback callback) {
        // Stub implementation - return empty list
        if (callback != null) {
            callback.onPhotosLoaded(new java.util.ArrayList<>());
        }
    }
    
    public interface SharedPhotosCallback {
        void onPhotosLoaded(List<Object> photos);
        void onError(String error);
    }
}