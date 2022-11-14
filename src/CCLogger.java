import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CCLogger 
{
    private String logFile;
    private boolean debugMode;

    public CCLogger (String logFile, boolean debugMode)
    {
        this.logFile = logFile;
        new File(this.logFile);
        this.debugMode = debugMode;
    }

    public void log(String message) throws IOException
    {
        if (this.debugMode)
        {
            System.out.println(message);
        }
        else
        {
            FileWriter fw = new FileWriter(this.logFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(message);
            bw.newLine();
            bw.close();
            fw.close();
        }
    }
}
