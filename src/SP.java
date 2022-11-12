import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SP
{
    private String databaseFile;
    private Map<String,String> macros;
    private Map<String, Object> entries;
    // private Map<String, Object> SOASPentires;
    private Map<String, Object> NSs;
    private Map<String, List<Object>> MXs;
    private Map<String, Object> alias;
    private Map<String, Object> cache;
    private int databaseSerialNumber, SOAEXPIRE;
    public SP (String databaseFile)
    {
        this.databaseFile = databaseFile;
        this.macros = new HashMap<>();
        this.entries = new HashMap<>();
        this.NSs = new HashMap<>();
        this.MXs = new HashMap<>();
        this.alias = new HashMap<>();
        this.cache = new HashMap<>();
        this.databaseSerialNumber = -1;
        this.SOAEXPIRE = -1;
    }

    public void ParseDB () throws FileNotFoundException, InvalidDatabaseException {
        List<String> lines = new ArrayList<String>();
        try
        {
            lines = Files.readAllLines(Paths.get(this.databaseFile));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


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
            }

            else if (tokens[1].equals("CNAME"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("CNAME entry should have 4 arguments");

                if (this.alias.containsKey(tokens[2]))
                throw new InvalidDatabaseException("A canonic name should not point to other canonic name");
                
                if (this.alias.containsKey(tokens[0]))
                    throw new InvalidDatabaseException("The same canonic name should not be given to two different parameters");


                this.alias.put(tokens[0], new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3])));
            }
            
            if (!tokens[0].endsWith(".")) tokens[0] = tokens[0] + "." + this.macros.get("@");
            
            else if (tokens[1].equals("SOASP"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("SOASP field is not correct (too many arguments)");

                this.entries.put(tokens[0], new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3])));
            }

            else if (tokens[1].equals("SOAADMIN"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("SOASP field is not correct (too many arguments)");
                    
                this.entries.put(tokens[0], new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3])));
            }

            else if (tokens[1].equals("SOAEXPIRE"))
            {
                if (tokens.length != 4)
                throw new InvalidDatabaseException("SOAEXPIRE field is not correct (too many arguments)");
                
                this.entries.put(tokens[0], new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3])));
            }
            
            else if (tokens[1].equals("NS"))
            {
                if (tokens.length < 3)
                throw new InvalidDatabaseException("NS entry should have 3/4/5 arguments");
                
                
                if (tokens.length == 3)
                this.NSs.put(tokens[0], new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], -1));
                else if (tokens.length == 4)
                this.NSs.put(tokens[0], new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3])));
                else 
                this.NSs.put(tokens[0], new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));
            }
            
            
            else if (tokens[1].equals("MX"))
            {
                if (tokens.length < 4)
                throw new InvalidDatabaseException("MX entry should have 4/5 arguments");
                
                this.MXs.put(tokens[0], new ArrayList<>());
                
                if (tokens.length == 4)
                this.MXs.get(tokens[0]).add(new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3])));
                else 
                this.MXs.get(tokens[0]).add(new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));
                
            }
            
            else if (tokens[1].equals("A"))
            {
                if (tokens.length < 4)
                throw new InvalidDatabaseException("A entry should have 4/5 arguments");
                
                if (tokens.length == 4)
                    this.cache.put(tokens[0], new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3])));
                else 
                    this.cache.put(tokens[0], new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));

                }

            

            System.out.println(Arrays.toString(tokens));
        }

        this.cache.forEach((key,value) -> System.out.println("Entry: " + key + ", value: " + value + "\n"));
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