import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SP
{
    private String configFile, databaseFile, logFile;
    private Cache cache;
    private Map<String,String> macros;
    private boolean debug;
    private CCLogger logger;
    private List<String> SSlist;
    private Map<String,List<String>> DDlist;
    private Map<String,List<String>> logFiles;
    // private Map<String, Object> entries;
    // private Map<String, Object> SOASPentires;
    // private Map<String, Object> NSs;
    // private Map<String, List<Object>> MXs;
    // private Map<String, Object> alias;
    // private int databaseSerialNumber, SOAEXPIRE, ttlDB;
    public SP (String configFile)
    {
        this.configFile = configFile;
        this.SSlist = new ArrayList<>();
        this.DDlist = new HashMap<>();
        this.logFiles = new HashMap<>();
        this.ParseConfig();
        this.logger = new CCLogger(this.logFile, this.debug);
        // this.databaseFile = configFile;
        this.macros = new HashMap<>();
        // this.entries = new HashMap<>();
        // this.NSs = new HashMap<>();
        // this.MXs = new HashMap<>();
        // this.alias = new HashMap<>();
        this.cache = new Cache(64000);//,this.configFile, this.debug);
        // this.databaseSerialNumber = -1;
        // this.SOAEXPIRE = -1;
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
                // if (tokens[0].equals(lines))
            }
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
        System.out.println(this.cache);
    }
}


class Test
{
    public static void main(String[] args)
    {
        SP sp = new SP("./BasedeDadosSPdomMafarrico.txt");

        try {sp.ParseDB();}
        catch (Exception e) {e.printStackTrace();}

    }
}