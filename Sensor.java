import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

// Sensors on their own local network
public class Sensor implements Runnable{
    private final int SENSOR_PORT;
    private final String ROOM_NAME;

    public Sensor(int sensorPort, String roomName) {
        SENSOR_PORT = sensorPort;
        ROOM_NAME = roomName;
    }

    @Override
    public void run() {
        try {
            DatagramSocket sensorSocket = new DatagramSocket(SENSOR_PORT);

            byte[] buffer;
            double temp = 20;
            DecimalFormat df = new DecimalFormat("#.#");
            Packet packet;

            while(true) {
                // Write packet
                ByteArrayOutputStream bstream = new ByteArrayOutputStream();
                ObjectOutputStream ostream = new ObjectOutputStream(bstream);
                ostream.writeInt(Constants.DATA);
                ostream.writeInt(Constants.PUBLISH);
                ostream.writeInt(Constants.TEMPERATURE);
                ostream.writeUTF("(" + ROOM_NAME + ") Temperature changed to: " + df.format(temp) + " degrees");

                ostream.flush();
                buffer = bstream.toByteArray();

                // send packet
                InetSocketAddress socketAddress = new InetSocketAddress("broker", Constants.BROKER_PORT);
                DatagramPacket packetOut = new DatagramPacket(buffer, buffer.length);
                packetOut.setSocketAddress(socketAddress);

                System.out.println("\n[" + ROOM_NAME.toUpperCase() + " SENSOR] Publishing to BROKER" );
                sensorSocket.send(packetOut);

                packet = Packet.getPacket(sensorSocket); // wait for packet

                // wait for ack if we receive anything else resend data in case an error occurred
                System.out.println("[" + ROOM_NAME.toUpperCase() + " SENSOR] Waiting for ACK...");
                while(packet.getType() != Constants.ACK) {
                    System.out.println("[" + ROOM_NAME.toUpperCase() + " SENSOR] ERROR - ACK not received! Re-Publishing to BROKER" );
                    sensorSocket.send(packetOut);
                    packet = Packet.getPacket(sensorSocket);
                }
                System.out.println("[" + ROOM_NAME.toUpperCase() + " SENSOR] OK - ACK Received!" );

                // sleep for 8 seconds
                TimeUnit.SECONDS.sleep(8);

                // simulate a change in temperature
                double min = temp - 1;
                double max = temp + 1;
                temp = (Math.random() * (max-min)) + min;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Sensor sensor1 = new Sensor(5100, "Room 1");
        Sensor sensor2 = new Sensor(5101, "Room 2");
        Sensor sensor3 = new Sensor(5102, "Room 3");

        Thread sensorThread1 = new Thread(sensor1);
        Thread sensorThread2 = new Thread(sensor2);
        Thread sensorThread3 = new Thread(sensor3);

        sensorThread1.start();
        sensorThread2.start();
        sensorThread3.start();
    }
}
