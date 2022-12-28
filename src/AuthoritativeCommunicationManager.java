import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AuthoritativeCommunicationManager extends CommunicationManager
{
    public AuthoritativeCommunicationManager(CCLogger logger, Cache cache, String domain, int port)
    {
        super(logger, cache, domain, port);
    }

    @Override
    public void run ()  
    {
        try 
        {
            boolean status = true;
            DatagramSocket serverSocket;
            serverSocket = new DatagramSocket(this.port, InetAddress.getByName("0.0.0.0"));
            
            while (status)
            {
                byte [] buf = new byte[256];
                DatagramPacket receive = new DatagramPacket(buf,buf.length);
                serverSocket.receive(receive) ;   // extrair ip cliente, Port Client, Payload UDP
                Thread t = new Thread(new ServerWorker(serverSocket, receive, this.logger, this.cache, this.domain));
                t.run();
            }
            
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
}
