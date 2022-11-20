import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Servidor {

//    public static void main (String[] args) throws IOException {
//        boolean status = true;
//        DatagramSocket serverSocket = new DatagramSocket(5555);
//
//        while (status)
//        {
//            byte [] buf = new byte[256];
//            DatagramPacket receive = new DatagramPacket(buf,buf.length);
//            serverSocket.receive(receive) ;   // extrair ip cliente, Port Client, Payload UDP
//            InetAddress clientAddress = receive.getAddress();
//            int clientPort = receive.getPort();
//            // MyAppProtoOld msg = new MyAppProtoOld(receive.getData());   // o getdata da um array de bytes - bytes []
//            MyAppProto msg = new MyAppProto(receive.getData());
//
//            List<CacheEntry> responseValues = this.cache.get(msg.getName(), msg.getTypeOfValue());
//            List<CacheEntry> authoritativeValues = this.cache.get(this.domain, "NS");
//            List<CacheEntry> extraValues = new ArrayList<>();
//
//            for (CacheEntry ce : responseValues)
//            {
//                int index = this.cache.searchValid(ce.getName(), "A", 1);
//                if (index != -1) extraValues.add(this.cache.get(index));
//            }
//
//            for (CacheEntry ce : authoritativeValues)
//            {
//                int index = this.cache.searchValid(ce.getName(), "A", 1);
//                if (index != -1) extraValues.add(this.cache.get(index));
//            }
//
//            MyAppProto answer = new MyAppProto(msg.getMsgID(), "A", 0, responseValues.size(), authoritativeValues.size(), extraValues.size(), msg.getName(), msg.getTypeOfValue());
//            String pdu = answer.toString();
//            DatagramPacket send = new DatagramPacket(pdu.getBytes(), pdu.getBytes().length, clientAddress, clientPort);
//
//            serverSocket.send(send);
//        }
//
//        serverSocket.close();
//    }
}
