package org.drmsoft.audioif.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.drmsoft.audioif.controllers.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static android.R.id.list;
import static android.app.Activity.RESULT_OK;
import static java.lang.Integer.parseInt;

public class FileScanner {


    public  final String STORY_EXTENSIONS = "(?i).+\\.(z[1-8]|zblorb|zlb)$";
    private final MainActivity activity;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public static final String EXTRA_IS_FILESCANNER = "org.drmsoft.audioif.helpers.FILESCANNER";
    public ListView list;
    private Dialog dialog;
    private File currentPath;
    private TextToSpeech tts;

    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    public FileScanner setFileListener(FileSelectedListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }
    private FileSelectedListener fileListener;


    public FileScanner(MainActivity activity, TextToSpeech tts) {
        this.activity = activity;
        this.tts = tts;
        dialog = new Dialog(activity);
        list = new ListView(activity);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                String fileChosen = (String) list.getItemAtPosition(which);
                File chosenFile = getChosenFile(fileChosen);

                    if (fileListener != null) {
                        fileListener.fileSelected(chosenFile);
                    }
                    dialog.dismiss();
            }
        });
        dialog.setContentView(list);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
        ArrayList<String> fileList = scanDir(getChosenFile("/sdcard"),new ArrayList<String>());
        list.setAdapter(new ArrayAdapter(activity,
                android.R.layout.simple_list_item_1, fileList) {
            @Override public View getView(int pos, View view, ViewGroup parent) {
                view = super.getView(pos, view, parent);
                ((TextView) view).setSingleLine(true);
                return view;
            }
        });
    }


    public void showDialog() {
        dialog.show();
        for (int i = 0; i < list.getCount(); i++){
            String[] splitPath = list.getItemAtPosition(i).toString().split("/");
            String fileName = splitPath[splitPath.length - 1].split("\\.")[0];
            tts.speak(String.valueOf(i + 1) + fileName, TextToSpeech.QUEUE_ADD, null);
        }
        startVoiceInput();

    }

    private File getChosenFile(String fileChosen) {

            return new File(currentPath, fileChosen);
    }

    private ArrayList<String> scanDir(File dir, ArrayList<String> list) {
        File[] children = dir.listFiles();
        if (children == null)
            return list;
        for (int count = 0; count < children.length; count++) {
            File child = children[count];
            if (child.isFile() && child.getName().matches(STORY_EXTENSIONS))
                list.add(child.getPath());
            else
                scanDir(child, list);
        }
        return list;
    }

    public void startVoiceInput() {
        Runnable r = new Runnable() {
            public void run() {
                try{
                    while(tts.isSpeaking()){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Which game do you want to play?");
                    activity.isFileScannerInput = true;
                    intent.putExtra(EXTRA_IS_FILESCANNER, true);
                    try {
                        activity.startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                    } catch (ActivityNotFoundException a) {

                    }
                }catch (Exception e){
                    Log.d(e.toString(), e.getMessage());
                }

            }};
        Thread t = new Thread(r);
        t.start();
    }

//        @Override
//        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//            super.onActivityResult(requestCode, resultCode, data);
//
//            switch (requestCode) {
//                case REQ_CODE_SPEECH_INPUT: {
//                    if (resultCode == RESULT_OK && null != data) {
//                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                        String voiceInputText = result.get(0);
//                        try
//                        {
//                            int chosenNumber = Integer.parseInt(voiceInputText);
//                            int indexNumber = chosenNumber - 1;
//                            list.performItemClick(list.getChildAt(indexNumber), indexNumber, list.getItemIdAtPosition(indexNumber));
//                        } catch (NumberFormatException e)
//                        {
//                            tts.speak("Number not recognized, try again.", TextToSpeech.QUEUE_FLUSH, null);
//                            startVoiceInput();
//                        }
//                    }
//                    break;
//                }
//
//            }
//        }

}
