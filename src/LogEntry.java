import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@code LogEntry} class used to represent
 * a entry on a log file
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class LogEntry 
{
    /**
     * {@link LocalDateTime} repesenting the timestamp on the log
     */
    private LocalDateTime TimeStamp;
    /**
     * String containing the Type, Address and Entry on the log
     */
    private String Type, Address, Entry;

    /**
     * Class constructor
     * @param Type type
     * @param Address address
     * @param Entry entry
     */
    public LogEntry(String Type, String  Address, String Entry)
    {
        this.Type = Type;
        this.Address = Address;
        this.Entry = Entry;
        this.TimeStamp = LocalDateTime.now();
    }

    /**
     * Getter for the type
     * @return type
     */
    public String getType()
    {
        return this.Type;
    }

    /**
     * Getter for the address
     * @return address
     */
    public String getAddress()
    {
        return this.Address;
    }

    /**
     * Getter for the entry
     * @return entry
     */
    public String getEntry()
    {
        return this.Entry;
    }

    /**
     * Getter for the timestamp
     * @return timestamp
     */
    public LocalDateTime getTimeStamp()
    {
        return this.TimeStamp;
    }

    /**
     * Setter for the timestamp
     * @param TimeStamp value to set
     */
    public void setTimeStamp(LocalDateTime TimeStamp)
    {
        this.TimeStamp = TimeStamp;
    }

    /**
     * Setter for the type
     * @param Type value to set
     */
    public void setType(String Type)
    {
        this.Type = Type;
    }

    /**
     * Setter for the Address
     * @param Address value to set
     */
    public void setAddress(String Address)
    {
        this.Address = Address;
    }

    /**
     * Setter for the Entry
     * @param Entry value to set
     */
    public void setEntry(String Entry)
    {
        this.Entry = Entry;

    }
    
    @Override
    /**
     * String representation of the entry
     * @return String representation of the entry
     */
    public String toString()
    {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "[" + this.TimeStamp.format(format) + "] " + this.Type + " " + this.Address + ": " + this.Entry + (this.Entry.endsWith("\n") ?"" :"\n");
    }
}
