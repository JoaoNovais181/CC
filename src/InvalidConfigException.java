/**
 * Implementation of an Exception to throw when a 
 * Configuration file is invalid
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class InvalidConfigException extends Exception 
{
    public InvalidConfigException (String message)
    {
        super(message);
    }
}
