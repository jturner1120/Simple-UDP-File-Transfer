import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by jefft on 4/15/2017.
 */
public class Toolbox {
    static byte[] toBytes(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }

    static byte[] trim(byte[] bytes)
    {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }

    static int random (int n){
        return (int) (Math.random() * n +1);
    }

    static String getTimeStamp(){
        SimpleDateFormat sdftimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String timeStamp = sdftimeStamp.format(now);
        return timeStamp;
    }
}
