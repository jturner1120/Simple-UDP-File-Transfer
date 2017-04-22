import java.io.Serializable;

/**
 * Created by jefft on 4/15/2017.
 */
public class Packet implements Serializable{

    private int type;
    private int seqno;
    private int size;
    private byte[] data;

    public Packet(int t, int i, int j, byte abyte[]){
        type = t;
        seqno = i;
        size = j;
        data = new byte[size];
        data = abyte;
    }

    public byte[] getData() {
        return data;
    }

    public int getSeqno() {
        return seqno;
    }

    public int getSize() {
        return size;
    }

    public int getType() {
        return type;
    }

    public String toString(){
        return "type: " + type + " seq: " + seqno + " size: " + size + " data " + data;
    }
}
