package model;

import util.Tool;

import java.io.Serializable;

public class ExchangeFormat implements Serializable {

    private Tool.RequestType parcelType;

    private String message;

    private String username;

    private String time;

    private String attachmentType;

    private String attachmentName;

    private int attachmentSize;

    private byte[] attachmentByteArray; // inner field

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Tool.RequestType getParcelType() {
        return parcelType;
    }

    public void setParcelType(Tool.RequestType parcelType) {
        this.parcelType = parcelType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public int getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(int attachmentSize) {
        this.attachmentSize = attachmentSize;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public byte[] getAttachmentByteArray() {
        return attachmentByteArray;
    }

    public void setAttachmentByteArray(byte[] attachmentByteArray) {
        this.attachmentByteArray = attachmentByteArray;
    }

    public ExchangeFormat() {
    }

    public String toParcel() {
        return "{" +
                "'parcelType':'" + parcelType.getStringValue() + '\'' +
                ", 'message':'" + message + '\'' +
                ", 'username':'" + username + '\'' +
                ", 'time':'" + time + '\'' +
                ", 'attachmentType':'" + attachmentType + '\'' +
                ", 'attachmentName':'" + attachmentName + '\'' +
                ", 'attachmentSize':'" + attachmentSize + '\'' +
                '}';
    }
}
