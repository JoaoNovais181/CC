import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

class TransferenciaZonaSPWorker implements Runnable
{
    private Socket socket;
    private List<String> SSlist;
    private int NumDBentries;
    private CCLogger logger;
    private String domain, databaseFile;

    public TransferenciaZonaSPWorker(Socket socket, List<String> SSlist, CCLogger logger, String domain, int NumDBentries, String databaseFile)
    {
        this.socket = socket;
        this.SSlist = SSlist;
        this.logger = logger;
        this.domain = domain;
        this.NumDBentries = NumDBentries;
        this.databaseFile = databaseFile;
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

            out.println("entries: "+this.NumDBentries);
            out.flush();

            line = in.readLine();

            if (!line.equals("ok: "+this.NumDBentries)) 
            {
                this.logger.log(new LogEntry("EZ", ra, "SP"));
                return; 
            }

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

            int totalLength = 0;
            for (String l : lines)
            {
                if (l.isEmpty() || l.startsWith("#"))
                    continue;

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

public class TransferenciaZonaSPManager implements Runnable {

    private List<String> SSlist;
    private int port, NumDBentries;
    private CCLogger logger;
    private String domain, databaseFile;

    public TransferenciaZonaSPManager(List<String> SSlist, int port, CCLogger logger, String domain, int NumDBentries, String databaseFile) 
    {
        this.SSlist = SSlist;
        this.port = port;
        this.logger = logger;
        this.domain = domain;
        this.NumDBentries = NumDBentries;
        this.databaseFile = databaseFile;
    }

    @Override
    public void run() {
        try
        {
            ServerSocket serverSocket = new ServerSocket(this.port);
            while (true)
            {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new TransferenciaZonaSPWorker(socket, this.SSlist, this.logger, this.domain, this.NumDBentries, this.databaseFile));
                thread.start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
}
