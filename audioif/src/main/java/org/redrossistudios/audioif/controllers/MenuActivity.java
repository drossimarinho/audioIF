package org.redrossistudios.audioif.controllers;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.redrossistudios.audioif.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        3);


        }

        setContentView(R.layout.activity_menu);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        Button audioButton = (Button)findViewById(R.id.audioButton);
        Button textButton = (Button)findViewById(R.id.textButton);
        Button linkButton = (Button)findViewById(R.id.linkButton);
        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadNewActivity(MainActivity.class);
            }
        });
        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadNewActivity(TextModeActivity.class);
            }
        });
        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ifarchive.org/indexes/if-archiveXgamesXzcode.html"));
                startActivity(browserIntent);
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 3:
                //External storage2
                if(grantResults.length == 2 && grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    //resume tasks needing this permission
                }else{
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
                break;
        }
    }

    private void loadNewActivity(Class<?> cls){
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

}
