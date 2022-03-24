import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// Packet class that provides nice functionality
public class Packet {
    private int type = 0;
    private int query = 0;
    private int topic = 0;
    private String message = "";
    private InetAddress address;
    private int port;

    // Convert a DatagramPacket object to a Packet object
    public static Packet toPacket(DatagramPacket datagramPacket) throws IOException {
        Packet packet = new Packet();
        byte[] buffer;
        ByteArrayInputStream bin;
        ObjectInputStream oin;
        buffer = datagramPacket.getData();
        bin = new ByteArrayInputStream(buffer);
        oin = new ObjectInputStream(bin);

        // read packet
        packet.type = oin.readInt();
        packet.query = oin.readInt();
        packet.topic = oin.readInt();
        packet.message = oin.readUTF();

        packet.port = datagramPacket.getPort();
        packet.address = datagramPacket.getAddress();

        return packet;
    }

    // Convert Packet object to a DatagramPacket object
    public static DatagramPacket toDatagramPacket(Packet packet) {
        DatagramPacket datagramPacket= null;

        try {
            ByteArrayOutputStream bout;
            ObjectOutputStream oout;
            byte[] data;

            bout= new ByteArrayOutputStream();
            oout= new ObjectOutputStream(bout);

            oout.writeInt(packet.type);
            oout.writeInt(packet.query);
            oout.writeInt(packet.topic);
            oout.writeUTF(packet.message);
            oout.flush();
            data = bout.toByteArray(); // convert content to byte array

            datagramPacket = new DatagramPacket(data, data.length); // create packet from byte array
            oout.close();
            bout.close();
        }
        catch(Exception e) {e.printStackTrace();}

        return datagramPacket;
    }

    // get a packet from a DatagramSocket and return a Packet object
    public static Packet getPacket(DatagramSocket datagramSocket) throws IOException {
        Packet packet;
        byte[] buffer = new byte[Constants.MTU];
        DatagramPacket packetIn = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(packetIn);
        packet = Packet.toPacket(packetIn);
        return packet;
    }

    public static void sendACK(Packet packet, DatagramSocket socket) throws IOException {
        ByteArrayOutputStream bostream = new ByteArrayOutputStream();
        ObjectOutputStream oostream = new ObjectOutputStream(bostream);
        byte[] buffer;
        oostream.writeInt(Constants.ACK);
        oostream.writeInt(Constants.PUBLISH);
        oostream.writeInt(packet.topic);
        oostream.writeUTF("ACK");
        oostream.flush();
        oostream.close();
        buffer = bostream.toByteArray();
        bostream.close();

        DatagramPacket packetOut = new DatagramPacket(
                buffer,
                buffer.length,
                packet.address,
                packet.port
        );

        socket.send(packetOut);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getQuery() {
        return query;
    }

    public int getTopic() {
        return topic;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}

