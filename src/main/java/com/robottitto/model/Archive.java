package com.robottitto.model;

public class Archive {

    private int id;
    private String name;
    private int directoryId;
    private byte[] binaryFile;

    public Archive() {
    }

    public Archive(int id, String name, int directoryId) {
        this.id = id;
        this.name = name;
        this.directoryId = directoryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(int directoryId) {
        this.directoryId = directoryId;
    }

    public byte[] getBinaryFile() {
        return binaryFile;
    }

    public void setBinaryFile(byte[] binaryFile) {
        this.binaryFile = binaryFile;
    }

}
