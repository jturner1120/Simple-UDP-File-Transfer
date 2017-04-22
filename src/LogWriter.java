import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by jefft on 4/21/2017.
 */
public class LogWriter {
    private String logName;
    private ArrayList<String> messageList = new ArrayList<>();
    public LogWriter (String n){
        this.logName = n;
    }

    public void writeToLog () throws IOException {
        int bytelength = 0;
        for (String msg : messageList){
            bytelength = bytelength + msg.length();
        }
        byte[] toWrite = new byte[bytelength];
        ByteBuffer target = ByteBuffer.wrap(toWrite);
        for (String msg : messageList){
            target.put(msg.getBytes());
        }
        File savePath = new File(new File(logName).getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(savePath, true);
        fos.write(toWrite);
        fos.close();
    }

    public void addMessage (String add){
        messageList.add(add);
    }
}
