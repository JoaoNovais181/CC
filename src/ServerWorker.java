import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerWorker implements Runnable
{
    private DatagramSocket serverSocket;
    private DatagramPacket receive; 
    private CCLogger logger;   
    private Cache cache;
    private String domain;

    public ServerWorker(DatagramSocket serverSocket, DatagramPacket receive, CCLogger logger, Cache cache, String domain)
    {
        this.serverSocket = serverSocket;
        this.receive = receive;
        this.logger = logger;
        this.cache = cache;
        this.domain = domain;
    }


    public void run()
    {
        try
        {
            InetAddress clientAddress = receive.getAddress();
            int clientPort = receive.getPort();
            MyAppProto msg = new MyAppProto(receive.getData());
            this.logger.log(new LogEntry("QR", clientAddress.toString(), msg.toString()));
    
            List<CacheEntry> responseValues = this.cache.get(msg.getName(), msg.getTypeOfValue());
            List<CacheEntry> authoritativeValues = this.cache.get(msg.getName(), "NS");
            List<CacheEntry> extraValues = new ArrayList<>();
    
            for (CacheEntry ce : responseValues)
            {
                int index;
                if ((index = this.cache.searchValid(ce.getValue(),  "A", 1)) != -1)
                    ce =  this.cache.get(index);
                if (ce.getType().equals("A") && !extraValues.contains(ce)) extraValues.add(ce);
            }
    
            for (CacheEntry ce : authoritativeValues)
            {
                int index;
                if ((index = this.cache.searchValid(ce.getValue(),  "A", 1)) != -1)
                    ce =  this.cache.get(index);
                if (ce.getType().equals("A") && !extraValues.contains(ce)) extraValues.add(ce);
            }
    
    
            int responseCode = 0;
            if (responseValues.size()==0) responseCode = 1;
            // if (responseValues.size()==0 && this.domain != ) responseCode = 2;
            // Falta ver valor 3
    
            MyAppProto answer = new MyAppProto(msg.getMsgID(), "A", responseCode, responseValues.size(), authoritativeValues.size(), extraValues.size(), msg.getName(), msg.getTypeOfValue());
    
            for (CacheEntry ce : responseValues)
                answer.PutValue(ce.dbString());
    
            for (CacheEntry ce : authoritativeValues)
                answer.PutAuthority(ce.dbString());
            
            for (CacheEntry ce : extraValues)
                answer.PutExtraValue(ce.dbString());
            
            String pdu = answer.toString();
            DatagramPacket send = new DatagramPacket(pdu.getBytes(), pdu.getBytes().length, clientAddress, clientPort);
    
            serverSocket.send(send);
            this.logger.log(new LogEntry("RE", clientAddress.toString(), answer.toString()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
