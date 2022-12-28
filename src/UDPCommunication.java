import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPCommunication 
{
    public static DatagramSocket sendUDP(MyAppProto pdu, String address, int port) throws IOException
    {
        InetAddress serverAdress = InetAddress.getByName(address);
        int portServer = port;
		
        String pduStr = pdu.toString();
        DatagramPacket req = new DatagramPacket(pduStr.getBytes(), pduStr.getBytes().length, serverAdress, portServer);
        DatagramSocket s = new DatagramSocket();
        s.send(req);

        return s;
    }

    public static MyAppProto receiveUDP(DatagramSocket s) throws IOException
    {
        byte[] buf = new byte[1024];
        DatagramPacket receive = new DatagramPacket(buf, buf.length);
        s.receive(receive);

        MyAppProto pdu = new MyAppProto(receive.getData());
        
        return pdu;
    }    
}
