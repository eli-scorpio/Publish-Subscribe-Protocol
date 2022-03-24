import java.net.InetAddress;

public class Subscriber {
    private int port;
    private InetAddress ip;
    private int topic;

    Subscriber(int port, InetAddress ip, int topic){
        this.port = port;
        this.ip = ip;
        this.topic = topic;
    }

    public int getPort(){
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getTopic() { return topic; }
}
