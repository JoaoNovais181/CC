import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import MyAppProto;

public class Servidor {

    public static void main (String[] args) throws IOException {
        DatagramSocket serverS = new DatagramSocket(5555);
        byte [] buf = new byte[256];
        DatagramPacket receive = new DatagramPacket(buf,buf.length);
        serverS.receive(receive) ;   // extrair ip cliente, Port Client, Payload UDP
        MyappProto msg = new(receive.getData());   // o getdata da um array de bytes - bytes []
    }
}
