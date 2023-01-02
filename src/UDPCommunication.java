import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Implementation of the {@code UDPCommunication} class, used to send and receive packets via UDP 
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class UDPCommunication 
{
    /**
     * Method used to send a packet to a specific address and port via UDP
     * @param pdu the packet to send
     * @param address the address to send the packet to
     * @param port the port the address is listening on
     * @return the {@link DatagramSocket} the packet was sent from
     * @throws IOException
     */
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

    /**
     * Method used to receive an address from a {@link DatagramSocket} 
     * @param s {@link DatagramSocket} to receive the packet from
     * @return the packet reveiced via UDP
     * @throws IOException
     */
    public static MyAppProto receiveUDP(DatagramSocket s) throws IOException
    {
        byte[] buf = new byte[1024];
        DatagramPacket receive = new DatagramPacket(buf, buf.length);
        s.receive(receive);

        MyAppProto pdu = new MyAppProto(receive.getData());
        
        return pdu;
    }    
}
