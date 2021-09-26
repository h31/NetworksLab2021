import java.io.Serializable;
import java.util.Date;

public class Request implements Serializable {

    public Request(String message, String time) {
        this.message = message;
        this.time = time;
    }

    private String message;

    private String time;

    @Override
    public String toString() {
        return "Request{" +
                "message='" + message + '\'' +
                ", time=" + time +
                '}';
    }
}
