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
        this.Value = Value;
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
        this.Value = Value;
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
        this.Value = Value;
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
        this.Status = FREE;
    }

    public synchronized String getName() {return this.Name;}
    
    public synchronized String getType() {return this.Type;}
    
    public synchronized String getValue() {return this.Value;}
    
    public synchronized int getttl() {return this.TTL;}
    
    public synchronized int getOrder() {return this.Order;}
    
    public synchronized String getOrigin() {return this.Origin;}
    
    public synchronized LocalDateTime getTimeStamp() {return this.TimeStamp;}

    public synchronized int getIndex() {return this.Index;}

    /** 
     * 
     * @return  true if Free
     * @return  false if Valid
     */
    public synchronized boolean getStatus() {return this.Status;}

    public synchronized void setIndex(int Index) { this.Index = Index; }

    public synchronized void setStatus(boolean Status) { this.Status = Status; }

    public synchronized void setTimeStamp(LocalDateTime TimeStamp) { this.TimeStamp = TimeStamp; }

    public synchronized String dbString()
    {
        return "" + this.Name + " " + this.Type + " " + this.Value + ((this.TTL>0) ?" "+this.TTL :"") + ((this.Order>0) ?" "+this.Order :"");
    }

    @Override
    public synchronized String toString() 
    {
        return "" + this.Name + " " + this.Type + " " + this.Value + " " + this.TTL + " " + this.Order + " " + this.Origin + " " + this.TimeStamp.toString() + " " + this.Index + " " + ((this.Status) ?"FREE" :"VALID");
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (obj.getClass() != this.getClass()) return false;
        CacheEntry ce = (CacheEntry) obj;
        return this.Name.equals(ce.getName()) && this.Type.equals(ce.getType()) && this.Value.equals(ce.getValue()) && this.TTL==ce.getttl() &&
                this.Order==ce.getOrder() && this.Origin.equals(ce.getOrigin()) && this.TimeStamp.equals(ce.getTimeStamp()) && this.Status==ce.getStatus(); 
    }
}
