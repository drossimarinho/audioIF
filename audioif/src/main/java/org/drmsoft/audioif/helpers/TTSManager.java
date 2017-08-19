package org.drmsoft.audioif.helpers;

import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.Locale;
import android.content.Context;
import android.util.Log;

/**
 * Created by Nilanchala
 * http://www.stacktips.com
 */
public class TTSManager {

    private TextToSpeech mTts = null;
    public boolean isLoaded = false;

    public void init(Context context) {
        try {
            mTts = new TextToSpeech(context, onInitListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = mTts.setLanguage(Locale.UK);
                isLoaded = true;

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("error", "This Language is not supported");
                }
            } else {
                Log.e("error", "Initialization Failed!");
            }
        }
    };

    public void shutDown() {
        mTts.shutdown();
    }

    public void addQueue(String text) {
        if (isLoaded){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTts.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
            } else {
                mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        else{
            Log.e("error", "TTS Not Initialized");
        }
    }

    public void initQueue(String text) {

        if (isLoaded){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTts.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
            } else {
                mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        else {
            Log.e("error", "TTS Not Initialized");
        }
    }
}