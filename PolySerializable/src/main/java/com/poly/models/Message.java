package com.poly.models;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Message {

    private String date;
    private String name;
    private String text;
    private String fileName;
    private Integer fileSize;

    public Message(String date, String name, String text, String fileName, Integer fileSize) {
        this.date = date;
        this.name = name;
        this.text = text;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public Message() {
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getFileName() {
        return fileName;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String message) {
        this.text = message;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "Message{" +
                "date='" + date + '\'' +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, name, text, fileName, fileSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(date, message.date) &&
                Objects.equals(name, message.name) &&
                Objects.equals(text, message.text) &&
                Objects.equals(fileSize, message.fileSize) &&
                Objects.equals(fileName, message.fileName);
    }

    public String toTransferString() {
        return "date : " + date + " , " +
                "name : " + name + " , " +
                "text : " + text
                .replace(",", "\\,")
                .replace(":", "\\:") + " , " +
                "fileName : " + fileName + " , " +
                "fileSize : " + fileSize;
    }

    public void parseToMessage(String message) {
        String[] messageParts = message
                .split(" , ");
        List<String> messagePartsWithoutGarbage = Arrays.stream(messageParts)
                .map(it -> {
                    String[] partsOfMessageField = it.split(" : ");
                    return (partsOfMessageField.length > 1) ? partsOfMessageField[1] : "";
                }).collect(Collectors.toList());

        setDate(messagePartsWithoutGarbage.get(0).trim());
        setName(messagePartsWithoutGarbage.get(1).trim());
        setText(messagePartsWithoutGarbage.get(2)
                .replaceAll("\\\\,", ",")
                .replaceAll("\\\\:", ":")
        );
        setFileName(messagePartsWithoutGarbage.get(3).equals("null")
                ? null : messagePartsWithoutGarbage.get(3));
        setFileSize(messagePartsWithoutGarbage.get(4).equals("null")
                ? null : Integer.valueOf(messagePartsWithoutGarbage.get(4)));
    }
}
