package com.example.snapbook;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

public class VoiceManager implements TextToSpeech.OnInitListener {

    private static final String TAG = "VoiceManager";
    private TextToSpeech textToSpeech;
    private boolean isReady = false;
    private Context context;

    public VoiceManager(Context context) {
        this.context = context;
        textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            isReady = true;
            Log.d(TAG, "Text-to-Speech ready");
        } else {
            Log.e(TAG, "Text-to-Speech initialization failed");
        }
    }

    public void speak(String text) {
        if (isReady && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void playCountdown(int seconds) {
        speak(seconds + "...");
    }

    public void playPhotoCaptureSounds() {
        speak("Get ready, darling! Three... Two... One...");
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}