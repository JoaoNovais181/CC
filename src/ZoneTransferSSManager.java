import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementation of the Worker of the {@link ZoneTransferSSManager}, used to receive the Database entries from de Primary Server
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
class ZoneTransferSSWorker implements Runnable
{
    /**
     * Address of the Primary Server
     */
    private String SP;
    /**
     * The secondary Server's {@link Cache} 
     */
    private Cache cache;
    /**
     * The port the secondary Server is listening on
     */
    private int port;
    /**
     * The logger that the server uses to log it's operations
     */
    private CCLogger logger;
    /**
     * The domain on which the server is located
     */
    private String domain;
    /**
     * The value of the SOARETRY field
     */
    private int SOARETRY;
    /**
     * The Worker's Manager
     */
    private ZoneTransferSSManager manager;

    /**
     * Instantiates a new ZoneTransferSSWorker with the specified arguments
     * @param SP Address of the Primary Server
     * @param cache The secondary Server's {@link Cache}
     * @param port The port the secondary Server is listening on
     * @param logger The logger that the server uses to log it's operations
     * @param domain The domain on which the server is located
     * @param SOARETRY The value of the SOARETRY field
     * @param manager The Worker's Manager
     */
    public ZoneTransferSSWorker(String SP, Cache cache, int port, CCLogger logger, String domain, int SOARETRY, ZoneTransferSSManager manager) 
    {
        this.SP = SP;
        this.cache = cache;
        this.port = port;
        this.logger = logger;
        this.domain = domain;
        this.SOARETRY = SOARETRY;
        this.manager = manager;
    }

    /**
     * Method used to throw an exception
     * @param e exception to be thrown
     * @throws Exception
     */
    public void ThrowException(Exception e) throws Exception
    {
        this.logger.log(new LogEntry("FL", "localhost", e.getMessage()));
        e.printStackTrace();
        throw e;
    }

