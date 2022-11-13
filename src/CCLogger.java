import java.io.File;

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

    public void log(String message)
    {
        if (this.debugMode)
        {

        }
    }
}
