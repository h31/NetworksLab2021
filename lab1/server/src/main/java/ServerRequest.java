import java.io.Serializable;
import java.util.Date;

public class ServerRequest implements Serializable {

    public ServerRequest(String requestType, String message, String time) {
        this.requestType = requestType;
        this.message = message;
        this.time = time;
    }

    private String requestType; // заменить на enum или что-то более осмысленное, {greeting, exception, exit}

    private String message;

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

    @Override
    public String toString() {
        return "ServerRequest{" +
                "requestType='" + requestType + '\'' +
                ", message='" + message + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
