import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jefft on 4/18/2017.
 */
public class ReadFile {

    private String filePath = "c:\\Users\\jefft\\networkFinal\\";
    private ArrayList<byte[]> fileByteStorage = new ArrayList<>();

    public ReadFile(){
    }

    public ArrayList<byte[]> readInFile(String filename) throws IOException {
        int eofFlag = 0;
        String fileLocation = filePath + filename;
        byte[] buffer = new byte[1024];
        FileInputStream fis = new FileInputStream(fileLocation);

        try{
            while ((eofFlag = fis.read(buffer)) != -1){
                fileByteStorage.add(buffer);
            }
        }catch(FileNotFoundException e){
            System.out.println("Unable to locate file: " + filename + " at " + fileLocation);
        }catch(IOException e){
            System.out.println("Error reading file: " + filename);
        }
        for(byte[] bitten : fileByteStorage){
            System.out.print(new String(bitten));
        }

        return fileByteStorage;
    }
}
