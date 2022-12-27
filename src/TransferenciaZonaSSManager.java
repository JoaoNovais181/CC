import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

class TransferenciaZonaSSWorker implements Runnable
{
    private String SP;
    private Cache cache;
    private int port;
    private CCLogger logger;
    private String domain;
    private int SOARETRY;
    private TransferenciaZonaSSManager manager;

    public TransferenciaZonaSSWorker(String SP, Cache cache, int port, CCLogger logger, String domain, int SOARETRY, TransferenciaZonaSSManager manager) 
    {
        this.SP = SP;
        this.cache = cache;
        this.port = port;
        this.logger = logger;
        this.domain = domain;
        this.SOARETRY = SOARETRY;
        this.manager = manager;
    }

    public void ThrowException(Exception e) throws Exception
    {
        this.logger.log(new LogEntry("FL", "localhost", e.getMessage()));
        e.printStackTrace();
        throw e;
    }

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
                    
                    synchronized(cache)
                    {
                        this.cache.put(new CacheEntry(tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "SP"));
                    }

                    receivedLines++;   
                }
                
                if (receivedLines==entries)
                    success = true;
                else
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

public class TransferenciaZonaSSManager extends TransferenciaZonaManager {

    private String SP;
    private Cache cache;
    private int port;
    private CCLogger logger;
    private String domain;
    private int SOAREFRESH, SOARETRY;
    private boolean running;

    public TransferenciaZonaSSManager(String SP, Cache cache, int port, CCLogger logger, String domain) 
    {
        this.SP = SP;
        this.cache = cache;
        this.port = port;
        this.logger = logger;
        this.domain = domain;
        List<CacheEntry> l = this.cache.get(this.domain, "SOAREFRESH");
        if (l.size()==0)
            this.SOAREFRESH = 10000;
        else 
            this.SOAREFRESH = Integer.parseInt(l.get(0).getValue());
        l = this.cache.get(this.domain, "SOARETRY");
        
        if (l.size()==0)
            this.SOARETRY = 300;
        else 
            this.SOARETRY = Integer.parseInt(l.get(0).getValue());
        this.running = true;
    }

    public synchronized void renewTimeouts()
    {
        List<CacheEntry> l = this.cache.get(this.domain, "SOAREFRESH");
        for (CacheEntry entry : l)
            System.out.println(entry.toString());
        
        if (l.size()==0)
            this.SOAREFRESH = 10000;
        else 
            this.SOAREFRESH = Integer.parseInt(l.get(0).getValue());

        l = this.cache.get(this.domain, "SOARETRY");
        
        if (l.size()==0)
            this.SOARETRY = 300;
        else 
            this.SOARETRY = Integer.parseInt(l.get(0).getValue());
    }

    @Override
    public void run() {
        
        Object lock = new Object();
        boolean zt = true;
        while (this.running)
        {
            Thread thread = new Thread(new TransferenciaZonaSSWorker(this.SP, this.cache, this.port, this.logger, this.domain, this.SOARETRY, this));
            thread.start();
            
            synchronized (lock)
            {
                try
                {
                    lock.wait(this.SOAREFRESH);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            
        }
    }

	@Override
	public synchronized void turnOff() { this.running = false; }
    
}