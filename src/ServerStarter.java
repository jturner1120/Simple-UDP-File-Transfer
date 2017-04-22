import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by jefft on 4/15/2017.
 */
public class ServerStarter {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        int port = 1973;
        try {
            InetAddress IpAddy = InetAddress.getByName("localhost");
            Server myServer = new Server(port, IpAddy);
            myServer.setReliabilityMode(Integer.parseInt(args[0]));
            myServer.startServer();
        } catch (UnknownHostException e) {
            System.out.println("Something terrible happened (Unknown Host) when I tried to initialize my IPAddy.");
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

}
