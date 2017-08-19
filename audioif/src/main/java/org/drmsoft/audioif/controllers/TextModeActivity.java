package org.drmsoft.audioif.controllers;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.drmsoft.audioif.R;
import org.drmsoft.audioif.helpers.Alert;
import org.drmsoft.audioif.helpers.FileChooser;
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

public class TextModeActivity extends AppCompatActivity {

    private ExecutionControl executionControl;
    private MachineFactory.MachineInitStruct machineInit;
    private BufferedScreenModel screenModel;
    private TextView storyField;
    private EditText commandField;
    private Button button;
    private InputStream storyIs;
    private FileChooser fileChooser;
    private StoryFileTypeChecker storyFileTypeChecker;
    private org.drmsoft.audioif.helpers.Alert Alert;
    private ScrollView scrollView;
    private TextView footer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Alert = new Alert(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_mode);
        storyField = (TextView) findViewById(R.id.textView);
        storyField.setText("");
        commandField = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        footer = (TextView) findViewById(R.id.footer);
        storyFileTypeChecker = new StoryFileTypeChecker();

        fileChooser = new FileChooser(this);
        fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
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
            String initialText = getBufferText(screenModel);
            if(initialText.length() > 1){
                storyField.append(initialText);
                //scrollView.fullScroll(View.FOCUS_DOWN);
                //footer.requestFocus();
                //scrollView.scrollTo(0, scrollView.getBottom());
            }
            else{
                commandField.setText(" ");
                handleButtonClick();
            }

        } catch (Exception ex) {
            Alert.show("Error", ex.toString() + "\n" + ex.getMessage());
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
            storyField.append("\n\n >" + commandField.getText() + "\n\n" + currentText);
            //scrollView.fullScroll(View.FOCUS_DOWN);
            footer.requestFocus();
            //scrollView.scrollTo(0, scrollView.getBottom());
            commandField.setText("");
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
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                handleButtonClick();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}
