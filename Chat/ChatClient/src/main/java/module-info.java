module com.example.chatclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.chatclient to javafx.fxml;
    exports com.example.chatclient;
}