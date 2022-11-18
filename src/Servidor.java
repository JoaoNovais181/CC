import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Servidor {

    public static void main (String[] args) throws IOException {
        boolean status = true;
        DatagramSocket serverSocket = new DatagramSocket(5555);

        while (status)
        {
            byte [] buf = new byte[256];
            DatagramPacket receive = new DatagramPacket(buf,buf.length);
            serverSocket.receive(receive) ;   // extrair ip cliente, Port Client, Payload UDP
            InetAddress clientAddress = receive.getAddress();
            int clientPort = receive.getPort();
            // MyAppProtoOld msg = new MyAppProtoOld(receive.getData());   // o getdata da um array de bytes - bytes []
            MyAppProto msg = new My

            int result;
            boolean op = msg.getFlags().toUpperCase().equals("S"); // se for True temos Adicao senao temos Multiplicacao

            if (op) result = 0;
            else result = 1;

            for (String s : msg.getNumbers().split(" "))
            {
                int num = Integer.parseInt(s.trim());

                if (op)
                    result += num;
                else
                    result *= num;
            }

            int msgID = Integer.parseInt(msg.getMsgID()) + 1;

            String pdu = new MyAppProtoOld(("" + msgID), "A", (""+result)).toString();

            DatagramPacket send = new DatagramPacket(pdu.getBytes(), pdu.getBytes().length, clientAddress, clientPort);

            serverSocket.send(send);
        }

        serverSocket.close();
    }
}
