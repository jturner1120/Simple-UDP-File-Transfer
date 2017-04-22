import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by jefft on 4/15/2017.
 */
public class ClientStarter {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        int port = 2009;
        try {
            InetAddress IpAddy = InetAddress.getByName("localhost");
            Client myClient = new Client(port, IpAddy);
            myClient.setArgument(args[0]);
            myClient.startClient();
        } catch (UnknownHostException e) {
            System.out.println("Something terrible happened (Unknown Host) when I tried to initialize my IPAddy.");
        } catch (SocketException e) {
            System.out.println("Something terrible happened (Socket) when I tried to initialize my Socket.");
            e.printStackTrace();
        }

    }

}
