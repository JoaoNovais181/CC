import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class defined to represent a DNS Server on the context of the project.
 * 
 * <p> The server can have 3 distict types -> Primary Server (SP), Secondary Server (SS) or DNS Cache Only Server (SR) </p>
 * 
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class Server
{
    /**
     * Definition of {@code enum Type}, used to represent the different types of servers
     */
    public enum Type {SP, SS, SR, UNDEFINED};
    /**
     * Variable of type {@link Type}, used to represent the type of this server
     */
    private Type type;
    /**
     * boolean to represent whether the server is a top-level server or not
     */
    private boolean isST;
    /**
     * Strings representing the domain of the server, it's configuration file, database file, log file and ST list file
     */
    private String domain, configFile, databaseFile, logFile, STfile;
    /**
     * {@link Cache} used to store the information about the server's domain
     */
    private Cache cache;
    /**
     * {@link Map} used to represent the "DEFAULT" entries on the Database file
     */
    private Map<String,String> macros;
    /**
     * boolean to indicate whether the server is operating on debug mode or not
     */
    private boolean debug;
    /**
     * {@link CCLoger} used to log the operations performed by the server
     */
    private CCLogger logger;
    /**
     * {@link List} of Strings that represent the addres of the Secondary Servers of the domain (if the server is a Primary Server)
     */
    private List<String> SSlist;
    /**
     * String containing the address of the primary server of the domain (if the server is a Secondary Server)
     */
    private String SP;
    /**
     * List of Default Domains of a server
     */
    private Map<String, String> DDlist;
    /**
     * {@link Map} that pairs the name of something to it's log file
     */
    private Map<String,String> logFiles;
    /**
     * {@link List} that contains String containing the Top-Level Servers's addresses 
     */
    private List<String> STs;
    /**
     * {@link ZoneTransferManager} used to manage the zone transfer of the Server (if the server is not a SR)
     */
    private ZoneTransferManager tzm;
    /**
     * {@link CommunicationManager} used to manage the communication of the Server
     */
    private CommunicationManager cm;
    /**
     * ints containing the value of the timeout waited to receive answers to queries and the port the server should listen on
     */
    private int timeout, port;

    /**
     * Constructor of the Server
     * @param port port it should listen on
     * @param timeout timeout to wait for answers to queries
     * @param configFile file containing the configuration of the server
     * @param debug boolean indicating wheter the server is in debug mode
     * @throws IOException
     * @throws InvalidConfigException
     * @throws InvalidDatabaseException
     * @throws InvalidCacheEntryException
     */
    public Server (int port, int timeout, String configFile, boolean debug) throws IOException, InvalidConfigException, InvalidDatabaseException, InvalidCacheEntryException
    {
        this.type = Type.UNDEFINED;
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
        this.logger = new CCLogger(null, null, this.debug);
        this.logFile = null;
        this.STfile = null;
        this.tzm = null;
        this.cm = null;
    }

    /**
     * Method called to setup the server (read it's configuration file, figure out what the type of the server is,
     * parse the Database file, parse the ST list file, instantiate the {@link ZoneTransferManager} and the
     * {@link CommunicationManager})
     * @throws Exception
     */
    private void setup() throws Exception {
        this.ParseConfig();
        this.logger.log(new LogEntry("EV", "localhost", ("conf-file-read " + this.configFile)));
        
        File f = new File(this.logFile), allLogsFile = null;
        String allLogs = this.logFiles.get("all");
        if (allLogs != null)
        {
            allLogsFile = new File(allLogs);
            this.logger.setAllLogs(allLogs);
            
            if (!allLogsFile.exists())
            {
                allLogsFile.createNewFile();
                this.logger.log(new LogEntry("EV", "localhost", ("all log-file-create " + allLogs)));
            }
        }

        if (!f.exists())
        {
            f.createNewFile();
            this.logger.log(new LogEntry("EV", "localhost", ("log-file-create " + this.logFile)));
        }
        


        if (this.type == Type.SP)
        {
            this.ParseDB();
            this.logger.log(new LogEntry("EV", "localhost", ("db-file-read " + this.databaseFile)));
        }

        if (!this.isST)
        {
            this.ParseSTfile();
            this.logger.log(new LogEntry("EV", "localhost", ("st-file-read " + this.STfile)));
        }

        if (this.type == Type.SP)
        {
            List<CacheEntry> DBentries = this.cache.getEntriesByOrigin("FILE");
            this.tzm = new ZoneTransferSPManager(  this.SSlist, 
                                                        this.port, 
                                                        this.logger, 
                                                        this.domain,
                                                        DBentries);
        }
        else if (this.type == Type.SS)
            this.tzm = new ZoneTransferSSManager(  this.SP, 
                                                        this.cache, 
                                                        this.port, 
                                                        this.logger, 
                                                        this.domain);


        if (this.type == Type.SR)
            this.cm = new ResolveCommunicationManager(this.logger, this.cache, this.domain, this.port, this.timeout, this.STs, this.DDlist);
        else
            this.cm = new AuthoritativeCommunicationManager(this.logger, this.cache, this.domain, this.port, this.timeout);
    }

    /**
     * Method used to parse the file containing the list of the ST servers
     * @throws Exception
     */
    private void ParseSTfile () throws Exception
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

    /**
     * Method used to parse the configuration file
     * @throws Exception
     */
    private void ParseConfig() throws Exception
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
                if (this.type == Type.SS)
                    this.ThrowException(new InvalidConfigException("Secondary Server cannot have DB entries"));
                else if (this.type == Type.UNDEFINED)
                    this.type = Type.SP;
                this.databaseFile = tokens[2];
            }

            else if (tokens[1].equals("SS"))
            {
                if (this.type == Type.SS)
                    this.ThrowException(new InvalidConfigException("Secondary Server cannot have SS entries"));
                else if (this.type == Type.UNDEFINED)
                    this.type = Type.SP;
                this.SSlist.add(tokens[2]);
            }

            else if (tokens[1].equals("SP"))
            {
                if (this.type == Type.SP)
                    this.ThrowException(new InvalidConfigException("Primary Server cannot have SP entries"));
                else if (this.type == Type.UNDEFINED)
                    this.type = Type.SS;
                this.SP = tokens[2];
            }

            else if (tokens[1].equals("DD"))
            {
                this.DDlist.put(tokens[0], tokens[1]);
            }

            else if (tokens[1].equals("LG"))
            {
                if (this.logFile == null && !tokens[0].equals("all"))
                {
                    this.domain = tokens[0];
                    if (!this.domain.endsWith("."))
                        this.domain += ".";
                    this.logFile = tokens[2];
                    this.logger.setLogFile(tokens[2]);
                }
                else
                {
                    this.logFiles.put(tokens[0], tokens[2]);
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
        if (this.type == Type.UNDEFINED)
            this.type = Type.SR;
        else if (this.STfile == null)
            this.isST = true;
        else 
            this.isST = false;
    }

    /**
     * Method used to parse the Database File
     * @throws Exception
     */
    private void ParseDB () throws Exception 
    {
        if (this.type == Type.SP)
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
                    
                    CacheEntry entry = new CacheEntry(tokens[0], tokens[1], tokens[2], "FILE");
                    this.macros.put(tokens[0], tokens[2]);
                    this.cache.put(entry);
                    this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                }
                else
                {
                    if (!tokens[0].endsWith(".") && !this.isST) tokens[0] = tokens[0] + "." + this.macros.get("@");
                    else if (!tokens[0].endsWith(".")) tokens[0] = tokens[0] + ".";

                    if (tokens[1].equals("A"))
                    {
        
                        if (tokens.length < 4)
                            this.ThrowException(new InvalidDatabaseException("A entry should have 4/5 arguments"));
                        
                        CacheEntry entry;
                        if (tokens.length == 4)
                            entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE");
                        else 
                            entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "FILE");
                        this.cache.put(entry);
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                    }
                    else if (tokens[1].equals("SOAADMIN"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOASP field is not correct (too many arguments)"));
                        
                        CacheEntry entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE");
                        this.cache.put(entry);
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                    }
                    else if (tokens[1].equals("SOASERIAL"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOASERIAL field is not correct"));
        
                        CacheEntry entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE");
                        this.cache.put(entry);
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                    }
                    else if (tokens[1].equals("SOAEXPIRE"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOAEXPIRE field is not correct (too many arguments)"));
                        
                        CacheEntry entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"); 
                        this.cache.put(entry);
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                    }
                    else if (tokens[1].equals("SOAREFRESH"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOAREFRESH field is not correct"));
        
                        CacheEntry entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE");
                        this.cache.put(entry);
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                    }
                    else if (tokens[1].equals("SOARETRY"))
                    {
                        if (tokens.length != 4)
                            this.ThrowException(new InvalidDatabaseException("SOARETRY field is not correct"));
                        
                        CacheEntry entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE");
                        this.cache.put(entry);
                        this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                    }
                    else
                    {
    
                        if (!tokens[2].endsWith(".")) tokens[2] = tokens[2] + "." + this.macros.get("@");
                        
                        if (tokens[1].equals("SOASP"))
                        {
                            if (tokens.length != 4)
                                this.ThrowException(new InvalidDatabaseException("SOASP field is not correct (too many arguments)"));
                            
                            CacheEntry entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE");
                            this.cache.put(entry);
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                        }
                        else if (tokens[1].equals("NS"))
                        {
                            if (tokens.length < 3)
                                this.ThrowException(new InvalidDatabaseException("NS entry should have 3/4/5 arguments"));
                            
                            CacheEntry entry;
                            if (tokens.length == 3)
                                entry = new CacheEntry(tokens[0],tokens[1], tokens[2], "FILE");
                            else if (tokens.length == 4)
                                entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE");
                            else 
                                entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "FILE");
                            this.cache.put(entry);
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                        }
                        else if (tokens[1].equals("MX"))
                        {
                            if (tokens.length < 4)
                                this.ThrowException(new InvalidDatabaseException("MX entry should have 4/5 arguments"));
                            
                            CacheEntry entry;
                            if (tokens.length == 4)
                                entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE");
                            else 
                                entry = new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "FILE");
                            
                            this.cache.put(entry);
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));    
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
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + ce.dbString()));    
                        }
                        else if (tokens[1].equals("PTR"))
                        {
                            if (tokens.length != 3)
                                this.ThrowException(new InvalidDatabaseException("PTR entry should have 3 arguments"));

                            CacheEntry entry = new CacheEntry(tokens[0], tokens[1], tokens[2], "FILE");
                            this.cache.put(entry);
                            this.logger.log(new LogEntry("EV", "localhost", "DataBase entry added to cache. Entry: " + entry.dbString()));
                        }
            
                        else 
                            this.ThrowException(new InvalidDatabaseException("Type " + tokens[1] + " is invalid"));
                    }
                }
            }
        }
    }


    /**
     * Mathod used to throw exceptions and also log them
     * @param e Exception to be thrown
     * @throws Exception
     */
    private void ThrowException(Exception e) throws Exception
    {
        this.logger.log(new LogEntry("FL", "localhost", e.getMessage()));
        e.printStackTrace();
        throw e;
    }

    /**
     * Method used to get the {@link Type} of the server
     * @return the type of the server
     */
    public Type getType() { return this.type; }

    /**
     * Method called to run the server
     */
    public void run()
    {
        try
        {
            this.setup();
            Thread ztThread = new Thread(this.tzm), communicationThread = new Thread(this.cm);
            ztThread.start();
            communicationThread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) throws Exception {
        if (args.length < 1)
        {
            System.out.println("<Usage> : SP [portNumber] timeout [D] configFile");
            return;
        }

        Server servidor;

        if (args.length == 3 && args[args.length-2].equals("D"))
            servidor = new Server(53, Integer.parseInt(args[0]), args[2], true);
        else if (args.length == 4 && args[args.length-2].equals("D"))
            servidor = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[3], true);
        else if (args.length == 2)
            servidor = new Server(53, Integer.parseInt(args[0]), args[1], false);
        else if (args.length == 3)
            servidor = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], false);
        else 
            return;

        servidor.run(); 
    }
}
