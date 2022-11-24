import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry 
{
    private LocalDateTime TimeStamp;
    private String Type, Address, Entry;

    public LogEntry(String Type, String  Address, String Entry)
    {
        this.Type = Type;
        this.Address = Address;
        this.Entry = Entry;
        this.TimeStamp = LocalDateTime.now();
    }

    public String getType()
    {
        return this.Type;
    }

    public String getAddress()
    {
        return this.Address;
    }

    public String getEntry()
    {
        return this.Entry;
    }

    public LocalDateTime getTimeStamp()
    {
        return this.TimeStamp;
    }

    public void setTimeStamp(LocalDateTime TimeStamp)
    {
        this.TimeStamp = TimeStamp;
    }

    public void setType(String Type)
    {
        this.Type = Type;
    }

    public void setAddress(String Address)
    {
        this.Address = Address;
    }

    public void setEntry(String Entry)
    {
        this.Entry = Entry;

    }
    
    @Override
    public String toString()
    {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "[" + this.TimeStamp.format(format) + "] " + this.Type + " " + this.Address + ": " + this.Entry + (this.Entry.endsWith("\n") ?"" :"\n");
    }
}
