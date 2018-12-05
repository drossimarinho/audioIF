package org.drmsoft.audioif.helpers;

import org.zmpp.base.DefaultMemory;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.vm.SaveGameDataStore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SavedGameManager implements SaveGameDataStore {

    private String savedGameFilePath;

    public SavedGameManager(String savedGameFilePath){
        this.savedGameFilePath = savedGameFilePath;
    }

    @Override
    public boolean saveFormChunk(WritableFormChunk formchunk) {
        File savefile = new File(savedGameFilePath);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(savefile, "rw");
            byte[] data = formchunk.getBytes();
            raf.write(data);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (raf != null){
                try {
                    raf.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public FormChunk retrieveFormChunk() {
        File savedFile = new File(savedGameFilePath);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(savedFile, "r");
            byte[] data = new byte[(int) raf.length()];
            raf.readFully(data);
            return new DefaultFormChunk(new DefaultMemory(data));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (raf != null){
                try {
                    raf.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }
}
