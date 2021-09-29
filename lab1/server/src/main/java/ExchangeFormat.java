import java.io.Serializable;

public class ExchangeFormat implements Serializable {

    private String parcelType; // заменить на enum или что-то более осмысленное, {greeting, exception, exit, msg}

    private String message;

    private String username;

    private String time;

    private String attachmentType;

    private String attachmentName;

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

    public String getParcelType() {
        return parcelType;
    }

    public void setParcelType(String parcelType) {
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

    public ExchangeFormat(String parcelType, String message, String username, String time, String attachmentType) {
        this.parcelType = parcelType;
        this.message = message;
        this.username = username;
        this.time = time;
        this.attachmentType = attachmentType;
    }

    public ExchangeFormat(String requestType, String message, String time) {
        this.parcelType = requestType;
        this.message = message;
        this.time = time;
    }

    public ExchangeFormat() {
    }

    @Override
    public String toString() {
        return "{" +
                "'parcelType':'" + parcelType + '\'' +
                ", 'message':'" + message + '\'' +
                ", 'username':'" + username + '\'' +
                ", 'time':'" + time + '\'' +
                ", 'attachmentType':'" + attachmentType + '\'' +
                '}';
    }
}
