import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jefft on 4/15/2017.
 */
public class Client {

    private static final int WINDOW_SIZE = 4;
    private static final int MAX_SEG_SIZE = 16;

    private int port;
    private InetAddress IpAddy;
    private String argument;

    private int lastPacketSent = 0;
    private int waitingForAck = 0;
    private DatagramSocket clientSendSocket;
    private DatagramSocket receiveFileSocket = new DatagramSocket(20100);
    private LogWriter clientLog = new LogWriter("clientLog.txt");

    public Client(int p, InetAddress IP) throws SocketException {
        this.port = p;
        this.IpAddy = IP;
    }

    public void startClient() throws IOException, ClassNotFoundException {
        clientSendSocket = new DatagramSocket(port);
        byte[] fileRequest = argument.getBytes();
        int lastPacketNumber = (int) Math.ceil( (double) fileRequest.length / MAX_SEG_SIZE);
        ArrayList<Packet> sentPackets = new ArrayList<>();
        int eotAck = 0;


        while(true){
            while(lastPacketSent - waitingForAck < WINDOW_SIZE && lastPacketSent < lastPacketNumber){
                byte[] packetData = new byte[MAX_SEG_SIZE];
                packetData = Arrays.copyOfRange(fileRequest, lastPacketSent*MAX_SEG_SIZE, lastPacketSent*MAX_SEG_SIZE + MAX_SEG_SIZE);
                Packet fileRequestPacket = new Packet(1, lastPacketSent, packetData.length, packetData);
                byte[] requestData = Toolbox.toBytes(fileRequestPacket);
                DatagramPacket packet = new DatagramPacket(requestData, requestData.length, IpAddy, 1973);
                sentPackets.add(fileRequestPacket);
                clientSendSocket.send(packet);
                lastPacketSent++;
            }
            byte[] ackBytes = new byte[1024];
            DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);
            try{
                clientSendSocket.setSoTimeout(30);
                clientSendSocket.receive(ack);
                Packet ackPacket = (Packet) Toolbox.toObject(ack.getData());
                if (ackPacket.getSeqno() == lastPacketNumber){
                    break;
                }
                waitingForAck = Math.max(waitingForAck, ackPacket.getSeqno());
            }catch(SocketTimeoutException e){
                for(int i = waitingForAck; i < lastPacketSent; i++){
                    byte[] sendData = Toolbox.toBytes(sentPackets.get(i));
                    DatagramPacket packet = new DatagramPacket(sendData, sendData.length, IpAddy, 1973 );
                    clientSendSocket.send(packet);
                }
            }
        }
        byte[] eot = new byte[8];
        Packet eotPacket = new Packet(4, lastPacketSent, eot.length, eot);
        byte[] eotData = Toolbox.toBytes(eotPacket);
        DatagramPacket eotDgPacket = new DatagramPacket(eotData, eotData.length, IpAddy, 1973);
        clientSendSocket.send(eotDgPacket);
        listenForFile();
        clientSendSocket.close();


    }

    private void listenForFile() throws IOException, ClassNotFoundException {
        int acknowledgementSent = 0;
        int duplicatePacketsReceived = 0;

        boolean eot = false;
        System.out.println("Listening");
        int waitingOn = 0;
        ArrayList<Packet> filePackets = new ArrayList<>();
        byte[] receiveBuffer = new byte[2048];
        DatagramPacket dgFileData = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        while(!eot) {
            receiveFileSocket.receive(dgFileData);
            Packet incFilePacket = (Packet) Toolbox.toObject(dgFileData.getData());
            clientLog.addMessage("\nReceiving packet " + incFilePacket.getSeqno() + " @ " + Toolbox.getTimeStamp());
            System.out.println(incFilePacket.getSeqno());
            if (filePackets.size() > 0){
                for (Packet packet : filePackets){
                    if (packet.getSeqno() == incFilePacket.getSeqno()){
                        duplicatePacketsReceived++;
                    }
                }
            }
            if (incFilePacket.getType() == 4) {
                eot = true;
                clientLog.writeToLog();
                break;
            } else if (incFilePacket.getSeqno() == waitingOn) {
                filePackets.add(incFilePacket);
                sendAnAck(incFilePacket.getSeqno());
                acknowledgementSent++;
                waitingOn++;
                clientLog.addMessage("\nAcknowledgement " + incFilePacket.getSeqno() +" sent " + " @ " + Toolbox.getTimeStamp());
            } else {
                clientLog.addMessage("\nPacket " + incFilePacket.getSeqno() + " out of order  @ " + Toolbox.getTimeStamp());
                System.out.println("Packet out of order...binned it!");
            }
        }
        System.out.println("File Array Size: " + filePackets.size());
        System.out.println("\n\nReceiving Statistics:\n");
        System.out.println("    Acknowledgements Sent: " + acknowledgementSent);
        System.out.println("      Duplicates Received: " + duplicatePacketsReceived);
        writeFile(filePackets);
    }

    private void writeFile(ArrayList<Packet> filedata) throws IOException {
        String passedFileName = "P" + argument;
        int lengthBytesToWrite = 0;
        File getSavePath = new File(new File(passedFileName).getAbsolutePath());
        for (Packet packet : filedata){
             lengthBytesToWrite = lengthBytesToWrite + packet.getData().length;
        }
        byte[] fileToWrite = new byte[lengthBytesToWrite];
        ByteBuffer transfer = ByteBuffer.wrap(fileToWrite);
        for (Packet packet : filedata){
            transfer.put(packet.getData());
        }
        FileOutputStream fos = new FileOutputStream(getSavePath.getAbsolutePath());
        fos.write(fileToWrite);
        fos.close();
    }

    private void sendAnAck(int seqno) throws IOException {
        String jokeFiller = "What do you call a dog in summer?  A hot dog!!!";
        byte[] ackdata = jokeFiller.getBytes();
        Packet ackPacket = new Packet(0, seqno, ackdata.length, ackdata);
        byte[] ackWrapper = Toolbox.toBytes(ackPacket);
        DatagramPacket dgAck = new DatagramPacket(ackWrapper, ackWrapper.length, IpAddy, 19740);
        receiveFileSocket.send(dgAck);
    }

    public void setArgument(String arg){
        this.argument = arg;
    }
}
