import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jefft on 4/18/2017.
 */
public class SendFile {

    private ArrayList<byte[]> fileToSend;
    private InetAddress IpAddy = InetAddress.getByName("localhost");
    private int segmentSize = 16;
    private int windowSize = 4;
    private int numOBytes = 0;
    private DatagramSocket fileSendingSocket = new DatagramSocket(19740);
    private int reliability;
    private LogWriter sendLog = new LogWriter("sendLog.txt");

    public SendFile(ArrayList<byte[]> sf, int r) throws SocketException, UnknownHostException {
        this.fileToSend = sf;
        this.reliability = r;
    }

    public void sendAFile() throws IOException, ClassNotFoundException {


        int lastPacketInTransfer;
        int lastPacketSent = 0;
        int waitingToAck = 0;
        int totalPacketsTransmitted = 0;
        int totalRetransmissions = 0;
        int totalAcknowledgementsReceived = 0;
        int totalBytesofDataSent = 0;

        lastPacketInTransfer = computeLastPacketinGroup(fileToSend);
        ArrayList<Packet> packetsSent = new ArrayList<>();
        byte[] fileInBytes = new byte[numOBytes];
        ByteBuffer target = ByteBuffer.wrap(fileInBytes);
        for (byte[] data : fileToSend){
            target.put(data);
        }
        System.out.println("Starting file transfer...");
        while(true){
            if (Toolbox.random(100) < 5){
                System.out.println("Dropping Packets");
                if (reliability == 1 && lastPacketSent <= lastPacketInTransfer){
                    System.out.println("First Packet");
                    sendLog.addMessage("\nPacket " + lastPacketSent + " dropped @ " + Toolbox.getTimeStamp());
                    byte[] fileBit = new byte[segmentSize];
                    fileBit = Arrays.copyOfRange(fileInBytes, segmentSize * lastPacketSent, segmentSize * lastPacketSent + segmentSize);
                    Packet droppedPacket = new Packet(2, lastPacketSent, fileBit.length, fileBit);
                    packetsSent.add(droppedPacket);
                    lastPacketSent++;
                }else if(reliability == 2 && lastPacketSent <= lastPacketInTransfer - 4){
                    System.out.println("All Packets");
                    for (int i = 0; i < windowSize; i++){
                        byte[] fileBit = new byte[segmentSize];
                        fileBit = Arrays.copyOfRange(fileInBytes, segmentSize * lastPacketSent, segmentSize * lastPacketSent + segmentSize);
                        Packet droppedPacket = new Packet(2, lastPacketSent, fileBit.length, fileBit);
                        sendLog.addMessage("\nPacket " + lastPacketSent + " dropped @ " + Toolbox.getTimeStamp());
                        packetsSent.add(droppedPacket);
                        lastPacketSent++;
                    }
                }
            }
            while(lastPacketSent - waitingToAck < windowSize && lastPacketSent <= lastPacketInTransfer){
                byte[] fileSegment = new byte[segmentSize];
                fileSegment = Arrays.copyOfRange(fileInBytes, segmentSize * lastPacketSent, segmentSize * lastPacketSent + segmentSize);
                Packet sendingPacket = new Packet(2, lastPacketSent, fileSegment.length, fileSegment);
                byte[] wrapPacket = Toolbox.toBytes(sendingPacket);
                DatagramPacket segmentDelivery = new DatagramPacket(wrapPacket, wrapPacket.length, IpAddy, 20100);
                System.out.println("sending: " + sendingPacket.getSeqno());
                sendLog.addMessage("\nPacket " + sendingPacket.getSeqno() + " sent @ " + Toolbox.getTimeStamp());
                packetsSent.add(sendingPacket);
                lastPacketSent++;
                totalPacketsTransmitted++;
                totalBytesofDataSent = totalBytesofDataSent + wrapPacket.length;
                fileSendingSocket.send(segmentDelivery);
            }
            byte[] ackbytes = new byte[1024];
            DatagramPacket dgAckbytes = new DatagramPacket(ackbytes, ackbytes.length);
            try{
                fileSendingSocket.setSoTimeout(30);
                fileSendingSocket.receive(dgAckbytes);
                Packet ackPacket = (Packet) Toolbox.toObject(dgAckbytes.getData());
                sendLog.addMessage("\nPacket " + ackPacket.getSeqno() + " acked @ " + Toolbox.getTimeStamp());
                totalAcknowledgementsReceived++;
                System.out.println("Acknowledment #: " + ackPacket.getSeqno());
                if (ackPacket.getSeqno() == lastPacketInTransfer){
                    sendLog.writeToLog();
                    sendEotPacket(ackPacket.getSeqno());
                    System.out.println("\n\nSending Statistics:\n");
                    System.out.println("         Packets Expected: " + totalPacketsTransmitted);
                    System.out.println("      Packets Transmitted: " + totalPacketsTransmitted);
                    System.out.println("   Packets Re-Transmitted: " + totalRetransmissions);
                    System.out.println("Acknowledgements Received: " + totalAcknowledgementsReceived);
                    System.out.println("\n");
                    System.out.println("         Size of File (B): " + fileInBytes.length);
                    System.out.println("         Total Bytes Sent: " + totalBytesofDataSent);
                    break;
                }
                waitingToAck = Math.max(ackPacket.getSeqno(), waitingToAck);
            }catch (SocketTimeoutException e){
                for(int i = waitingToAck; i < lastPacketSent; i++){
                    byte[] resendData = Toolbox.toBytes(packetsSent.get(i));
                    DatagramPacket dgResend = new DatagramPacket(resendData, resendData.length, IpAddy, 20100);
                    System.out.println("re-sending: " + packetsSent.get(i).getSeqno());
                    sendLog.addMessage("\nPacket " + packetsSent.get(i).getSeqno() + " re-sent @ " + Toolbox.getTimeStamp());
                    fileSendingSocket.send(dgResend);
                    totalBytesofDataSent = totalBytesofDataSent + resendData.length;
                    totalRetransmissions++;
                }
            }
        }
    }

    private void sendEotPacket(int seqno) throws IOException {
        String anotherJoke = "What prize do you get for putting your phone on vibrate?  The \"No Bell Prize\"";
        byte[] jokePayload = anotherJoke.getBytes();
        Packet eotPacket = new Packet(4, seqno, jokePayload.length, jokePayload);
        byte[] bittenEotPacket = Toolbox.toBytes(eotPacket);
        DatagramPacket dgEot = new DatagramPacket(bittenEotPacket, bittenEotPacket.length, IpAddy, 20100);
        fileSendingSocket.send(dgEot);
    }

    private int computeLastPacketinGroup(ArrayList<byte[]> fts){
        int lastPacketNumber;
        ArrayList<byte[]> compArr = fts;
        for(byte[] item : compArr){
            numOBytes = numOBytes + item.length;
        }

        lastPacketNumber = (int)Math.ceil((double) numOBytes/segmentSize);

        return lastPacketNumber;
    }
}
