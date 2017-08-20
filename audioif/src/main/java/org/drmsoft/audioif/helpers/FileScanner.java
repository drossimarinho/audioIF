package org.drmsoft.audioif.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import static android.R.id.list;

public class FileScanner {


    public  final String STORY_EXTENSIONS = "(?i).+\\.(z[1-8]|zblorb|zlb)$";
    private final Activity activity;
    private ListView list;
    private Dialog dialog;
    private File currentPath;

    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    public FileScanner setFileListener(FileSelectedListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }
    private FileSelectedListener fileListener;


    public FileScanner(Activity activity) {
        this.activity = activity;
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
