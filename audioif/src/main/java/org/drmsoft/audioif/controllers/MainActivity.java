package org.drmsoft.audioif.controllers;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.drmsoft.audioif.R;
import org.drmsoft.audioif.helpers.Alert;
import org.drmsoft.audioif.helpers.FileChooser;
import org.drmsoft.audioif.helpers.FileScanner;
import org.drmsoft.audioif.helpers.NumberReader;
import org.drmsoft.audioif.helpers.StoryFileTypeChecker;
import org.drmsoft.audioif.models.StoryFileType;
import org.zmpp.ExecutionControl;
import org.zmpp.blorb.NativeImage;
import org.zmpp.blorb.NativeImageFactory;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.io.IOSystem;
import org.zmpp.vm.MachineFactory;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.windowing.AnnotatedText;
import org.zmpp.windowing.BufferedScreenModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Locale;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public boolean isFileScannerInput = false;
    private ExecutionControl executionControl;
    private MachineFactory.MachineInitStruct machineInit;
    private BufferedScreenModel screenModel;
    private TextView storyField;
    private EditText commandField;
    private Button button;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextToSpeech tts;
    private InputStream storyIs;
    private FileScanner fileScanner;
    private StoryFileTypeChecker storyFileTypeChecker;
    private org.drmsoft.audioif.helpers.Alert Alert;
    private ProgressDialog progressDialog;

    private void startVoiceInput() {
        Runnable r = new Runnable() {
            public void run() {
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
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What do you want to do next?");
                isFileScannerInput = false;
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {

                }
            }};
        Thread t = new Thread(r);
        t.start();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    if(isFileScannerInput){
                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        String voiceInputText = result.get(0);
                        try
                        {
                            int chosenNumber = Integer.parseInt(NumberReader.replaceNumbers(voiceInputText));
                            if(chosenNumber == 0){
                                tts.speak("Invalid story number, try again.", TextToSpeech.QUEUE_FLUSH, null);
                                fileScanner.startVoiceInput();
                            }
                            else{
                                int indexNumber = chosenNumber - 1;
                                fileScanner.list.performItemClick(fileScanner.list.getChildAt(indexNumber), indexNumber, fileScanner.list.getItemIdAtPosition(indexNumber));
                            }
                        } catch (NumberFormatException e)
                        {
                            tts.speak("Number not recognized, try again.", TextToSpeech.QUEUE_FLUSH, null);
                            fileScanner.startVoiceInput();
                        }
                    }
                    else{
                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        String voiceInputText = result.get(0);
                        if(voiceInputText.contains("North West")){
                            voiceInputText = "NW";
                        }
                        else if(voiceInputText.contains("North East")){
                            voiceInputText = "NE";
                        }
                        else if(voiceInputText.contains("South East")){
                            voiceInputText = "SE";
                        }
                        else if(voiceInputText.contains("South West")){
                            voiceInputText = "SW";
                        }
                        commandField.setText(voiceInputText);
                        handleButtonClick();
                    }
                }
                break;
            }

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Alert = new Alert(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storyField = (TextView) findViewById(R.id.textView);
        commandField = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        progressDialog = ProgressDialog.show(MainActivity.this, "",
                "Loading. Please wait...", true);

        tts = new TextToSpeech(this, this);
        storyFileTypeChecker = new StoryFileTypeChecker();
    }

    public void executeEngine(){
        try {
            executionControl = new ExecutionControl(machineInit);
            executionControl.resizeScreen(100,500);
            screenModel.init(executionControl.getMachine(), executionControl.getZsciiEncoding());
            executionControl.run();
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleButtonClick();
                }
            });
            commandField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startVoiceInput();
                }
            });
            storyField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tts.isSpeaking()){
                        tts.stop();
                    }
                    else{
                        startVoiceInput();
                    }
                }
            });
            String initialText = getBufferText(screenModel);
            if(initialText.length() > 1){
                storyField.setText(initialText);
                tts.speak(storyField.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                startVoiceInput();
            }
            else{
                commandField.setText(" ");
                handleButtonClick();
            }

        } catch (Exception ex) {
            Alert.show("Error", ex.toString() + "\n" + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                //Call speak stuff
                fileScanner = new FileScanner(this, tts);
                progressDialog.dismiss();
                fileScanner.setFileListener(new FileScanner.FileSelectedListener() {
                    @Override
                    public void fileSelected(final File file) {
                        try {
                            String fileExtension = file.getName().split("\\.")[1];
                            StoryFileType storyFileType = storyFileTypeChecker.GetStoryFileType(fileExtension);
                            storyIs = new FileInputStream(file);
                            configureEngine(storyIs, storyFileType);
                            executeEngine();
                        } catch (Exception e){
                            e.printStackTrace();
                            Alert.show("Error", e.toString() + "\n" + e.getMessage());
                        }

                    }
                }).showDialog();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private void configureEngine(InputStream storyIs, StoryFileType storyFileType){
        machineInit = new MachineFactory.MachineInitStruct();
        if(storyFileType == StoryFileType.BLORBFILE){
            machineInit.blorbFile = storyIs;
        }
        else if(storyFileType == StoryFileType.ZFILE){
            machineInit.storyFile = storyIs;
        }
        machineInit.nativeImageFactory = new NativeImageFactory() {
            public NativeImage createImage(InputStream inputStream)
                    throws IOException {
                return null;
            }
        };
        machineInit.saveGameDataStore = new SaveGameDataStore() {
            public FormChunk retrieveFormChunk() { return null; }
            public boolean saveFormChunk(WritableFormChunk formchunk) { return false; }
        };
        machineInit.ioSystem = new IOSystem() {
            public Reader getInputStreamReader() { return null; }
            public Writer getTranscriptWriter() { return null; }
        };
        screenModel = new BufferedScreenModel();
        machineInit.statusLine = screenModel;
        machineInit.screenModel = screenModel;
        machineInit.keyboardInputStream = new org.zmpp.io.InputStream() {
            public void close() { }
            public String readLine() { return null; }
        };
    }

    private void handleButtonClick(){
        executionControl.resumeWithInput(commandField.getText().toString());

        String currentText = getBufferText(screenModel);
        if(currentText.length() > 1){
            storyField.setText(currentText);
            commandField.setText("");
            tts.speak(storyField.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            startVoiceInput();
        }
        else{
            commandField.setText(" ");
            handleButtonClick();
        }
    }

    private String getBufferText(BufferedScreenModel screenModel){
        String result = "";
        for (AnnotatedText text : screenModel.getLowerBuffer()) {
            result += text.getText();
        }
        if(result.length() > 2){
            return result.substring(0,result.length()-2).trim();
        }
        return result;
    }
}
