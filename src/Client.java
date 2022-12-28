import java.io.IOException;
import java.net.*;

public class Client {
    public static void main (String[] args) throws IOException {

        if (args.length < 3)
            return;

        boolean recursive = false;
        if (args.length == 4 && args[3].equals("R")) recursive = true;

        MyAppProto query = new MyAppProto("Q" + ((recursive) ?"+R" :""), args[1], args[2]);
        String[] serverString = args[0].split(":");

        DatagramSocket s = UDPCommunication.sendUDP(query, serverString[0], Integer.parseInt(serverString[1]));

        MyAppProto ans = UDPCommunication.receiveUDP(s);

        System.out.println(ans.prettyString());

        s.close();
    }
}
