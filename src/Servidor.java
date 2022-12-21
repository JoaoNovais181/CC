import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.Socket;

public class Servidor
{
    public enum Tipo {SP, SS, UNDEFINED};
    private Tipo tipo;
    private String domain, configFile, databaseFile, logFile, STfile;
    private Cache cache;
    private Map<String,String> macros;
    private boolean debug;
    private CCLogger logger;
    private List<String> SSlist;
    private String SP;
    private Map<String,List<String>> DDlist;
    private Map<String,List<String>> logFiles;
    private List<String> STs;
    private int timeout, port, NumDBentries;

    public Servidor (int port, int timeout, String configFile, boolean debug) throws IOException, InvalidConfigException, InvalidDatabaseException, InvalidCacheEntryException
    {
        this.tipo = Tipo.UNDEFINED;
        this.port = port;
        this.timeout = timeout;
        this.domain = "";
		this.configFile = configFile;
        this.debug = debug;

        this.SSlist = new ArrayList<>();
        this.SP = null;
        this.DDlist = new HashMap<>();
        this.logFiles = new HashMap<>();
        this.macros = new HashMap<>();
        this.STs    = new ArrayList<>();
        this.cache  = new Cache(1000);//,this.configFile, this.debug);
        this.logger = new CCLogger(null, this.debug);
        this.logFile = null;
    }

    public void setup() throws Exception {
        this.ParseConfig();
        this.logger.log(new LogEntry("EV", "localhost", ("conf-file-read " + this.configFile)));
        
        File f = new File(this.logFile);
        if (!f.exists())
        {
            f.createNewFile();
            this.logger.log(new LogEntry("EV", "localhost", ("log-file-create " + this.logFile)));
        }
        
        if (this.tipo == Tipo.SP)
        {
            this.ParseDB();
            this.logger.log(new LogEntry("EV", "localhost", ("db-file-read " + this.databaseFile)));
        }

        this.ParseSTfile();
        this.logger.log(new LogEntry("EV", "localhost", ("st-file-read " + this.STfile)));
    }

    public void ParseSTfile () throws Exception
    {
        List<String> lines = new ArrayList<String>();
        try
        {
            lines = Files.readAllLines(Paths.get(this.STfile));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            this.ThrowException(new IOException("Couldn't read DB file"));
        }

        for (String line : lines)
        {
            if (line.length() == 0 || line.charAt(0) == '#')
                continue;

            String[] tokens = line.split(" ");
            
            if (tokens.length != 2) 
                this.ThrowException(new InvalidSTFile("Incorrect number of arguments for entry: " + line));

            if (!tokens[1].equals("ST"))
                this.ThrowException(new InvalidSTFile("Wrong type \"" + tokens[1] + "\""));

            this.STs.add(tokens[0]);
        }            
    }

    public void ParseConfig() throws Exception
    {
        List<String> lines = new ArrayList<String>();
        try
        {
            lines = Files.readAllLines(Paths.get(this.configFile));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            this.ThrowException(new IOException("Couldn't read DB file"));
        }

        for (String line : lines)
        {
            if (line.length() == 0 || line.charAt(0) == '#')
                continue;

            String[] tokens = line.split(" ");

            if (tokens.length != 3)
                this.ThrowException(new InvalidConfigException("Config file entry should have 3 arguments"));

            if (tokens[1].equals("DB"))
            {
                this.databaseFile = tokens[2];
            }

            else if (tokens[1].equals("SS"))
            {
                this.tipo = Tipo.SP;
                this.SSlist.add(tokens[2]);
            }

            else if (tokens[1].equals("SP"))
            {
                this.tipo = Tipo.SS;
                this.SP = tokens[2];
            }

            else if (tokens[1].equals("DD"))
            {
                if (!this.DDlist.containsKey(tokens[0]))
                    this.DDlist.put(tokens[0],new ArrayList<>());
                this.DDlist.get(tokens[0]).add(tokens[1]);
            }

            else if (tokens[1].equals("LG"))
            {
                if (this.logFile == null)
                {
                    this.domain = tokens[0];
                    if (!this.domain.endsWith("."))
                        this.domain += ".";
                    this.logFile = tokens[2];
                    this.logger.setLogFile(tokens[2]);
                }
                else
                {
                    if (!this.logFiles.containsKey(tokens[0]))
                        this.logFiles.put(tokens[0], new ArrayList<>());
                    this.logFiles.get(tokens[0]).add(tokens[2]);
                }
            }
            
            else if (tokens[1].equals("ST"))
            {
                if (!tokens[0].equals("root"))
                    this.ThrowException(new InvalidConfigException("ST entry should have 'root' as its parameter"));

                this.STfile = tokens[2];
            }

            else
                this.ThrowException(new InvalidConfigException("Invalid type " + tokens[1]));
        }
    }

