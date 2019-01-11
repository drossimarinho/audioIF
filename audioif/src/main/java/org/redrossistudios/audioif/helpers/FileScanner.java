package org.redrossistudios.audioif.helpers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;

public class FileScanner {


    public  final String STORY_EXTENSIONS = "(?i).+\\.(z[1-8]|zblorb|zlb)$";
    private final AppCompatActivity activity;
    public ListView list;
    private Dialog dialog;
    private File currentPath;
    private ArrayList<String> filePathList;
    private File chosenFile;
    private boolean noFilesFound;

    public interface FileSelectedListener {
        void fileSelected(File file);
    }

    public interface FileDialogDismissedListener{
        void dialogDismissed();
    }

    public FileScanner setDialogDismissListener(FileDialogDismissedListener dialogDismissListener){
        this.dialogDismissListener = dialogDismissListener;
        return this;
    }

    private FileDialogDismissedListener dialogDismissListener;
    public FileScanner setFileListener(FileSelectedListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }
    private FileSelectedListener fileListener;


    public FileScanner(final AppCompatActivity activity) {
        this.activity = activity;
        filePathList = scanDir(getChosenFile(Environment.getExternalStorageDirectory().getPath()),new ArrayList<String>());
        if(filePathList.isEmpty()){
            noFilesFound = true;

        }
        else {
        dialog = new Dialog(activity);
        list = new ListView(activity);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                String fileChosen = filePathList.get(which);
                chosenFile = getChosenFile(fileChosen);

                    if (fileListener != null) {
                        fileListener.fileSelected(chosenFile);
                    }
                    dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(chosenFile == null && dialogDismissListener != null){
                    dialogDismissListener.dialogDismissed();
                }
            }
        });
        dialog.setContentView(list);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);


            ArrayList<String> fileList = new ArrayList<>();
            String[] filePathParts;
            String rawFileName;
            for (String filePath : filePathList) {
                filePathParts = filePath.split("/");
                rawFileName = filePathParts[filePathParts.length - 1];
                fileList.add(rawFileName);
            }
            list.setAdapter(new ArrayAdapter(activity,
                    android.R.layout.simple_list_item_1, fileList) {
                @Override
                public View getView(int pos, View view, ViewGroup parent) {
                    view = super.getView(pos, view, parent);
                    ((TextView) view).setSingleLine(true);
                    return view;
                }
            });
        }
    }


    public void showDialog() {
        if(noFilesFound){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("No story files found.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            activity.finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else{
            dialog.show();
        }
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

}
