import java.time.LocalDateTime;

public class CacheEntry
{
    // private static String[] validTypes = {"DEFAULT", "SOASP", "SOAADMIN", "SOASERIAL", "SOAREFRESH", "SOARETRY", "SOAEXPIRE", "NS", "A", "CNAME", "MX", "PTR"};
    public static boolean VALID = false, FREE = true;
    private String Name, Value, Type, Origin;
    private int TTL, Order, Index;
    private LocalDateTime TimeStamp;
    private boolean Status; // false == Valid true == Free

    public CacheEntry (String Name, String Type, String Value, int TTL, int Order, String Origin) throws InvalidCacheEntryException
    {
        this.Name = Name;
        this.Type = Type;
        this.Value = "";
        this.TTL  = TTL;
        this.Order = Order;
        if ("OTHERS FILE SP".indexOf(Origin)==-1)
            throw new InvalidCacheEntryException("Origin field " + Origin + " is invalid\n");
        this.Origin = Origin;
        this.TimeStamp = null;
        this.Index = 0;
        this.Status = VALID;
    }

    public CacheEntry (String Name, String Type, String Value, int TTL, String Origin) throws InvalidCacheEntryException
    {
        this.Name = Name;
        this.Type = Type;
        this.Value = "";
        this.TTL  = TTL;
        this.Order = 0;
        if ("OTHERS FILE SP".indexOf(Origin)==-1)
            throw new InvalidCacheEntryException("Origin field " + Origin + " is invalid\n");
        this.Origin = Origin;
        this.TimeStamp = null;
        this.Index = 0;
        this.Status = VALID;
    }


    public CacheEntry (String Name, String Type, String Value, String Origin) throws InvalidCacheEntryException
    {
        this.Name = Name;
        this.Type = Type;
        this.Value = "";
        this.TTL  = 0;
        this.Order = 0;
        if ("OTHERS FILE SP".indexOf(Origin)==-1)
            throw new InvalidCacheEntryException("Origin field " + Origin + " is invalid\n");
        this.Origin = Origin;
        this.TimeStamp = null;
        this.Index = 0;
        this.Status = VALID;
    }

    public CacheEntry ()
    {
        this.Name = "";
        this.Type = "";
        this.Value = "";
        this.TTL  = 0;
        this.Order = 0;
        this.Origin = "";
        this.TimeStamp = LocalDateTime.now();
        this.Index = 0;
        this.Status = true;
    }

    public String getName() {return this.Name;}
    
    public String getType() {return this.Type;}
    
    public String getValue() {return this.Value;}
    
    public int getttl() {return this.TTL;}
    
    public int getOrder() {return this.Order;}
    
    public String getOrigin() {return this.Origin;}
    
    public LocalDateTime getTimeStamp() {return this.TimeStamp;}

    public int getIndex() {return this.Index;}

    /** 
     * 
     * @return  true if Free
     * @return  false if Valid
     */
    public boolean getStatus() {return this.Status;}

    public void setIndex(int Index) { this.Index = Index; }

    public void setStatus(boolean Status) { this.Status = Status; }

    public void setTimeStamp(LocalDateTime TimeStamp) { this.TimeStamp = TimeStamp; }

    @Override
    public String toString() 
    {
        return "" + this.Name + " " + this.Type + " " + this.Value + " " + this.TTL + " " + this.Order + " " + this.TimeStamp.toString() + " " + this.Index + " " + ((this.Status) ?"FREE" :"VALID");
    }
}