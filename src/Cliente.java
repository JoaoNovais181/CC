import java.io.IOException;
import java.net.*;

public class Cliente {

    public static void main (String[] args) throws IOException {
        InetAddress serverAdress = InetAddress.getByName("127.0.0.1");
        int portServer = 5555;
        String pdu = new MyAppProto("1", "s", "5 5").toString();
        DatagramPacket req = new DatagramPacket(pdu.getBytes(), pdu.getBytes().length, serverAdress, portServer);
        DatagramSocket s = new DatagramSocket();
        s.send(req);
    }
}
