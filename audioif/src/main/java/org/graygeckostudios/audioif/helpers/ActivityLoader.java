package org.graygeckostudios.audioif.helpers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

public class ActivityLoader {
    public static void load(AppCompatActivity origin ,Class<?> activityToLoad){
        Intent intent = new Intent(origin, activityToLoad);
        origin.startActivity(intent);
    }
}
