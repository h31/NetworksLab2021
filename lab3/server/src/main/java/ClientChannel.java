import io.netty.channel.Channel;

import java.io.IOException;

public class ClientChannel {

    private String nickname = "";

    private Channel channel;

    public ClientChannel(Channel channel) {
        this.channel = channel;
    }

    public String getNickname() {
        return nickname;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
