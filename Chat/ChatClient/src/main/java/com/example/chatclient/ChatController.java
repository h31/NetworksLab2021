package com.example.chatclient;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

public class ChatController {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox vBox;
    @FXML
    private TextArea message;
    @FXML
    private TextField nickname;
    @FXML
    private Button changeNickname;

    private final LinkedList<String> messages = new LinkedList<>();

    private OutputStream os;
    private PrintWriter pw;
    private Coder coder = new Coder();

    private void addMessage(String message) {
        Platform.runLater(() -> {
            messages.add(message);
            Label label = new Label(message);
            label.wrapTextProperty().setValue(true);
            vBox.getChildren().add(label);
            scrollPane.vvalueProperty().bind(vBox.heightProperty());
        });
    }

    public ChatController() {
        try {
//            Socket s = new Socket(InetAddress.getByName("networkslab-ivt.ftp.sh"), 8777);
            Socket s = new Socket(InetAddress.getLocalHost(), 8777);
            InputStream is = s.getInputStream();
            Scanner sc = new Scanner(s.getInputStream());
            os = s.getOutputStream();
            pw = new PrintWriter(s.getOutputStream(), true);

            Thread t = new Thread(() -> {
                while (true) {
                    if (!sc.hasNext()) {
                        return;
                    }
                    String sInp = sc.nextLine();
                    Map<String, String> data = coder.decodeData(sInp);
                    String type = data.get("type");
                    switch (type) {
                        case "init" -> {
                            Platform.runLater(() -> nickname.setText(data.get("nickname")));
                        }
                        case "text" -> {
                            Instant instant = Instant.ofEpochSecond(Long.parseLong(data.get("timestamp")));
                            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                            addMessage(
                                    String.format(
                                            "<%02d:%02d> [%s] > %s\n",
                                            ldt.getHour(),
                                            ldt.getMinute(),
                                            data.get("nickname"),
                                            data.get("text")
                                    )
                            );
                        }
                        case "file" -> {
                            Instant instant = Instant.ofEpochSecond(Long.parseLong(data.get("timestamp")));
                            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                            addMessage(
                                    String.format(
                                        "<%02d:%02d> [%s] @ attached a file: %s\n",
                                        ldt.getHour(),
                                        ldt.getMinute(),
                                        data.get("nickname"),
                                        data.get("filename")
                                    )
                            );
                            try {
                                byte[] bytes = new byte[Integer.parseInt(data.get("filesize"))];
                                is.read(bytes, 0, bytes.length);
                                String path = "files/" + data.get("filename");
                                FileOutputStream fos = new FileOutputStream(path);
                                fos.write(bytes);
                                fos.close();
                                addMessage("File is saved in " + path);
                            }
                            catch (Exception e) {
                                addMessage("Error while reading file");
                            }
                        }
                        case "shutdown" -> {
                            addMessage("=".repeat(40));
                            addMessage("Server is shutdown");
                            addMessage("=".repeat(40));
                            System.exit(1);
                        }
                        case "error" -> addMessage("Error: " + data.get("message"));
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
        catch (IOException e) {

        }
    }

    private void send(String input) {
        if (input.isEmpty()) {
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "text");
        data.put("text", input);
        pw.println(coder.encodeData(data));
    }

    public void onNicknameInput() {
        Platform.runLater(() -> changeNickname.setDisable(false));
    }

    public void changeNickname() {
        Map<String, String> data = new HashMap<>();
        data.put("type", "nickname");
        data.put("nickname", nickname.getText());
        pw.println(coder.encodeData(data));
        Platform.runLater(() -> changeNickname.setDisable(true));
    }

    @FXML
    protected void onSend() {
        LocalDateTime ldt = LocalDateTime.now();
        addMessage(String.format(
                "<%02d:%02d> [ME] > %s\n",
                ldt.getHour(),
                ldt.getMinute(),
                message.getText()
        ));
        send(message.getText());
        message.setText("");
    }
    @FXML
    protected void onAttach() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(scrollPane.getScene().getWindow());
        if (file != null && file.isFile()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] bytes = fis.readAllBytes();
                fis.close();

                Map<String, String> data = new HashMap<>();
                data.put("type", "file");
                data.put("filename", file.getName());
                data.put("filesize", String.valueOf(bytes.length));
                pw.println(coder.encodeData(data));
                Thread.sleep(1);
                os.write(bytes);
                addMessage("@ File is sent");
            }
            catch (Exception e) {
                addMessage(e.getMessage());
            }
        }
    }


}