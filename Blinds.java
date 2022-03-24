import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Blinds {
    public static void main(String[] args){
        try {
            DatagramSocket blindsSocket = new DatagramSocket(Constants.BLINDS_PORT);
            byte[] buffer;
            Packet packet;

            // Write packet
            ByteArrayOutputStream bstream = new ByteArrayOutputStream();
            ObjectOutputStream ostream = new ObjectOutputStream(bstream);
            ostream.writeInt(Constants.DATA);
            ostream.writeInt(Constants.SUBSCRIBE);
            ostream.writeInt(Constants.BLINDS);
            ostream.writeUTF("Subscribing");
            ostream.flush();
            buffer = bstream.toByteArray();

            // send packet to broker
            InetSocketAddress socketAddress = new InetSocketAddress("broker", Constants.BROKER_PORT);
            DatagramPacket packetOut = new DatagramPacket(buffer, buffer.length);
            packetOut.setSocketAddress(socketAddress);
            System.out.println("\n[BLINDS] Subscribing to BROKER" );
            blindsSocket.send(packetOut);

            // wait for acknowledgement
            packet = Packet.getPacket(blindsSocket);
            System.out.println("[BLINDS] Waiting for ACK...");
            while(packet.getType() != Constants.ACK) {
                System.out.println("[BLINDS] ERROR - ACK not received! Re-Publishing to BROKER" );
                blindsSocket.send(packetOut);
                packet = Packet.getPacket(blindsSocket);
            }
            System.out.println("[BLINDS] OK - ACK Received!" );

            boolean open = false;   // simulate blinds false = close, true = open

            while(true) {
                System.out.println("[BLINDS] Waiting for packets...");
                packet = Packet.getPacket(blindsSocket); // wait for packet
                System.out.println("[BLINDS] Packet received!");

                Packet.sendACK(packet, blindsSocket); // send Acknowledgement
                System.out.println("[BLINDS] Sending ACK!");

                if(packet.getType() == Constants.DATA) {
                    if(packet.getMessage().equalsIgnoreCase("close")) {
                        System.out.println("[BLINDS] Closing blinds");
                        open = false;
                    }
                    else if(packet.getMessage().equalsIgnoreCase("open")) {
                        System.out.println("[BLINDS] Opening blinds");
                        open = true;
                    }
                    else
                        System.out.println("[BLINDS] Invalid command! Packet discarded!");
                }
                else if(packet.getType() == Constants.ACK)
                    System.out.println("[BLINDS] OK - ACK Received!" );

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
