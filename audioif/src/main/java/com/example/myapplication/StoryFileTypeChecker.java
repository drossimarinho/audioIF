package com.example.myapplication;

import org.zmpp.vm.InvalidStoryException;

/**
 * Created by Lo on 8/19/2017.
 */

public class StoryFileTypeChecker {
    public StoryFileType GetStoryFileType(String fileExtension) throws InvalidStoryException {
        if(fileExtension.compareTo("zblorb") == 0 || fileExtension.compareTo("zblb") == 0){
            return StoryFileType.BLORBFILE;
        }
        else if(fileExtension.contains("z")){
            return StoryFileType.ZFILE;
        }
        throw new InvalidStoryException();
    }
}
