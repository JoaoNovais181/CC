import java.util.Collection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Cache
{
    private CacheEntry[] values;
    private CCLogger logger;

    
    public Cache (int size)
    {
        this.values = new CacheEntry[size];
        for (int i=0 ; i<this.values.length ; i++)
            this.values[i] = new CacheEntry();
    }

    private boolean putFILEorSP (CacheEntry entry)
    {
        for (int i=0 ; i<this.values.length ; i++)
            if (this.values[i].getStatus()) { this.values[i] = entry; entry.setIndex(i); entry.setTimeStamp(LocalDateTime.now()); return true;}
        return false;
    }

    public boolean put(CacheEntry entry)
    {
        if ("FILE SP".indexOf(entry.getType()) != -1)
            return putFILEorSP(entry);
        
        int i = this.get(entry.getName(), entry.getType(), 1);
        if (i != -1)
        {
            CacheEntry existing = this.values[i];
            if ("FILE SP".indexOf(existing.getOrigin()) != -1)
                return true;
            
            existing.setTimeStamp(LocalDateTime.now());
            existing.setStatus(CacheEntry.VALID);
            return true;
        }

        for (int j=0 ; j<this.values.length ; j++)
            if (this.values[i].getStatus()) 
                i=j;

        if (i==-1) return false;
        this.values[i] = entry; 
        entry.setIndex(i);
        entry.setTimeStamp(LocalDateTime.now()); 
        return true;
    }

    public boolean put(Collection<CacheEntry> entries)
    {
        boolean r = true;
        for (CacheEntry entry : entries)
            r = r && this.put(entry);
        return r;
    }

    public int get(String Name, String Type, int Index)
    {
        int r = -1;
        for (int i=0 ; i<this.values.length ; i++)
        {
            int index = Index+i;
            CacheEntry curr = this.values[index]; 
            if (curr.getName().equals(Name) && curr.getType().equals(Type)) r = index;

            if (curr.getOrigin().equals("OTHERS") && curr.getttl()>0 && ChronoUnit.SECONDS.between(curr.getTimeStamp(), LocalDateTime.now()) > curr.getttl())
                curr.setStatus(CacheEntry.FREE);
        }

        return r;
    }

    @Override
    public String toString()
    {
        String r = "";
        int free = 0;
        for (CacheEntry entry : this.values)
        {
            if (entry.getStatus())
                free++;
            else
                r += entry.toString() + "\n";
        }
        r += "and " + free + " Free entries";

        return r;
    }

}