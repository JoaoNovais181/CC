
import java.lang.Runnable;

/**
 * Implementation of the abstract class {@code ZoneTransferManager}, used
 * to manager the Zone Transfer on primary and secondary servers
 *  
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public abstract class ZoneTransferManager implements Runnable
{
    public abstract void run();
    public abstract void turnOff();
}
