/**
 * Implementation of an Exception to throw when a 
 * Database file is invalid
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class InvalidDatabaseException extends Exception
{
    public InvalidDatabaseException (String message)
    {
        super(message);
    }
}