    public void ParseDB () throws Exception 
    {
        if (this.tipo == Tipo.SP)
        {

            List<String> lines = new ArrayList<String>();
            try
            {
                lines = Files.readAllLines(Paths.get(this.databaseFile));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                this.ThrowException(new IOException("Couldn't read DB file"));
            }
    
            Map<String, CacheEntry> alias = new HashMap<>();
            this.NumDBentries = 0;
    
            for (String line : lines)
            {
                if (line.length() == 0 || line.charAt(0) == '#')
                    continue;
    
                String[] tokens = line.split(" ");
    
                for (int i=0 ; i<tokens.length ; i++)
                {
                    for (String macro : this.macros.keySet())
                        if (tokens[i].contains(macro)) tokens[i] = tokens[i].replace(macro, this.macros.get(macro));
                }
    
                if (tokens[1].equals("DEFAULT"))
                {
                    if (tokens.length != 3)
                        this.ThrowException(new InvalidDatabaseException("Macro has too many arguments"));
                    
                    this.macros.put(tokens[0], tokens[2]);
                    this.cache.put(new CacheEntry(tokens[0], tokens[1], tokens[2], "FILE"));
                    this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));
                }
                else
                {
                    if (!tokens[0].endsWith(".")) tokens[0] = tokens[0] + "." + this.macros.get("@");
                    
                    if (tokens[1].equals("A"))
                    {
        
                        if (tokens.length < 4)
                            this.ThrowException(new InvalidDatabaseException("A entry should have 4/5 arguments"));
                        
                        if (tokens.length == 4)
                            this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                        else 
                            this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "FILE"));
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));
                    }
                    else if (tokens[1].equals("SOAADMIN"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOASP field is not correct (too many arguments)"));
                            
                        this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));
                    }
                    else if (tokens[1].equals("SOASERIAL"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOASERIAL field is not correct"));
        
                        this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));
                    }
                    else if (tokens[1].equals("SOAEXPIRE"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOAEXPIRE field is not correct (too many arguments)"));
                        
                        this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));
                    }
                    else if (tokens[1].equals("SOAREFRESH"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOAREFRESH field is not correct"));
        
                        this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));
                    }
                    else if (tokens[1].equals("SOARETRY"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOARETRY field is not correct"));
        
                        this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));
                    }
                    else
                    {
    
                        if (!tokens[2].endsWith(".")) tokens[2] = tokens[2] + "." + this.macros.get("@");
                        
                        if (tokens[1].equals("SOASP"))
                        {
                            if (tokens.length != 4)
                                this.ThrowException(new InvalidDatabaseException("SOASP field is not correct (too many arguments)"));
                            
                            this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));
                        }
                        else if (tokens[1].equals("NS"))
                        {
                            if (tokens.length < 3)
                                this.ThrowException(new InvalidDatabaseException("NS entry should have 3/4/5 arguments"));
                            
                            if (tokens.length == 3)
                                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], "FILE"));
                            else if (tokens.length == 4)
                                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                            else 
                                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "FILE"));
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));
                        }
                        else if (tokens[1].equals("MX"))
                        {
                            if (tokens.length < 4)
                                this.ThrowException(new InvalidDatabaseException("MX entry should have 4/5 arguments"));
                            
                            if (tokens.length == 4)
                                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                            else 
                                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "FILE"));
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));    
                        }
                        else if (tokens[1].equals("CNAME"))
                        {
                            if (tokens.length != 4)
                                this.ThrowException(new InvalidDatabaseException("CNAME entry should have 4 arguments"));
                    
                            if (alias.containsKey(tokens[2]))
                                this.ThrowException(new InvalidDatabaseException("A canonic name should not point to other canonic name"));
                            
                            if (alias.containsKey(tokens[0]))
                                this.ThrowException(new InvalidDatabaseException("The same canonic name should not be given to two different parameters"));
                    
                            
                            CacheEntry ce = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE");
                            alias.put(tokens[0], ce);
                            this.cache.put(ce);
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + line));    
                        }
            
                        else 
                        this.ThrowException(new InvalidDatabaseException("Type " + tokens[1] + " is invalid"));
                    }
                }
                this.NumDBentries++;
            }
        }
    }

    public void UDPreceiving () throws IOException {
        boolean status = true;
        DatagramSocket serverSocket = new DatagramSocket(this.port, InetAddress.getByName("0.0.0.0"));

        while (status)
        {
            byte [] buf = new byte[256];
            DatagramPacket receive = new DatagramPacket(buf,buf.length);
            serverSocket.receive(receive) ;   // extrair ip cliente, Port Client, Payload UDP
            InetAddress clientAddress = receive.getAddress();
            int clientPort = receive.getPort();
            MyAppProto msg = new MyAppProto(receive.getData());
            this.logger.log(new LogEntry("QR", clientAddress.toString(), msg.toString()));

            List<CacheEntry> responseValues = this.cache.get(msg.getName(), msg.getTypeOfValue());
            List<CacheEntry> authoritativeValues = this.cache.get(this.domain, "NS");
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

        serverSocket.close();
    }

    public void ThrowException(Exception e) throws Exception
    {
        this.logger.log(new LogEntry("FL", "localhost", e.getMessage()));
        e.printStackTrace();
        throw e;
    }

    public Tipo getTipo() { return this.tipo; }

    public void run()
    {
        try
        {
            this.setup();
    
            if (this.tipo == Tipo.SP)
            {
                TransferenciaZonaSPManager tzspm = new TransferenciaZonaSPManager(  this.SSlist, 
                                                                                    this.port, 
                                                                                    this.logger, 
                                                                                    this.domain,
                                                                                    this.NumDBentries,
                                                                                    this.databaseFile);
    
                Thread  zoneTransfer = new Thread(tzspm, "Zone Transfer Manager (SP)");
                zoneTransfer.start();
    
                this.UDPreceiving();
            }
            else if (this.tipo == Tipo.SS)
            {
                TransferenciaZonaSSManager tzssm = new TransferenciaZonaSSManager(  this.SP, 
                                                                                    this.cache, 
                                                                                    this.macros, 
                                                                                    this.port, 
                                                                                    this.logger, 
                                                                                    this.domain);
                
                Thread zoneTransfer = new Thread(tzssm, "Zone Transfer Manager (SS)");
                zoneTransfer.start();

                this.UDPreceiving();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) throws Exception {
        if (args.length < 1)
        {
            System.out.println("<Usage> \nSP [portNumber] timeout [D] configFile\nArguments with [] are not mandatory\n");
            return;
        }

        Servidor servidor;

        if (args.length == 3 && args[args.length-2].equals("D"))
            servidor = new Servidor(53, Integer.parseInt(args[0]), args[2], true);
        else if (args.length == 4 && args[args.length-2].equals("D"))
            servidor = new Servidor(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[3], true);
        else if (args.length == 2)
            servidor = new Servidor(53, Integer.parseInt(args[0]), args[1], false);
        else if (args.length == 3)
            servidor = new Servidor(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], false);
        else 
            return;

        servidor.run(); 
    }
}
