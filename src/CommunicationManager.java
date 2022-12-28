public abstract class CommunicationManager implements Runnable 
{
    protected CCLogger logger;
    protected Cache cache;
    protected String domain;
    protected int port;

    public CommunicationManager(CCLogger logger, Cache cache, String domain, int port)
    {
        this.logger = logger;
        this.cache = cache;
        this.domain = domain;
        this.port = port;
    }

    public abstract void run ();  
}
