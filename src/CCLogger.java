import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used as a Logger in the context of the project.
 * 
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class CCLogger 
{
    /**
     * Strings containing the name of the log files
     */
    private String logFile, allLogs;
    /**
     * boolean indicating whether the logs should be displayed on the terminal or file
     */
    private boolean debugMode;
    /**
     * {@link List} of staged entries, both on the normal log file and on the general log file
     */
    private List<LogEntry> stagedEntries, allLogsStagedEntries;

    /**
     * Constructs a new CCLoger instance
     * @param logFile normal log file of the server
     * @param allLogs general log file 
     * @param debugMode boolean indicating whether debug mode is enabled
     */
    public CCLogger (String logFile, String allLogs, boolean debugMode)
    {
        this.logFile = logFile;
        this.allLogs = allLogs;
        this.debugMode = debugMode;
        this.stagedEntries = new ArrayList<LogEntry>();
        this.allLogsStagedEntries = new ArrayList<LogEntry>();
    }

    /**
     * Method used to set the normal log file
     * @param logFile Name of the log file
     */
    public synchronized void setLogFile(String logFile)
    {
        this.logFile = logFile;
    }
    
    /**
     * Method used to set the general log file
     * @param allLogs Name of the log file
     */
    public synchronized void setAllLogs(String allLogs)
    {
        this.allLogs = allLogs;
    }

    /**
     * Method used to log a {@link LogEntry} to the general log file
     * @param log
     * @throws IOException
     */
    public synchronized void generalLog(LogEntry log) throws IOException
    {
        if (this.allLogs == null || !new File(this.allLogs).exists())
            this.allLogsStagedEntries.add(log);
        else
        {
            this.allLogsStagedEntries.add(log);
            FileWriter fw = new FileWriter(this.logFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (LogEntry entry : allLogsStagedEntries)
                bw.write(entry.toString());
            bw.close();
            fw.close();
            this.allLogsStagedEntries.clear();
        }
        if (allLogsStagedEntries.size() > 50000)
            this.allLogsStagedEntries.clear();
    }

    public synchronized void log(LogEntry log) throws IOException
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
        this.generalLog(log);
    }
}
