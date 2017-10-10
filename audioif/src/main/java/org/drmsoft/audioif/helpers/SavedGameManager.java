package org.drmsoft.audioif.helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;

public class SavedGameManager {

    public void save(String fileName, ArrayList<String> commands) throws FileNotFoundException, IOException {
        FileOutputStream fout= new FileOutputStream (fileName, false);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(commands);
        fout.close();
    }

    public ArrayList<String> read(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fin= new FileInputStream (fileName);
        ObjectInputStream ois = new ObjectInputStream(fin);
        ArrayList<String> commands = (ArrayList<String>)ois.readObject();
        fin.close();
        return commands;
    }
}
