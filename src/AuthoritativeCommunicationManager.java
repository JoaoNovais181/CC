import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@code ServerWorker} class, used when a Server receives a query, in order to produce a response
 * to it. This class is used in a multi-threaded context. 
 *
 * One thing to note is that this is only used to treat queries on Servers that are not DNS Cache Only Servers (SR)
 *
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
class ServerWorker implements Runnable
{
    /**
     * {@link DatagramSocket} used to send the message back to whoever sent it
     */
    private DatagramSocket serverSocket;
    /**
     * Parameter corresponding to the {@link DatagramPacket} received by the server
     */
    private DatagramPacket receive; 
    /**
     * Instance of {@link CCLogger} used to log every event that needs logging
     */
    private CCLogger logger;   
    /**
     * Instance of {@link Cache} that corresponds to the cache of the server it is serving, used to get the values to answer the
     * queries and also store the information about the query in the cache
     */
    private Cache cache;
    /**
     * Name of the domain on which the server it servers is located.
     */
    private String domain;

    /**
     * Constructs a new instance of the {@code ServerWorker} class
     * @param serverSocket socket of the server
     * @param receive received UDP packet
     * @param logger loger
     * @param cache server cache
     * @param domain name of the domain on which the server it servers is located
     */
    public ServerWorker(DatagramSocket serverSocket, DatagramPacket receive, CCLogger logger, Cache cache, String domain)
    {
        this.serverSocket = serverSocket;
        this.receive = receive;
        this.logger = logger;
        this.cache = cache;
        this.domain = domain;
    }

    /**
     * Method used to generate an answer to a query by getting the values it wants from the cache
     * @param msg {@link MyAppProto} object that represents the query received by the server
     * @param successfullDecode {@code boolean} that indicates whether the decode was successful or not
     * @return {@link MyAppProto} object that represents the answer to the query
     */
    public MyAppProto generateAnswer(MyAppProto msg, boolean successfullDecode)
    {
        
        List<CacheEntry> responseValues = this.cache.get(msg.getName(), msg.getTypeOfValue());
        if (!msg.getTypeOfValue().equals("CNAME") && 
            responseValues.size() > 0 && 
            !responseValues.get(0).getName().equals(msg.getName()))
        {
            responseValues.clear();
            System.out.println("CONAAA");
        }
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

        int responseCode = 0;
        if (responseValues.size()==0 && !authoritativeValuesDomain.equals(this.domain)) responseCode = 1;
        else if (responseValues.size() == 0) responseCode = 2;
        if (!successfullDecode) responseCode = 3;
        
        MyAppProto answer = new MyAppProto( ""+(Integer.parseInt(msg.getMsgID())+1),"A", responseCode, responseValues.size(), authoritativeValues.size(), extraValues.size(), msg.getName(), msg.getTypeOfValue());
        
        for (CacheEntry ce : responseValues)
        answer.PutValue(ce.dbString());
        
        for (CacheEntry ce : authoritativeValues)
        answer.PutAuthority(ce.dbString());
        
        for (CacheEntry ce : extraValues)
        answer.PutExtraValue(ce.dbString());
        
        
        return answer;
    }

    /**
     * Method needed to execute the thread on which the ServerWorker is going to be ran. 
     */
    @Override
    public void run()
    {
        System.out.println(this.domain);
        InetAddress clientAddress = receive.getAddress();
        int clientPort = receive.getPort();
        boolean successfullDecode = true;
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

        String flags = msg.getFlags();
        
        List<CacheEntry> cachedQuery = this.cache.get(msg.getName() + " " + msg.getTypeOfValue(), "QUERY");
        MyAppProto answer = null;
        if (cachedQuery.size() != 0)
        {
            answer = new MyAppProto(cachedQuery.get(0).getValue().getBytes()); 
            answer.setMsgID(""+(Integer.parseInt(answer.getMsgID())+1));
        }
        else
        {
            boolean recursive = false;
            if (flags.endsWith("+R"))
                recursive = true;
    
            answer = this.generateAnswer(msg, successfullDecode);
    
            if (recursive)
            {
                if (answer.getResponseCode() == 1)
                {
                    boolean success = false;
                    List<String> extraValues = answer.getExtraValues();
                    int indx  = 0;
                    while(!success && indx < extraValues.size())
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
                            this.logger.log(new LogEntry("QE", tokenizedEntry[2], msg.toString()));
                            answer = UDPCommunication.receiveUDP(s);
                            this.logger.log(new LogEntry("RR", tokenizedEntry[2], answer.toString()));
                        }
                        catch (Exception e)
                        {
                            success = false;
                        }
                    }
                }
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

/**
 * Class that extends the {@link CommunicationManager} abstract class. used to manage the communication of a server that
 * isn't a DNS Cache Only Server.
 * 
 * <p> It receives messages via UDP communication and treats them with sub-Threads </p>
 *
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class AuthoritativeCommunicationManager extends CommunicationManager
{
    /**
     * Constructs a new authoritative communication manager
     * @param logger logger of the server
     * @param cache the Server it serves's cache
     * @param domain name of the server it serves
     * @param port port on which the server is listening
     * @param timeout timeout used on querys
     */
    public AuthoritativeCommunicationManager(CCLogger logger, Cache cache, String domain, int port, int timeout)
    {
        super(logger, cache, domain, port, timeout);
    }

    /**
     * Method needed to execute the thread on which the AuthoritativeCommunicationManager is going to be ran. 
     */
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
                serverSocket.receive(receive) ;
                Thread t = new Thread(new ServerWorker(serverSocket, receive, this.logger, this.cache, this.domain));
                t.run();
            }
            
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
}
