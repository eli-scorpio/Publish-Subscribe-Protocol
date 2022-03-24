import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DateTime {
    public static void main(String[] args){
        try {
            DatagramSocket sensorSocket = new DatagramSocket(Constants.DATETIME_PORT);

            byte[] buffer;
            Packet packet;
            DateTimeFormatter dtf;
            LocalDateTime now;

            while(true) {
                dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                now = LocalDateTime.now();
                // Write packet
                ByteArrayOutputStream bstream = new ByteArrayOutputStream();
                ObjectOutputStream ostream = new ObjectOutputStream(bstream);
                ostream.writeInt(Constants.DATA);
                ostream.writeInt(Constants.PUBLISH);
                ostream.writeInt(Constants.DATETIME);
                ostream.writeUTF(dtf.format(now));

                ostream.flush();
                buffer = bstream.toByteArray();

                // send packet
                InetSocketAddress socketAddress = new InetSocketAddress("broker", Constants.BROKER_PORT);
                DatagramPacket packetOut = new DatagramPacket(buffer, buffer.length);
                packetOut.setSocketAddress(socketAddress);

                System.out.println("\n[DATETIME] Publishing to BROKER" );
                sensorSocket.send(packetOut);

                // wait for acknowledgment
                packet = Packet.getPacket(sensorSocket);
                System.out.println("[DATETIME] Waiting for ACK...");
                while(packet.getType() != Constants.ACK) {
                    System.out.println("[DATETIME] ERROR - ACK not received! Re-Publishing to BROKER");
                    sensorSocket.send(packetOut);
                    packet = Packet.getPacket(sensorSocket);
                }
                System.out.println("[DATETIME] OK - ACK Received!" );

                // sleep for 1 seconds
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
