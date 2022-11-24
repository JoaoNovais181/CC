import java.io.IOException;
import java.net.*;
import java.util.Random;

public class Cliente {

    public static void main (String[] args) throws IOException {

        if (args.length < 3)
            return;

        boolean recursive = false;
        if (args.length == 4 && args[3].equals("R")) recursive = true;

        String[] serverString = args[0].split(":");
        InetAddress serverAdress = InetAddress.getByName(serverString[0]);
        int portServer = Integer.parseInt(serverString[1]);
		
		Random r = new Random(ProcessHandle.current().pid());
        String pdu = new MyAppProto(""+r.nextInt(65535), "Q" + ((recursive) ?"+R" :""), args[1], args[2]).toString();
        // String pdu = new MyAppProtoOld("1", "s", "5 5").toString();
        DatagramPacket req = new DatagramPacket(pdu.getBytes(), pdu.getBytes().length, serverAdress, portServer);
        DatagramSocket s = new DatagramSocket();
        s.send(req);

        byte[] buf = new byte[1024];
        DatagramPacket receive = new DatagramPacket(buf, buf.length);
        s.receive(receive);

        MyAppProto ans = new MyAppProto(receive.getData());
        System.out.println(ans.prettyString());

        s.close();
    }
}
