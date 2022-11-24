import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CCLogger 
{
    private String logFile;
    private boolean debugMode;
    private List<LogEntry> stagedEntries;

    public CCLogger (String logFile, boolean debugMode)
    {
        this.logFile = logFile;
        this.debugMode = debugMode;
        this.stagedEntries = new ArrayList<LogEntry>();
    }

    public void setLogFile(String logFile)
    {
        this.logFile = logFile;
    }

    public void log(LogEntry log) throws IOException
    {
        if (this.debugMode)
        {
            System.out.print(log);
        }
        else
        {
            if (this.logFile == null || !new File(this.logFile).exists())
                this.stagedEntries.add(log);
            else
            {
                this.stagedEntries.add(log);
                FileWriter fw = new FileWriter(this.logFile, true);
                BufferedWriter bw = new BufferedWriter(fw);
                for (LogEntry entry : stagedEntries)
                    bw.write(entry.toString());
                bw.close();
                fw.close();
                this.stagedEntries.clear();
            }
        }
    }
}
