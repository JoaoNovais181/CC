import java.time.LocalDateTime;

/**
 * Implementation of {@code CacheEntry}, a class that represents a entry on the {@link Cache} implementation of a lookup cache in the
 * context of this project.
 * 
 * <p> This class is going to be used in a multi-threaded environment, so it should implement some Mutual Exclusion clauses so that
 * the accesses to the cache are protected </p>
 * 
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class CacheEntry
{
    /**
     * static definition of {@code Valid} and {@code FREE}, booleans that represent the status of a {@code CacheEntry}
     */
    public static boolean VALID = false, FREE = true;
    /**
     * Strings that represent the Name, Value, Type and Origin of a {@code CacheEntry}
     */
    private String Name, Value, Type, Origin;
    /**
     * integers that represent the TTL, Order and Index of a {@code CacheEntry} in the {@link Cache}
     */
    private int TTL, Order, Index;
    /**
     * {@link LocalDateTime} representing the time on which the CacheEntry was added to the {@link Cache}
     */
    private LocalDateTime TimeStamp;
    /**
     * boolean indicating the status of the {@code CacheEntry}
     */
    private boolean Status; // false == Valid true == Free

    /**
     * Creates an instance of {@link CacheEntry} with the specified parameters
     * @param Name Name of the entry
     * @param Type Type of the entry
     * @param Value Value of the entry
     * @param TTL TTL of the entry
     * @param Order Order of the entry
     * @param Origin Origin of the entry
     * @throws InvalidCacheEntryException if the entry has an invalid Origin
     */
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

    /**
     * Creates an instance of {@link CacheEntry} with the specified parameters
     * @param Name Name of the entry
     * @param Type Type of the entry
     * @param Value Value of the entry
     * @param TTL TTL of the entry
     * @param Origin Origin of the entry
     * @throws InvalidCacheEntryException
     */
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

    /**
     * Creates an instance of {@link CacheEntry} with the specified parameters
     * @param Name Name of the entry
     * @param Type Type of the entry
     * @param Value Value of the entry
     * @param Origin Origin of the entry
     * @throws InvalidCacheEntryException
     */
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

    /**
     * Creates a default {@code CacheEntry}
     */
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

    /**
     * Method used to retrieve the value of Name
     * @return the value of Name
     */
    public synchronized String getName() {return this.Name;}
    
    /**
     * Method used to retrieve the value of Type
     * @return the value of Type
     */
    public synchronized String getType() {return this.Type;}
    
    /**
     * Method used to retrieve the value of Value
     * @return the value of Value
     */
    public synchronized String getValue() {return this.Value;}
    
    /**
     * Method used to retrieve the value of TTL
     * @return the value of TTL
     */
    public synchronized int getttl() {return this.TTL;}
    
    /**
     * Method used to retrieve the value of Order
     * @return the value of Order
     */
    public synchronized int getOrder() {return this.Order;}
    
    /**
     * Method used to retrieve the value of Origin
     * @return the value of Origin
     */
    public synchronized String getOrigin() {return this.Origin;}
    
    /**
     * Method used to retrieve the value of TimeStamp
     * @return the value of TimeStamp
     */
    public synchronized LocalDateTime getTimeStamp() {return this.TimeStamp;}

    /**
     * Method used to retrieve the value of Index
     * @return the value of Index
     */
    public synchronized int getIndex() {return this.Index;}

    /** 
     * Method used to retrieve the value of Status
     * @return  true if Free or false if Valid
     */
    public synchronized boolean getStatus() {return this.Status;}

    /**
     * Method used to set the value of Index
     * @param Index Value to set to Index
     */
    public synchronized void setIndex(int Index) { this.Index = Index; }

    /**
     * Method used to set the value of Index
     * @param Status Value to set to Status
     */
    public synchronized void setStatus(boolean Status) { this.Status = Status; }

    /**
     * Method used to set the value of TimeStamp
     * @param TimeStamp Value to set to TimeStamp
     */
    public synchronized void setTimeStamp(LocalDateTime TimeStamp) { this.TimeStamp = TimeStamp; }

    /**
     * Returns a string used to send the entry via TCP
     * @return string used to send the entry via TCP
     */
    public synchronized String tcpString()
    {
        return "" + this.Name + ";" +
                    this.Type + ";" +
                    this.Value + ";" +
                    this.TTL + ";" +
                    this.Order;
    }

    /**
     * Returns the string representation of the entry in the database
     * @return string representation of the entry in the database
     */
    public synchronized String dbString()
    {
        return "" + this.Name + " " + this.Type + " " + this.Value + ((this.TTL>0) ?" "+this.TTL :"") + ((this.Order>0) ?" "+this.Order :"");
    }

    @Override
    /**
     * Returns a string representation of the entry
     * @return string representation of the entry
     */
    public synchronized String toString() 
    {
        return "" + this.Name + " " + this.Type + " " + this.Value + " " + this.TTL + " " + this.Order + " " + this.Origin + " " + this.TimeStamp.toString() + " " + this.Index + " " + ((this.Status) ?"FREE" :"VALID");
    }

    @Override
    /**
     * Method used to check if an entry is equal to another object
     * @param obj Object to compare
     * @return boolean indicating if the objects are equal
     */
    public synchronized boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (obj.getClass() != this.getClass()) return false;
        CacheEntry ce = (CacheEntry) obj;
        return this.Name.equals(ce.getName()) && this.Type.equals(ce.getType()) && this.Value.equals(ce.getValue()) && this.TTL==ce.getttl() &&
                this.Order==ce.getOrder() && this.Origin.equals(ce.getOrigin()) && this.TimeStamp.equals(ce.getTimeStamp()) && this.Status==ce.getStatus(); 
    }
}
