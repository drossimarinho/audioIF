package org.redrossistudios.audioif.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Alert {

    private Activity activity;

    public Alert(Activity activity){
        this.activity = activity;
    }
    public void show(String title, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
