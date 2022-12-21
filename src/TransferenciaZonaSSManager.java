import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TransferenciaZonaSSWorker implements Runnable
{
    private String SP;
    private Cache cache;
    private Map<String,String> macros;
    private int port;
    private CCLogger logger;
    private String domain;
    private int SOARETRY;

    public TransferenciaZonaSSWorker(String SP, Cache cache, Map<String,String> macros, int port, CCLogger logger, String domain, int SOARETRY) 
    {
        this.SP = SP;
        this.cache = cache;
        this.macros = macros;
        this.port = port;
        this.logger = logger;
        this.domain = domain;
        this.SOARETRY = SOARETRY;
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
                Map<String, CacheEntry> alias = new HashMap<>();
                
                int receivedLines = 0;
                while ((response = in.readLine()) != null) 
                {
                    totalLength += response.length();
    
                    String[] tokens = response.split(" ");
    
                    for (int i=0 ; i<tokens.length ; i++)
                    {
                        synchronized (macros)
                        {
                            for (String macro : this.macros.keySet())
                                if (tokens[i].contains(macro)) tokens[i] = tokens[i].replace(macro, this.macros.get(macro));
                        }
                    }
    
                    if (tokens[1].equals("DEFAULT"))
                    {
                        if (tokens.length != 3)
                            this.ThrowException(new InvalidDatabaseException("Macro has too many arguments"));
                        
                        synchronized (macros)
                        {
                            this.macros.put(tokens[0], tokens[2]);
                        }
                        this.cache.put(new CacheEntry(tokens[0], tokens[1], tokens[2], "SP"));
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));
                    }
                    else
                    {
                        synchronized (macros)
                        {
                            if (!tokens[0].endsWith(".")) tokens[0] = tokens[0] + "." + this.macros.get("@");
                        }
                        
                        if (tokens[1].equals("A"))
                        {
            
                            if (tokens.length < 4)
                                this.ThrowException(new InvalidDatabaseException("A entry should have 4/5 arguments"));
                            
                            if (tokens.length == 4)
                                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP"));
                            else 
                                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "SP"));
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));
                        }
                        else if (tokens[1].equals("SOAADMIN"))
                        {
                            if (tokens.length != 4)
                                this.ThrowException(new InvalidDatabaseException("SOASP field is not correct (too many arguments)"));
                                
                            this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP"));
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));
                        }
                        else if (tokens[1].equals("SOASERIAL"))
                        {
                            if (tokens.length != 4)
                                this.ThrowException(new InvalidDatabaseException("SOASERIAL field is not correct"));
            
                            this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP"));
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));
                        }
                        else if (tokens[1].equals("SOAEXPIRE"))
                        {
                            if (tokens.length != 4)
                                this.ThrowException(new InvalidDatabaseException("SOAEXPIRE field is not correct (too many arguments)"));
                            
                            this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP"));
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));
                        }
                        else if (tokens[1].equals("SOAREFRESH"))
                        {
                            if (tokens.length != 4)
                                this.ThrowException(new InvalidDatabaseException("SOAREFRESH field is not correct"));
            
                            this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP"));
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));
                        }
                        else if (tokens[1].equals("SOARETRY"))
                        {
                            if (tokens.length != 4)
                                this.ThrowException(new InvalidDatabaseException("SOARETRY field is not correct"));
            
                            this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP"));
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));
                        }
                        else
                        {
    
                            synchronized (macros)
                            {
                                if (!tokens[2].endsWith(".")) tokens[2] = tokens[2] + "." + this.macros.get("@");
                            }
                            
                            if (tokens[1].equals("SOASP"))
                            {
                                if (tokens.length != 4)
                                    this.ThrowException(new InvalidDatabaseException("SOASP field is not correct (too many arguments)"));
                                
                                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP"));
                                this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));
                            }
                            else if (tokens[1].equals("NS"))
                            {
                                if (tokens.length < 3)
                                    this.ThrowException(new InvalidDatabaseException("NS entry should have 3/4/5 arguments"));
                                
                                if (tokens.length == 3)
                                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], "SP"));
                                else if (tokens.length == 4)
                                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP"));
                                else 
                                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "SP"));
                                this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));
                            }
                            else if (tokens[1].equals("MX"))
                            {
                                if (tokens.length < 4)
                                    this.ThrowException(new InvalidDatabaseException("MX entry should have 4/5 arguments"));
                                
                                if (tokens.length == 4)
                                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP"));
                                else 
                                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "SP"));
                                this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));    
                            }
                            else if (tokens[1].equals("CNAME"))
                            {
                                if (tokens.length != 4)
                                    this.ThrowException(new InvalidDatabaseException("CNAME entry should have 4 arguments"));
                        
                                if (alias.containsKey(tokens[2]))
                                    this.ThrowException(new InvalidDatabaseException("A canonic name should not point to other canonic name"));
                                
                                if (alias.containsKey(tokens[0]))
                                    this.ThrowException(new InvalidDatabaseException("The same canonic name should not be given to two different parameters"));
                        
                                
                                CacheEntry ce = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "SP");
                                alias.put(tokens[0], ce);
                                this.cache.put(ce);
                                this.logger.log(new LogEntry("EV", "localhost", "DataBase entry from Zone Transfer added to cache. Entry: " + response));    
                            }
                
                            else 
                                this.ThrowException(new InvalidDatabaseException("Type " + tokens[1] + " is invalid"));
                        }
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class TransferenciaZonaSSManager implements Runnable {

    private String SP;
    private Cache cache;
    private Map<String, String> macros;
    private int port;
    private CCLogger logger;
    private String domain;
    private int SOAREFRESH, SOARETRY;
    private boolean zt;

    public TransferenciaZonaSSManager(String SP, Cache cache, Map<String,String> macros, int port, CCLogger logger, String domain) 
    {
        this.SP = SP;
        this.cache = cache;
        this.macros = macros;
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
        
            this.zt = true;
    }

    @Override
    public void run() {
        
        Object lock = new Object();

        while (true)
        {
            if (zt)
            {
                Thread thread = new Thread(new TransferenciaZonaSSWorker(this.SP, this.cache, this.macros, this.port, this.logger, this.domain, this.SOARETRY));
                thread.start();
            }
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
    
}