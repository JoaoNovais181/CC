import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

class ResolveServerWorker implements Runnable
{
    private DatagramSocket serverSocket;
    private DatagramPacket receive; 
    private CCLogger logger;   
    private Cache cache;
    private List<String> STlist;
    private Map<String,String> DD;

    public ResolveServerWorker(DatagramSocket serverSocket, DatagramPacket receive, CCLogger logger, Cache cache, List<String> STlist, Map<String,String> DD)
    {
        this.serverSocket = serverSocket;
        this.receive = receive;
        this.logger = logger;
        this.cache = cache;
        this.STlist = STlist;
        this.DD = DD;
    }

    public List<Object> sendTo(MyAppProto pdu, List<String> Aentries)
    {
        String sentAddress = null;
        MyAppProto answer = null;
        int indx = 0;

        Aentries = Aentries.stream().map((entry) -> { return entry.split(" ")[2];}).collect(Collectors.toList());

        boolean success = false;
        do
        {
            success = true;
            String address = Aentries.get(indx++);
            sentAddress = address;
            int port = 53;
            if (address.contains(":"))
            {
                String[] parts = address.split(":");
                address = parts[0];
                port = Integer.parseInt(parts[1]);
            }

            try
            {
                DatagramSocket s = UDPCommunication.sendUDP(pdu, address, port);
                this.logger.log(new LogEntry("QE", sentAddress, pdu.toString()));
                answer = UDPCommunication.receiveUDP(s);
                this.logger.log(new LogEntry("RR", sentAddress, answer.toString()));
            }
            catch (IOException e)
            {
                success = false;
            }
        } while(!success && indx < Aentries.size());
        
        return List.of(sentAddress, answer);
    }

    public MyAppProto treatQuery(MyAppProto msg, boolean recursive, boolean successfullDecode)
    {
        if (msg.getFlags().contains("+R"))
        recursive = true;

        MyAppProto answer = null;
        List<CacheEntry> cachedQuery = this.cache.get(msg.getName() + " " + msg.getTypeOfValue(), "Query");
        String sentAddress = null;
        if (cachedQuery.size() == 1)
            answer = new MyAppProto(cachedQuery.get(0).toString().getBytes());
        else
        {
            if (this.DD != null)
            {
                String DD = null;
                for (String key : this.DD.keySet())
                    if (msg.getName().contains(key))
                        DD = this.DD.get(key);
                if (DD != null)
                {
                    String address = DD;
                    sentAddress = address;
                    int port = 53;
                    if (DD.contains(":"))
                    {
                        String[] partes = DD.split(":");
                        address = partes[0];
                        port = Integer.parseInt(partes[1]);
                    }
                    try
                    {
                        DatagramSocket s = UDPCommunication.sendUDP(msg, address, port);
                        answer = UDPCommunication.receiveUDP(s);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            if (answer == null)
            {
                int indx = 0;
                boolean success = false;
                do
                {
                    success = true;
                    String address = this.STlist.get(indx++);
                    sentAddress = address;
                    int port = 53;
                    try
                    {
                        DatagramSocket s = UDPCommunication.sendUDP(msg, address, port);
                        answer = UDPCommunication.receiveUDP(s);
                    }
                    catch (IOException e)
                    {
                        success = false;
                    }
                } while(!success && indx < this.STlist.size());
            }

        }
        
        if (answer.getResponseCode() == 1 && !recursive)
        {
            String nextAddress = null;
            List<String> authoritiesNames = answer.getAuthoritiesValues();
            authoritiesNames.stream().map((entry) -> { return entry.split(" ")[2];}).collect(Collectors.toList());
            List<String> extraValues = answer.getExtraValues(), sendto = new ArrayList<>();
            for (String extraValue : extraValues)
            {
                String[] parts = extraValue.split(" ");
                if (authoritiesNames.contains(parts[0]))
                    nextAddress = parts[2];
                else
                   sendto.add(extraValue);
            }
            
            while (sentAddress != nextAddress)
            {
                List<Object> rv = this.sendTo(msg, sendto);
                sentAddress = (String)rv.get(0);
                answer = (MyAppProto)rv.get(1);
                
                if (answer.getResponseCode() == 0)
                    break;
                if (answer.getResponseCode() == 1)
                {
                    authoritiesNames = answer.getAuthoritiesValues();
                    authoritiesNames.stream().map((entry) -> { return entry.split(" ")[2];}).collect(Collectors.toList());
                    extraValues = answer.getExtraValues();
                    sendto.clear();
                    for (String extraValue : extraValues)
                    {
                        String[] parts = extraValue.split(" ");
                        if (authoritiesNames.contains(parts[0]))
                            nextAddress = parts[2];
                        else
                            sendto.add(extraValue);
                    }
                }
                else
                    break;
                }

        }
        else if (!successfullDecode)
            answer.setResponseCode(3);
        
            return answer;
    }

    @Override
    public void run()
    {
        InetAddress clientAddress = receive.getAddress();
        int clientPort = receive.getPort();
        boolean successfullDecode = true;
        boolean recursive = false;
        MyAppProto msg = null;
        try
        {
            msg = new MyAppProto(receive.getData());
            this.logger.log(new LogEntry("QR", clientAddress.toString(), msg.toString()));
        }
        catch (Exception e)
        {
            successfullDecode = false;
        }

        List<CacheEntry> cachedQuery = this.cache.get(msg.getName() + " " + msg.getTypeOfValue(), "QUERY");
        
        MyAppProto answer = null;
        if (cachedQuery.size() != 0 && cachedQuery.get(0).getName().equals(msg.getName() + " " + msg.getTypeOfValue()))
        {
            answer = new MyAppProto(cachedQuery.get(0).getValue().getBytes()); 
            answer.setMsgID(""+(Integer.parseInt(answer.getMsgID())+1));
        }
        else
            answer = this.treatQuery(msg, recursive, successfullDecode);

        String pdu = answer.toString();
        DatagramPacket send = new DatagramPacket(pdu.getBytes(), pdu.getBytes().length, clientAddress, clientPort);
        try {
            this.serverSocket.send(send);
            this.logger.log(new LogEntry("RE", clientAddress.toString(), answer.toString()));    
            this.cache.put(new CacheEntry(answer.getName() + " " + answer.getTypeOfValue(), "QUERY", answer.toString(), "OTHERS"));
        } catch (IOException | InvalidCacheEntryException e) {
            e.printStackTrace();
        }
    }
}

public class ResolveCommunicationManager extends CommunicationManager 
{
    private List<String> STlist;
    private Map<String,String> DD;

    public ResolveCommunicationManager(CCLogger logger, Cache cache, String domain, int port, int timeout, List<String> STlist, Map<String,String> DD)
    {
        super(logger, cache, domain, port, timeout);
        this.STlist = STlist;
        this.DD = DD;
    }


    @Override
    public void run() {
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
                Thread t = new Thread(new ResolveServerWorker(serverSocket, receive, this.logger, this.cache, this.STlist, this.DD));
                t.run();
            }
            
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
