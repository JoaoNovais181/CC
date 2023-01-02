/**
 * Abstract class used to represent a {@code CommunicationManager} for the {@link Server} in the 
 * context of this Project.
 * 
 * <p> The classes that extend this class should implement the run method, as this class implements
 * the {@link Runnable} interface </p>
 * 
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897  
 */
public abstract class CommunicationManager implements Runnable 
{
    /**
     * {@link CCLoger used to log messages}
     */
    protected CCLogger logger;
    /**
     * {@link Cache} containing the values of the {@link Server}'s cache
     */
    protected Cache cache;
    /**
     * String containing the name of the Domain the {@link Server} is in
     */
    protected String domain;
    /**
     * The port the {@link Server} is listening on
     */
    protected int port;
    /**
     * Timeout used in queries
     */
    protected int timeout;

    /**
     * Constructor for CommunicationManager
     * @param logger the logger
     * @param cache the cache
     * @param domain the domain 
     * @param port the port
     * @param timeout the timeout
     */
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
