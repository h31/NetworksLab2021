package com.example.chatclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

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

    private final Coder coder = new Coder();

    private InetAddress address;

    private final int port = 8777;

    private DatagramSocket socket;

    public ChatController() {
        try {
            address = InetAddress.getLocalHost();
            socket = new DatagramSocket();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
            return;
        }
        send("type=init");


        Thread t = new Thread(() -> {
            while (true) {
                String sInp = receive(65536);
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
                    case "shutdown" -> {
                        addMessage("=".repeat(40));
                        addMessage("Server is shutdown");
                        addMessage("=".repeat(40));
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.exit(1);
                    }
                    case "error" -> addMessage("Error: " + data.get("message"));
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void send(String str) {
        byte[] buffer = str.getBytes(StandardCharsets.UTF_8);
        DatagramPacket p = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            socket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String receive(int length) {
        byte[] buffer = new byte[length];
        DatagramPacket p = new DatagramPacket(buffer, length);
        try {
            socket.receive(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(p.getData(), 0, p.getLength());
    }

    private void addMessage(String message) {
        Platform.runLater(() -> {
            Label label = new Label(message);
            label.wrapTextProperty().setValue(true);
            vBox.getChildren().add(label);
            scrollPane.vvalueProperty().bind(vBox.heightProperty());
        });
    }

    public void onNicknameInput() {
        Platform.runLater(() -> changeNickname.setDisable(false));
    }

    public void changeNickname() {
        Map<String, String> data = new HashMap<>();
        data.put("type", "nickname");
        data.put("nickname", nickname.getText());
        send(coder.encodeData(data));
        Platform.runLater(() -> changeNickname.setDisable(true));
    }

    @FXML
    protected void onSend() {
        String input = message.getText();
        if (input.isEmpty()) {
            return;
        }
        LocalDateTime ldt = LocalDateTime.now();
        addMessage(String.format(
                "<%02d:%02d> [ME] > %s\n",
                ldt.getHour(),
                ldt.getMinute(),
                message.getText()
        ));
        message.setText("");

        Map<String, String> data = new HashMap<>();
        data.put("type", "text");
        data.put("text", input);
        send(coder.encodeData(data));
    }
}
