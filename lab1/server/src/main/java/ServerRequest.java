import java.io.Serializable;
import java.util.Date;

public class ServerRequest implements Serializable {

    private String requestType; // заменить на enum или что-то более осмысленное, {greeting, exception, exit, msg}

    private String message;

    // file
    // расширение файла

    private String username;

    private String time;

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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ServerRequest(String requestType, String message, String username, String time) {
        this.requestType = requestType;
        this.message = message;
        this.username = username;
        this.time = time;
    }

    public ServerRequest(String requestType, String message, String time) {
        this.requestType = requestType;
        this.message = message;
        this.time = time;
    }

    public ServerRequest() {
    }

    @Override
    public String toString() {
        return "{" +
                "'requestType':'" + requestType + '\'' +
                ", 'message':'" + message + '\'' +
                ", 'username':'" + username + '\'' +
                ", 'time':'" + time + '\'' +
                '}';
    }
}
