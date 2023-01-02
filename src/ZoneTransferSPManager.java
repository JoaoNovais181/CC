import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementation of the Worker of the {@link ZoneTransferSPManager}, used to send the Database entries to the Secondary Server
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
class ZoneTransferSPWorker implements Runnable
{
    /**
     * The socket used to communicate with the Secondary Server
     */
    private Socket socket;
    /**
     * The list of Secodary Servers
     */
    private List<String> SSlist;
    /**
     * The logger used to log every event on the primary server
     */
    private CCLogger logger;
    /**
     * The domain where the Server is located at
     */
    private String domain;
    /**
     * The list of {@link CacheEntry} of the Server's Database
     */
    private List<CacheEntry> DBentries;


    /**
     * Creation of a new instance of ZoneTransferSPWorker
     * @param socket The socket used to communicate with the Secondary Server
     * @param SSlist The list of Secodary Servers
     * @param logger The logger used to log every event on the primary server
     * @param domain The domain where the Server is located at
     * @param DBentries The list of {@link CacheEntry} of the Server's Database
     */
    public ZoneTransferSPWorker(Socket socket, List<String> SSlist, CCLogger logger, String domain, List<CacheEntry> DBentries)
    {
        this.socket = socket;
        this.SSlist = SSlist;
        this.logger = logger;
        this.domain = domain;
        this.DBentries = DBentries;
    }

    @Override
    /**
     * Method used to run the Worker
     */
    public void run() {
        try
        {
            LocalDateTime start = LocalDateTime.now();
                
            InetAddress receivingAddress = socket.getInetAddress();
            String ra = (receivingAddress.toString().startsWith("/")) ?receivingAddress.toString().substring(1) :receivingAddress.toString();
            boolean isSP = false;
            for (String SS: this.SSlist)
                if (ra.equals(SS)) isSP = true;

            if (!isSP) 
            {
                this.logger.log(new LogEntry("EZ", ra, "SP"));
                return; 
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;
            line = in.readLine();
            if (!line.equals("domain: \"" + this.domain + "\"")) 
            {
                this.logger.log(new LogEntry("EZ", ra, "SP"));
                return; 
            }

            out.println("entries: " + this.DBentries.size());
            out.flush();

            line = in.readLine();

            if (!line.equals("ok: "+this.DBentries.size())) 
            {
                this.logger.log(new LogEntry("EZ", ra, "SP"));
                return; 
            }

            int totalLength = 0;
            for (CacheEntry ce : this.DBentries)
            {
                String l = ce.tcpString();
                totalLength += l.length();
                out.println(l);
                out.flush();
            }

            socket.shutdownOutput();
            socket.shutdownInput();
            this.logger.log(new LogEntry("ZT", ra, "SP, totalBytes = " + totalLength + ", durations = " + ChronoUnit.MILLIS.between(start, LocalDateTime.now()) + "ms"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}

/**
 * Implementation of {@code ZoneTransferSPManager} that extends the {@link ZoneTransferManager} abstract class, used to 
 * manager the Zone Transfer on a Primary Server
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class ZoneTransferSPManager extends ZoneTransferManager {

    /**
     * The list of Secodary Servers
     */
    private List<String> SSlist;
    /**
     * The logger used to log every event on the primary server
     */
    private CCLogger logger;
    /**
     * The domain where the Server is located at
     */
    private String domain;
    /**
     * The list of {@link CacheEntry} of the Server's Database
     */
    private List<CacheEntry> DBentries;
    /**
     * integer that represents the port the server is listening on
     */
    private int port;
    /**
     * Boolean indicating whether the server is running or not
     */
    private boolean running;

    /**
     * Constructor for the {@code ZoneTransferSPManager}
     * @param SSlist The list of Secodary Servers
     * @param port The port the server is listening on
     * @param logger The logger used to log every event on the primary server
     * @param domain The domain where the Server is located at
     * @param DBentries The list of {@link CacheEntry} of the Server's Database
     */
    public ZoneTransferSPManager(List<String> SSlist, int port, CCLogger logger, String domain, List<CacheEntry> DBentries) 
    {
        this.SSlist = SSlist;
        this.port = port;
        this.logger = logger;
        this.domain = domain;
        this.DBentries = DBentries;
        this.running = true;
    }

    @Override
    /**
     * Method used to run the server
     */
    public synchronized void run() {
        try
        {
            ServerSocket serverSocket = new ServerSocket(this.port);
            while (this.running)
            {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new ZoneTransferSPWorker(socket, this.SSlist, this.logger, this.domain, this.DBentries));
                thread.start();
            }

            serverSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    /**
     * Method used to turn off the server
     */
    public synchronized void turnOff() { this.running = false; }
}
