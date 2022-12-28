import java.io.IOException;
import java.net.*;

public class Client {
    public static void main (String[] args) throws IOException {

        if (args.length < 3)
            return;

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
