import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Broker{
    private static DatagramSocket brokerSocket;

    public static void main(String[] args) {
        try {
            brokerSocket = new DatagramSocket(Constants.BROKER_PORT);
            ArrayList<Subscriber> subscribers = new ArrayList<>();

            while(true) {
                System.out.println("\n[BROKER] Waiting for packet...");
                Packet packet = Packet.getPacket(brokerSocket); // wait for packet

                if (packet.getType() == Constants.DATA) {
                    System.out.println("[BROKER] Data Packet received!");
                    Packet.sendACK(packet,brokerSocket); // send acknowledgment
                    System.out.println("[BROKER] Sending ACK");

                    // check if packet is Subscribing or Publishing
                    if (packet.getQuery() == Constants.SUBSCRIBE) {
                        // add a new subscriber
                        subscribers.add(new Subscriber(packet.getPort(), packet.getAddress(), packet.getTopic()));
                        System.out.println("\n[BROKER] Subscriber Added!");
                    } else if (packet.getQuery() == Constants.PUBLISH) {
                        // if there are subscribers publish otherwise discard the packet
                        System.out.println("[BROKER] Checking Subscriptions");
                        if (!subscribers.isEmpty()) {
                            for (int i = 0; i < subscribers.size(); i++) {
                                if (packet.getTopic() == Constants.TEMPERATURE && subscribers.get(i).getTopic() == Constants.TEMPERATURE ||
                                        packet.getTopic() == Constants.DATETIME && subscribers.get(i).getTopic() == Constants.DATETIME ||
                                        packet.getTopic() == Constants.BLINDS && subscribers.get(i).getTopic() == Constants.BLINDS ||
                                        packet.getTopic() == Constants.AC && subscribers.get(i).getTopic() == Constants.AC ) {
                                    System.out.println("\n[BROKER] Publishing to subscribers");
                                    InetSocketAddress address = new InetSocketAddress(subscribers.get(i).getIp(),
                                            subscribers.get(i).getPort());
                                    DatagramPacket datagramPacket = Packet.toDatagramPacket(packet);
                                    datagramPacket.setSocketAddress(address);

                                    brokerSocket.send(datagramPacket);
                                }
                            }
                        } else
                            System.out.println("[BROKER] Packet discarded (No Subscribers to packets topic)");
                    } else
                        System.out.println("[BROKER] Message Received: " + packet.getMessage());
                }
                else if(packet.getType() == Constants.ACK){
                    System.out.println("[BROKER] OK - ACK Received");
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}