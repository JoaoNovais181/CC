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

class TransferenciaZonaSPWorker implements Runnable
{
    private Socket socket;
    private List<String> SSlist;
    private CCLogger logger;
    private String domain;
    private List<CacheEntry> DBentries;

    public TransferenciaZonaSPWorker(Socket socket, List<String> SSlist, CCLogger logger, String domain, List<CacheEntry> DBentries)
    {
        this.socket = socket;
        this.SSlist = SSlist;
        this.logger = logger;
        this.domain = domain;
        this.DBentries = DBentries;
    }

    @Override
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
                totalLength += l.length() + 1;
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

public class TransferenciaZonaSPManager extends TransferenciaZonaManager {

    private List<String> SSlist;
    private int port;
    private CCLogger logger;
    private String domain;
    private List<CacheEntry> DBentries;
    private boolean running;

    public TransferenciaZonaSPManager(List<String> SSlist, int port, CCLogger logger, String domain, List<CacheEntry> DBentries) 
    {
        this.SSlist = SSlist;
        this.port = port;
        this.logger = logger;
        this.domain = domain;
        this.DBentries = DBentries;
        this.running = true;
    }

    @Override
    public synchronized void run() {
        try
        {
            ServerSocket serverSocket = new ServerSocket(this.port);
            while (this.running)
            {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new TransferenciaZonaSPWorker(socket, this.SSlist, this.logger, this.domain, this.DBentries));
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
    public synchronized void turnOff() { this.running = false; }
}
