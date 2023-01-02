import java.io.IOException;
import java.net.*;

/**
 * Implementation of a Client to the DNS server. This class is used to make queries on the
 * DNS network.
 * 
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class Client {
    /**
     * Method used to run the client
     * @param args arguments passed via the command line
     * @throws IOException
     */
    public static void main (String[] args) throws IOException {

        if (args.length < 3)
        {
            System.out.println("Usage: " + args[0] + " addressOfServer[:port] NameToLookup TypeOfEntry [R]\nArguments with [] are optional.");
            return;
        }

        boolean recursive = false;
        if (args.length == 4 && args[3].equals("R")) recursive = true;

        MyAppProto query = new MyAppProto("Q" + ((recursive) ?"+R" :""), args[1], args[2]);

        String address = args[0];
        int port = 53;
        if (address.contains(":"))
        {
            String[] serverString = args[0].split(":");
            address = serverString[0];
            port = Integer.parseInt(serverString[1]);
        }

        DatagramSocket s = UDPCommunication.sendUDP(query, address, port);

        MyAppProto ans = UDPCommunication.receiveUDP(s);

        System.out.println(ans.prettyString());

        s.close();
    }
}
