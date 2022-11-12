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
    private String databaseFile;
    private Map<String,String> macros;
    private List<Object> entries;
    private int databaseSerialNumber;
    public SP (String databaseFile)
    {
        this.databaseFile = databaseFile;
        this.macros = new HashMap<>();
    }

    public List<Object> Parse () throws FileNotFoundException, InvalidDatabaseException {
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

            if (tokens[1].equals("DEFAULT"))
            {
                if (tokens.length != 3)
                    throw new InvalidDatabaseException("Macro has too many arguments");

                macros.put(tokens[0], tokens[2]);
            }

            else if (tokens[1].equals("SOASP"))
            {
                if (tokens.length != 4)
                    throw new InvalidDatabaseException("SOASP field is not correct (too many arguments)");

                this.entries.add(new DBEntry(tokens[0],DBEntry.stringToType(tokens[1]), tokens[2], Integer.parseInt(tokens[3])));
            }



        }
    }
}
