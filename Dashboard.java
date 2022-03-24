import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Dashboard{
    private static int topic;
    private static DatagramSocket dashboardSocket;
    private static ByteArrayOutputStream bostream;
    private static ObjectOutputStream oostream;
    private static byte[] buffer;

    public static void main(String[] args) throws Exception {
        dashboardSocket = new DatagramSocket(Constants.DASHBOARD_PORT);
        bostream = new ByteArrayOutputStream();
        oostream = new ObjectOutputStream(bostream);

        // Keep asking user to choose a query until user subscribes
        // then we will wait for packets from the topic we subscribed to
        while(askQuery() == Constants.PUBLISH){
            userPublish();
        }

        // Subscribe
        topic = askTopics();
        System.out.println("[DASHBOARD] Subscribing to TOPIC: " + ((topic == 0)? "TEMPERATURE":
                                                                    (topic == 1)? "DATETIME":
                                                                    "UNDEFINED"));
        sendPacket(Constants.DATA, Constants.SUBSCRIBE, topic, "Subscribing");

        while(true){
            System.out.println("\n[DASHBOARD] Waiting for packets... ");
            Packet packet = Packet.getPacket(dashboardSocket); // wait for packet

            if(packet.getType() == Constants.DATA) {
                System.out.println("[DASHBOARD] Data packet received!");
                Packet.sendACK(packet, dashboardSocket); // send acknowledgment
                System.out.println("[DASHBOARD] Sending ACK!");

                //if packet Topic is not what we subscribed to, ignore
                if (packet.getTopic() == topic) {
                    if (topic == Constants.TEMPERATURE)
                        System.out.println("\n[DASHBOARD] Temperature: " + packet.getMessage());
                    else if(topic == Constants.DATETIME)
                        System.out.println("\n[DASHBOARD] Date and Time: " + packet.getMessage());
                } else
                    System.out.println("[DASHBOARD] Packet discarded!");
            }
            else if(packet.getType() == Constants.ACK){
                System.out.println("[DASHBOARD] OK - ACK Received");
            }
        }
    }

    // ask user what topic they want to publish to
    private static void userPublish() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("[DASHBOARD] CHOOSE TOPIC TO PUBLISH");
        System.out.println("Select Topic:\n2 BLINDS\n3 AC");
        while(!sc.hasNextInt()) {
            System.out.println("[DASHBOARD] USER ERROR: Select query 2 or 3 by typing the respective number");
            System.out.println("Select Topic:\n2 BLINDS\n3 AC");
            sc.nextLine(); // flush scanner
        }
        int topic = sc.nextInt();
        sc.nextLine(); // flush scanner
        if(topic == Constants.BLINDS || topic == Constants.AC) {
            System.out.print("[DASHBOARD] Type command and press enter to publish > ");
            String command = sc.nextLine();
            publish(topic, command);
        }
        else
            System.out.println("[DASHBOARD] ERROR: Invalid topic number!");
    }

    // publish to a topic
    private static void publish(int topic, String message) throws IOException {
        System.out.println("[DASHBOARD] Publishing to " + ((topic == Constants.BLINDS)? "BLINDS":
                                                            (topic == Constants.AC)? "AC" : "Not implemented"));
        sendPacket(Constants.DATA, Constants.PUBLISH, topic, message);

        Packet packet = Packet.getPacket(dashboardSocket);
        System.out.println("[DASHBOARD] Waiting for ACK...");
        while(packet.getType() != Constants.ACK) {
            System.out.println("[DASHBOARD] ERROR - ACK not received! Re-Publishing to BROKER" );
            sendPacket(Constants.DATA, Constants.PUBLISH, topic, message);
            packet = Packet.getPacket(dashboardSocket);
        }
        System.out.println("[DASHBOARD] OK - ACK Received!" );
    }

    // send a packet
    private static void sendPacket(int type, int query, int topic, String message) throws IOException {
        bostream = new ByteArrayOutputStream();
        oostream = new ObjectOutputStream(bostream);
        oostream.writeInt(type);
        oostream.writeInt(query);
        oostream.writeInt(topic);
        oostream.writeUTF(message);
        oostream.flush();
        oostream.close();
        buffer = bostream.toByteArray();
        bostream.close();

        InetSocketAddress socketAddress = new InetSocketAddress("broker", Constants.BROKER_PORT);
        DatagramPacket packetOut = new DatagramPacket(buffer, buffer.length);
        packetOut.setSocketAddress(socketAddress);

        dashboardSocket.send(packetOut);
    }

    // ask user for a topic
    private static int askTopics(){
        Scanner sc = new Scanner(System.in);
        System.out.println("[DASHBOARD] SELECT A TOPIC TO SUBSCRIBE TO");
        System.out.println("Select Topic:\n0 Temperature\n1 DateTime");
        while(!sc.hasNextInt()) {
            System.out.println("[DASHBOARD] USER ERROR: Select query 0,1 or 2 by typing the respective number");
            System.out.println("Select Topic:\n0 Temperature\n1 DateTime");
            sc.nextLine(); // flush scanner
        }
        return sc.nextInt();
    }

    // ask user for a query i.e Publish or subscribe
    private static int askQuery(){
        Scanner sc = new Scanner(System.in);
        System.out.println("[DASHBOARD] SELECT A QUERY");
        System.out.println("Select Topic:\n0 PUBLISH\n1 SUBSCRIBE");
        while(!sc.hasNextInt()) {
            System.out.println("[DASHBOARD] USER ERROR: Select query 0,1 or 2 by typing the respective number");
            System.out.println("Select Topic:\n0 PUBLISH\n1 SUBSCRIBE");
            sc.nextLine(); // flush scanner
        }
        return sc.nextInt();
    }
}
