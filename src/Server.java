import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by jefft on 4/15/2017.
 */
public class Server {

    private int port;
    private int reliabilityMode;
    private InetAddress IpAddy;
    private String filename = "";
    private DatagramSocket serverReceiveSocket;

    public Server(int p, InetAddress IP){
        this.port = p;
        this.IpAddy = IP;
    }

    public void startServer() throws IOException, ClassNotFoundException {
        serverReceiveSocket = new DatagramSocket(port);
        byte[] receivedData = new byte[1024];
        int waitingFor = 0;
        ArrayList<Packet> receivedPackets = new ArrayList<>();
        int incomingPacketType = 0;

        while(incomingPacketType != 4){
            DatagramPacket receivedDgPacket = new DatagramPacket(receivedData, receivedData.length);
            serverReceiveSocket.receive(receivedDgPacket);
            Packet receivedPacket = (Packet) Toolbox.toObject(receivedDgPacket.getData());
            incomingPacketType = receivedPacket.getType();
            if(incomingPacketType == 4){
                filename = printFileName(receivedPackets);
            }else if(receivedPacket.getSeqno() == waitingFor){
                waitingFor++;
                receivedPackets.add(receivedPacket);
            }
            byte[] ackByte = new byte[32];
            Packet ackPacket = new Packet(0, waitingFor, ackByte.length, ackByte);
            byte[] ackByteData = Toolbox.toBytes(ackPacket);
            DatagramPacket ackDg = new DatagramPacket(ackByteData, ackByteData.length, receivedDgPacket.getAddress(), receivedDgPacket.getPort());
            serverReceiveSocket.send(ackDg);
        }
        serverReceiveSocket.close();
        ReadFile requestedFile = new ReadFile();
        ArrayList<byte[]> byteFile = requestedFile.readInFile(filename);
        SendFile myFileSender = new SendFile(byteFile, reliabilityMode);
        myFileSender.sendAFile();
        LogWriter serverLog = new LogWriter("fileLog.txt");
        serverLog.addMessage(filename + " Served @ " + Toolbox.getTimeStamp() + "\n");
        serverLog.writeToLog();
    }

    private String printFileName(ArrayList<Packet> pal){
        ArrayList<byte[]> packetData = new ArrayList<>();
        for (int i = 0; i < pal.size(); i++){
            packetData.add(pal.get(i).getData());
        }
        for(byte[] data : packetData){
            byte[] temp = Toolbox.trim(data);
            String printstr = new String(temp);
            filename = filename + printstr;
        }
        return filename;
    }

    public void setReliabilityMode(int r){
        this.reliabilityMode = r;
    }

}
