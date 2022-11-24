import java.util.ArrayList;
import java.util.Collection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Cache
{
    private CacheEntry[] values;

    
    public Cache (int size)
    {
        this.values = new CacheEntry[size];
        for (int i=0 ; i<this.values.length ; i++)
            this.values[i] = new CacheEntry();
    }

    private boolean putFILEorSP (CacheEntry entry)
    {
        for (int i=0 ; i<this.values.length ; i++)
            if (this.values[i].getStatus() == CacheEntry.FREE) { this.values[i] = entry; entry.setIndex(i); entry.setTimeStamp(LocalDateTime.now()); return true;}
        return false;
    }

    public boolean put(CacheEntry entry)
    {
        if ("FILE SP".contains(entry.getOrigin()))
            return putFILEorSP(entry);

        int i = this.searchValid(entry.getName(), entry.getType(), 1);
        if (i != -1)
        {
            CacheEntry existing = this.values[i];
            if ("FILE SP".contains(existing.getOrigin()))
                return false;
            
            existing.setTimeStamp(LocalDateTime.now());
            existing.setStatus(CacheEntry.VALID);
            return true;
        }

        for (int j=0 ; j<this.values.length ; j++)
            if (this.values[j].getStatus()) 
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

    public int searchValid(String Name, String Type, int Index)
    {
        int r = -1;
        for (int i=0 ; i<this.values.length ; i++)
        {
            int index = (Index+i)%this.values.length;
            CacheEntry curr = this.values[index]; 
            if (curr.getName().equals(Name) && curr.getType().equals(Type)) r = index;

            if (curr.getOrigin().equals("OTHERS") && curr.getttl()>0 && ChronoUnit.SECONDS.between(curr.getTimeStamp(), LocalDateTime.now()) > curr.getttl())
                curr.setStatus(CacheEntry.FREE);
        }

        return r;
    }

    public CacheEntry get(int index)
    {
        if (this.values[index].getStatus()==CacheEntry.VALID) return this.values[index];
        return null;
    }

    public List<CacheEntry> get (String Name, String Type)
    {
        ArrayList<CacheEntry> r = new ArrayList<>();
        for (CacheEntry curr : this.values) {
            if (curr.getName().equals(Name) && curr.getType().equals(Type)) r.add(curr);

            if (curr.getOrigin().equals("OTHERS") && curr.getttl() > 0 && ChronoUnit.SECONDS.between(curr.getTimeStamp(), LocalDateTime.now()) > curr.getttl())
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