    /**
     * Method used to run the Worker
     */
    public void run()
    {
        try {
            int porta = this.port;
            String spIP = this.SP;
            if (this.SP.contains(":"))
            {
                spIP = this.SP.substring(0, this.SP.indexOf(":"));
                porta = Integer.parseInt(this.SP.substring(this.SP.indexOf(":") + 1, this.SP.length()));
            }

            Socket socket = new Socket(spIP, porta);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            
            out.println("domain: \"" + this.domain + "\"");
            out.flush();
            LocalDateTime start = LocalDateTime.now();
            
            this.cache.removeEntries("SP");

            String response = in.readLine();
            if (!response.startsWith("entries: ")) 
            {
                this.logger.log(new LogEntry("EZ", spIP, "SS"));
                socket.close(); 
                return; 
            }
            int entries = Integer.parseInt(response.split(": ")[1]);

            out.println("ok: " + entries);
            out.flush();

            Object lock = new Object();
            boolean success = false;
            int totalLength = 0;

            do
            {
                int receivedLines = 0;
                while ((response = in.readLine()) != null) 
                {
                    totalLength += response.length();
    
                    String[] tokens = response.split(";");

                    CacheEntry ce = new CacheEntry(tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "SP");
                    this.cache.put(ce);
                    this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + ce.dbString()));

                    receivedLines++;   
                }
                
                if (receivedLines==entries)
                    success = true;
                else
                {
                    synchronized (lock)
                    {
                        try
                        {
                            lock.wait(this.SOARETRY);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    this.run();
                    socket.shutdownOutput();
                    socket.shutdownInput();
                    socket.close();
                    return;
                }

            } while(!success);

            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
            this.logger.log(new LogEntry("ZT", spIP, "SS, totalBytes = " + totalLength + ", duration = " + ChronoUnit.MILLIS.between(start, LocalDateTime.now()) + "ms"));
            this.manager.renewTimeouts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * Implementation of {@code ZoneTransferSSManager}, that extends the {@link ZoneTransferManager} abstract class, used 
 * to manage the zone transfer on a Secondary Server
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class ZoneTransferSSManager extends ZoneTransferManager {
/**
     * Address of the Primary Server
     */
    private String SP;
    /**
     * The secondary Server's {@link Cache} 
     */
    private Cache cache;
    /**
     * The port the secondary Server is listening on
     */
    private int port;
    /**
     * The logger that the server uses to log it's operations
     */
    private CCLogger logger;
    /**
     * The domain on which the server is located
     */
    private String domain;
    /**
     * The value of the SOAREFRESH, SOARETRY and SOASERIAL field
     */
    private int SOAREFRESH, SOARETRY, SOASERIAL;
    /**
     * boolean representing whether the server is running
     */
    private boolean running;

    /**
     * Instantiates a new ZoneTransferSSManager using the specified arguments
     * @param SP Address of the Primary Server
     * @param cache The secondary Server's {@link Cache}
     * @param port The port the secondary Server is listening on
     * @param logger The logger that the server uses to log it's operations
     * @param domain The domain on which the server is located
     */
    public ZoneTransferSSManager(String SP, Cache cache, int port, CCLogger logger, String domain) 
    {
        this.SP = SP;
        this.cache = cache;
        this.port = port;
        this.logger = logger;
        this.domain = domain;
        List<CacheEntry> l = this.cache.get(this.domain, "SOAREFRESH");
        if (l.size()==0)
            this.SOAREFRESH = 1000;
        else 
            this.SOAREFRESH = Integer.parseInt(l.get(0).getValue());
        l = this.cache.get(this.domain, "SOARETRY");
        
        if (l.size()==0)
            this.SOARETRY = 300;
        else 
            this.SOARETRY = Integer.parseInt(l.get(0).getValue());
        this.SOASERIAL = -1;
        this.running = true;
    }

    /**
     * Method used to renew the value of the timeouts
     */
    public synchronized void renewTimeouts()
    {
        List<CacheEntry> l = this.cache.get(this.domain, "SOAREFRESH");
        
        if (l.size()==0)
            this.SOAREFRESH = 1000;
        else 
            this.SOAREFRESH = Integer.parseInt(l.get(0).getValue());

        l = this.cache.get(this.domain, "SOARETRY");
        
        if (l.size()==0)
            this.SOARETRY = 300;
        else 
            this.SOARETRY = Integer.parseInt(l.get(0).getValue());
    }

    /**
     * Method used to see wheter the Server needs a zone transfer or not
     * @return boolean indicating whether the Server needs a zone transfer or not
     */
    public synchronized boolean needsZT()
    {
        this.renewTimeouts();
        List<CacheEntry> l = this.cache.get(this.domain, "SOASERIAL");

        if (l.size() == 0)
            return true;
        
        this.SOASERIAL = Integer.parseInt(l.get(0).getValue());

        MyAppProto pdu = new MyAppProto("Q", this.domain, "SOASERIAL");
        int porta = this.port;
        String spIP = this.SP;
        if (this.SP.contains(":"))
        {
            spIP = this.SP.substring(0, this.SP.indexOf(":"));
            porta = Integer.parseInt(this.SP.substring(this.SP.indexOf(":") + 1, this.SP.length()));
        }
        
        try
        {
            DatagramSocket s = UDPCommunication.sendUDP(pdu, spIP, porta);
            this.logger.log(new LogEntry("QE", spIP + ":" + porta, pdu.toString()));
            MyAppProto ans = UDPCommunication.receiveUDP(s);
            this.logger.log(new LogEntry("RR", spIP + ":" + porta, ans.toString()));
            List<String> rv = ans.getResponseValues();
    
            if (rv.size()==0)
                return true;
            
            String[] parsedString = rv.get(0).split(" ");
            int soaSerial = Integer.parseInt(parsedString[2]);
    
            return this.SOASERIAL != soaSerial;
        }
        catch (Exception e)
        {
            return true;
        }
    }

    @Override
    /**
     * Method used to run the Manager
     */
    public void run() {
        
        Object lock = new Object();
        boolean zt = this.needsZT();
        while (this.running)
        {
            if (zt)
            {
                Thread thread = new Thread(new ZoneTransferSSWorker(this.SP, this.cache, this.port, this.logger, this.domain, this.SOARETRY, this));
                thread.start();
            }
            
            synchronized (lock)
            {
                try
                {
                    lock.wait(this.SOAREFRESH*1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            zt = this.needsZT();
            System.out.println(this.SOAREFRESH);
        }
    }

	@Override
    /**
     * Method used to turn off the Manager
     */
	public synchronized void turnOff() { this.running = false; }
    
}