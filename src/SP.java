import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SP
{
    private String serverName, configFile, databaseFile, logFile, STfile;
    private Cache cache;
    private Map<String,String> macros;
    private boolean debug;
    private CCLogger logger;
    private List<String> SSlist;
    private Map<String,List<String>> DDlist;
    private Map<String,List<String>> logFiles;
    private List<String> STs;

    public SP (String serverName, String configFile, boolean debug) throws IOException, InvalidConfigException, InvalidDatabaseException, InvalidCacheEntryException
    {
        this.serverName = serverName;
        this.configFile = configFile;
        this.debug = debug;

        this.SSlist = new ArrayList<>();
        this.DDlist = new HashMap<>();
        this.logFiles = new HashMap<>();
        this.macros = new HashMap<>();
        this.STs    = new ArrayList<>();
        this.cache = new Cache(64000);//,this.configFile, this.debug);
        this.logger = new CCLogger(null, this.debug);
    }

    public void setup() throws IOException, InvalidConfigException, InvalidDatabaseException, InvalidCacheEntryException
    {
        this.ParseConfig();
        this.logger.log(new LogEntry("EV", "localhost", ("conf-file-read " + this.configFile)));
        
        File f = new File(this.logFile);
        if (!f.exists())
        {
            f.createNewFile();
            this.logger.log(new LogEntry("EV", "localhost", ("log-file-create " + this.logFile)));
        }
        
        this.ParseDB();
        this.logger.log(new LogEntry("EV", "localhost", ("db-file-read " + this.configFile)));
    }

    public void ParseSTfile () throws IOException, InvalidSTFile
    {
        List<String> lines = new ArrayList<String>();
        try
        {
            lines = Files.readAllLines(Paths.get(this.STfile));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new IOException("Couldn't read DB file");
        }

        for (String line : lines)
        {
            if (line.length() == 0 || line.charAt(0) == '#')
                continue;

            String[] tokens = line.split(" ");
            
            if (tokens.length != 2) 
                throw new InvalidSTFile("Incorrect number of arguments for entry: " + line);

            if (!tokens[1].equals("ST"))
                throw new InvalidSTFile("Wrong type \"" + tokens[1] + "\"");

            this.STs.add(tokens[0]);
        }            
    }

    public void ParseConfig() throws IOException, InvalidConfigException
    {
        List<String> lines = new ArrayList<String>();
        try
        {
            lines = Files.readAllLines(Paths.get(this.configFile));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new IOException("Couldn't read DB file");
        }

        int firstDot = this.serverName.indexOf('.');
        String domain = this.serverName.substring(firstDot+1);

        for (String line : lines)
        {
            if (line.length() == 0 || line.charAt(0) == '#')
                continue;

            String[] tokens = line.split(" ");

            if (tokens.length != 3)
                throw new InvalidConfigException("Config file entry should have 3 arguments");

            if (tokens[1].equals("DB"))
            {
                this.databaseFile = tokens[2];
            }

            else if (tokens[1].equals("SS"))
            {
                this.SSlist.add(tokens[2]);
            }

            else if (tokens[1].equals("DD"))
            {
                if (!this.DDlist.containsKey(tokens[0]))
                    this.DDlist.put(tokens[0],new ArrayList<>());
                this.DDlist.get(tokens[0]).add(tokens[1]);
            }

            else if (tokens[1].equals("LG"))
            {
                if (tokens[0].equals(domain))
                {
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
                    throw new InvalidConfigException("ST entry should have 'root' as its parameter");

                this.STfile = tokens[2];
            }

            else
                throw new InvalidConfigException("Invalid type " + tokens[1]);
        }
    }

    public void ParseDB () throws InvalidDatabaseException, InvalidCacheEntryException, IOException 
    {
        List<String> lines = new ArrayList<String>();
        try
        {
            lines = Files.readAllLines(Paths.get(this.databaseFile));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new IOException("Couldn't read DB file");
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
                    throw new InvalidDatabaseException("Macro has too many arguments");
                
                this.macros.put(tokens[0], tokens[2]);
                this.cache.put(new CacheEntry(tokens[0], tokens[1], tokens[2], "FILE"));
                continue;
            }

            else if (tokens[1].equals("CNAME"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("CNAME entry should have 4 arguments");

                if (!tokens[0].endsWith(".")) tokens[0] = tokens[0] + "." + this.macros.get("@");
                if (!tokens[2].endsWith(".")) tokens[2] = tokens[2] + "." + this.macros.get("@");
                

                if (alias.containsKey(tokens[2]))
                    throw new InvalidDatabaseException("A canonic name should not point to other canonic name");
                
                if (alias.containsKey(tokens[0]))
                    throw new InvalidDatabaseException("The same canonic name should not be given to two different parameters");


                alias.put(tokens[0], new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                continue;
            }
            
            if (!tokens[0].endsWith(".")) tokens[0] = tokens[0] + "." + this.macros.get("@");
            
            if (tokens[1].equals("SOASP"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("SOASP field is not correct (too many arguments)");

                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
            }

            else if (tokens[1].equals("SOAADMIN"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("SOASP field is not correct (too many arguments)");
                    
                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
            }

            else if (tokens[1].equals("SOAEXPIRE"))
            {
                if (tokens.length != 4)
                throw new InvalidDatabaseException("SOAEXPIRE field is not correct (too many arguments)");
                
                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
            }

            else if (tokens[1].equals("SOASERIAL"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("SOASERIAL field is not correct");

                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
            }

            else if (tokens[1].equals("SOAREFRESH"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("SOAREFRESH field is not correct");

                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
            }

            else if (tokens[1].equals("SOARETRY"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("SOARETRY field is not correct");

                this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
            }
            
            else if (tokens[1].equals("NS"))
            {
                if (tokens.length < 3)
                    throw new InvalidDatabaseException("NS entry should have 3/4/5 arguments");
                
                if (tokens.length == 3)
                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], "FILE"));
                else if (tokens.length == 4)
                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                else 
                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "FILE"));
            }
            
            
            else if (tokens[1].equals("MX"))
            {
                if (tokens.length < 4)
                throw new InvalidDatabaseException("MX entry should have 4/5 arguments");
                
                if (tokens.length == 4)
                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                else 
                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "FILE"));
                
            }

            else if (tokens[1].equals("A"))
            {
                if (tokens.length < 4)
                throw new InvalidDatabaseException("A entry should have 4/5 arguments");
                
                if (tokens.length == 4)
                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), "FILE"));
                else 
                    this.cache.put(new CacheEntry(tokens[0],tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), "FILE"));
            }

            else 
                throw new InvalidDatabaseException("Type " + tokens[1] + " is invalid");
        }
        this.cache.put(alias.values());
        // System.out.println(this.cache);
    }
}


class MainSP
{
    public static void main(String[] args) throws IOException, InvalidConfigException, InvalidDatabaseException, InvalidCacheEntryException
    {
        if (args.length < 2)
            return;

        SP sp;

        if (args.length == 3 && args[0].equals("-g"))
            sp = new SP(args[1], args[2], true);
        else
            sp = new SP(args[0], args[1], false);

        sp.setup();
    }
}