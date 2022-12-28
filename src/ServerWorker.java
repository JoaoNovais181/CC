import java.io.IOException;
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

    public MyAppProto generateAnswer(MyAppProto msg, boolean successfullDecode)
    {
        List<CacheEntry> responseValues = this.cache.get(msg.getName(), msg.getTypeOfValue());
        List<CacheEntry> authoritativeValues = this.cache.get(msg.getName(), "NS");
        if (authoritativeValues.size() == 0)
            authoritativeValues = this.cache.get(this.domain, "NS");
        List<CacheEntry> extraValues = new ArrayList<>();

        for (CacheEntry ce : responseValues)
        {
            int index;
            if ((index = this.cache.searchValid(ce.getValue(),  "A", 1)) != -1)
                ce =  this.cache.get(index);
            if (ce.getType().equals("A") && !extraValues.contains(ce)) extraValues.add(ce);
        }

        String authoritativeValuesDomain = null;
        for (CacheEntry ce : authoritativeValues)
        {
            int index;
            String dom = ce.getName();
            authoritativeValuesDomain = dom;
            if ((index = this.cache.searchValid(ce.getValue(),  "A", 1)) != -1)
                ce =  this.cache.get(index);
            if (ce.getType().equals("A") && !extraValues.contains(ce)) extraValues.add(ce);
        }

        System.out.println(this.domain + " " + authoritativeValuesDomain);
        int responseCode = 0;
        if (responseValues.size()==0 && !authoritativeValuesDomain.equals(this.domain)) responseCode = 1;
        else if (responseValues.size() == 0) responseCode = 2;
        if (!successfullDecode) responseCode = 3;
        
        MyAppProto answer = new MyAppProto( msg.getMsgID(),"A", responseCode, responseValues.size(), authoritativeValues.size(), extraValues.size(), msg.getName(), msg.getTypeOfValue());
        
        for (CacheEntry ce : responseValues)
        answer.PutValue(ce.dbString());
        
        for (CacheEntry ce : authoritativeValues)
        answer.PutAuthority(ce.dbString());
        
        for (CacheEntry ce : extraValues)
        answer.PutExtraValue(ce.dbString());
        
        
        return answer;
    }

    public void run()
    {
        InetAddress clientAddress = receive.getAddress();
        int clientPort = receive.getPort();
        boolean successfullDecode = true;
        MyAppProto msg = null;
        try
        {
            msg = new MyAppProto(receive.getData());
        }
        catch (Exception e)
        {
            successfullDecode = false;
        }   

        String flags = msg.getFlags();

        boolean recursive = false;
        if (flags.endsWith("+R"))
            recursive = true;

        try {
            this.logger.log(new LogEntry("QR", clientAddress.toString(), msg.toString()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        MyAppProto answer = this.generateAnswer(msg, successfullDecode);

        if (recursive)
        {
            if (answer.getResponseCode() == 1)
            {
                boolean success = false;
                List<String> extraValues = answer.getExtraValues();
                int indx  = 0;
                do
                {
                    success = true;
                    String entry = extraValues.get(indx++);
                    String[] tokenizedEntry = entry.split(" ");
                    String address = tokenizedEntry[2];
                    int port = 53;
                    if (address.contains(":"))
                    {
                        String[] tokenizedAdress = address.split(":");
                        address = tokenizedAdress[0];
                        port = Integer.parseInt(tokenizedAdress[1]);
                    }
                    try
                    {
                        DatagramSocket s = UDPCommunication.sendUDP(msg, address, port);
                        answer = UDPCommunication.receiveUDP(s);
                    }
                    catch (Exception e)
                    {
                        success = false;
                    }
                } while(!success && indx < extraValues.size());
            }
        }

        String pdu = answer.toString();
        DatagramPacket send = new DatagramPacket(pdu.getBytes(), pdu.getBytes().length, clientAddress, clientPort);
        try {
            this.serverSocket.send(send);
            this.logger.log(new LogEntry("RE", clientAddress.toString(), answer.toString()));    
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    }
}
