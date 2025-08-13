package com.crud.ithreeamcrud;


import java.io.File;

public class FolderEntry {
    private  String folderName;
    private  String path;
    private  File folder;

    public FolderEntry(){}

    public FolderEntry(String folderName, String path, File folder) {
        this.folderName = folderName;
        this.path = path;
        this.folder = folder;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getPath() {
        return path;
    }

    public File getFolder() {
        return folder;
    }

    public File toFile() {
        return new File(path);
    }
}