import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class AC {
    public static void main(String[] args){
        try {
            DatagramSocket blindsSocket = new DatagramSocket(Constants.AC_PORT);

            byte[] buffer;
            Packet packet;
            // Write packet
            ByteArrayOutputStream bstream = new ByteArrayOutputStream();
            ObjectOutputStream ostream = new ObjectOutputStream(bstream);
            ostream.writeInt(Constants.DATA);
            ostream.writeInt(Constants.SUBSCRIBE);
            ostream.writeInt(Constants.AC);
            ostream.writeUTF("Subscribing");

            ostream.flush();
            buffer = bstream.toByteArray();

            // send packet
            InetSocketAddress socketAddress = new InetSocketAddress("broker", Constants.BROKER_PORT);
            DatagramPacket packetOut = new DatagramPacket(buffer, buffer.length);
            packetOut.setSocketAddress(socketAddress);

            System.out.println("\n[AC] Subscribing to BROKER" );
            blindsSocket.send(packetOut);

            // wait for acknowledgement
            packet = Packet.getPacket(blindsSocket);
            System.out.println("[AC] Waiting for ACK...");
            while(packet.getType() != Constants.ACK) {
                System.out.println("[AC] ERROR - ACK not received! Re-Publishing to BROKER" );
                blindsSocket.send(packetOut);
                packet = Packet.getPacket(blindsSocket);
            }
            System.out.println("[AC] OK - ACK Received!" );

            boolean open = false;   // simulate blinds false = close, true = open

            while(true) {
                System.out.println("[AC] Waiting for packets...");
                packet = Packet.getPacket(blindsSocket); // wait for packet
                System.out.println("[AC] Packet received!");

                Packet.sendACK(packet, blindsSocket); // send acknowledgement
                System.out.println("[AC] Sending ACK!");

                if(packet.getType() == Constants.DATA) {
                    if(packet.getMessage().equalsIgnoreCase("on")) {
                        System.out.println("[AC] Turning on AC");
                        open = false;
                    }
                    else if(packet.getMessage().equalsIgnoreCase("off")) {
                        System.out.println("[AC] Turning off AC");
                        open = true;
                    }
                    else
                        System.out.println("[AC] Invalid command! Packet discarded!");
                }
                else if(packet.getType() == Constants.ACK)
                    System.out.println("[AC] OK - ACK Received!" );

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
