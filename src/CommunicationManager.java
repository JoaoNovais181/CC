public abstract class CommunicationManager implements Runnable 
{
    protected CCLogger logger;
    protected Cache cache;
    protected String domain;
    protected int port;
    protected int timeout;

    public CommunicationManager(CCLogger logger, Cache cache, String domain, int port, int timeout)
    {
        this.logger = logger;
        this.cache = cache;
        this.domain = domain;
        this.port = port;
        this.timeout = timeout;
    }

    public abstract void run ();  
}
