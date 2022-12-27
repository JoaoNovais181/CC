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

    private synchronized boolean putFILEorSP (CacheEntry entry)
    {
        for (int i=0 ; i<this.values.length ; i++)
            if (this.values[i].getStatus() == CacheEntry.FREE) { this.values[i] = entry; entry.setIndex(i); entry.setTimeStamp(LocalDateTime.now()); return true;}
        return false;
    }

    public synchronized boolean put(CacheEntry entry)
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

    public synchronized boolean put(Collection<CacheEntry> entries)
    {
        boolean r = true;
        for (CacheEntry entry : entries)
            r = r && this.put(entry);
        return r;
    }

    public synchronized int searchValid(String Name, String Type, int Index)
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

    public synchronized CacheEntry get(int index)
    {
        if (this.values[index].getStatus()==CacheEntry.VALID) return this.values[index];
        return null;
    }

    public synchronized List<CacheEntry> get (String Name, String Type)
    {
        String currentMatch = null;
        ArrayList<CacheEntry> r = new ArrayList<>();
        for (CacheEntry curr : this.values) 
        {
            if (curr.getStatus() == CacheEntry.FREE) continue;
            String currName = curr.getName();
            if (currentMatch == null)
            {
                if (Name.endsWith(currName))
                    currentMatch = currName;
            }    
            else if (Name.endsWith(currName) && currName.length() > currentMatch.length())
            {
                r.clear();
                currentMatch = currName;
            }
            
            if (currentMatch != null && currName.equals(currentMatch) && curr.getType().equals(Type))
            {
                r.add(curr);
            }

            if (curr.getOrigin().equals("OTHERS") && curr.getttl() > 0 && ChronoUnit.SECONDS.between(curr.getTimeStamp(), LocalDateTime.now()) > curr.getttl())
                curr.setStatus(CacheEntry.FREE);
        }

        return r;
    }

    public synchronized List<CacheEntry> getEntriesByOrigin(String origin)
    {
        ArrayList<CacheEntry> r = new ArrayList<>();
        for (CacheEntry curr : this.values)
            if (curr.getOrigin().equals(origin))
                r.add(curr);
        return r;
    }

    public synchronized void removeEntries(String origin)
    {
        for (CacheEntry curr : this.values)
            if (curr.getOrigin().equals(origin))
                curr.setStatus(CacheEntry.FREE);
    }

    @Override
    public synchronized String toString()
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