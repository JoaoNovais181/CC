import java.util.ArrayList;
import java.util.Collection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementation of a Cache used to store information about DNS Databases/Queries in the context of the CC project.
 * 
 * <p> Since it is going to be used by multiple threads, this class is made to be safe in that context </p>
 * 
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class Cache
{
    /**
     * Static {@link CacheEntry} array used to store the entries in the cache
     */
    private CacheEntry[] values;

    /**
     * Constructs a new cache with capacity of {@code size}
     * @param size Capacity of the cache
     */
    public Cache (int size)
    {
        this.values = new CacheEntry[size];
        for (int i=0 ; i<this.values.length ; i++)
            this.values[i] = new CacheEntry();
    }

    /**
     * Method used to put a entry that comes from a Servers Database File or a ZoneTransfer, from the Primary Server
     * @param entry {@link CacheEntry} to put into the {@code Cache}
     * @return {@code boolean} that indicates whether the entry was successfully put into the Cache or not
     */
    private synchronized boolean putFILEorSP (CacheEntry entry)
    {
        for (int i=0 ; i<this.values.length ; i++)
            if (this.values[i].getStatus() == CacheEntry.FREE) { this.values[i] = entry; entry.setIndex(i); entry.setTimeStamp(LocalDateTime.now()); return true;}
        return false;
    }

    /**
     * Method used to put a new entry into the cache
     * @param entry {@link CacheEntry} to put into the {@code Cache}
     * @return {@code boolean} that indicates whether the entry was successfully put into the Cache or not
     */
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

    /**
     * Method to put every entry from a {@link Collection}&lt;{@link CacheEntry}&gt; into the cache
     * @param entries {@link Collection}&lt;{@link CacheEntry}&gt; containing the entries to be put into the cache 
     * @return {@code boolean} that indicates whether every entry was successfully put into the Cache or not
     */
    public synchronized boolean put(Collection<CacheEntry> entries)
    {
        boolean r = true;
        for (CacheEntry entry : entries)
            r = r && this.put(entry);
        return r;
    }

    /**
     * Method to search a entry in the cache with the same name and type given as arguments, starting on the Index given also as an argument 
     * @param Name Name the entry should have
     * @param Type Type the entry should have
     * @param Index Index to start the search at
     * @return -1 if the entry is not found and it's index in case it is
     */
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

    /**
     * Method to get the {@link CacheEntry} located at the specified index
     * @param index Index of the {@link CacheEntry} to retrieve
     * @return the {@link CacheEntry} located at the specified index
     */
    public synchronized CacheEntry get(int index)
    {
        if (this.values[index].getStatus()==CacheEntry.VALID) return this.values[index];
        return null;
    }

    /**
     * Method used to get a {@link List}&lt;{@link CacheEntry}&gt; containing the entries with the best possible suffix of Name and
     * of type given as argument.
     * @param Name Name the entries should be a suffix to
     * @param Type Type the entries should have
     * @return a list of the entries with the best possible suffix
     */
    public synchronized List<CacheEntry> get (String Name, String Type)
    {
        String currentMatch = null;
        ArrayList<CacheEntry> r = new ArrayList<>();
        boolean cname = Type.equals("CNAME");
        for (CacheEntry curr : this.values) 
        {
            if (curr.getStatus() == CacheEntry.FREE) continue;
            String currName = (cname) ?curr.getValue() :curr.getName();
            String currType = curr.getType();
            if (currentMatch == null)
            {
                if (Name.endsWith(currName) && currType.equals(Type))
                    currentMatch = currName;
            }    
            else if (Name.endsWith(currName) && currName.length() > currentMatch.length() && currType.equals(Type))
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

    /**
     * Method used to retrieve all the {@link CacheEntry} with the specified origin
     * @param origin the origin the entries should have
     * @return List of {@link CacheEntry} of the specified origin
     */
    public synchronized List<CacheEntry> getEntriesByOrigin(String origin)
    {
        ArrayList<CacheEntry> r = new ArrayList<>();
        for (CacheEntry curr : this.values)
            if (curr.getOrigin().equals(origin))
                r.add(curr);
        return r;
    }

    /**
     * Method used to remove all the {@link CacheEntry} with the specified origin
     * @param origin origin the of cache entries to remove
     */
    public synchronized void removeEntries(String origin)
    {
        for (CacheEntry curr : this.values)
            if (curr.getOrigin().equals(origin))
                curr.setStatus(CacheEntry.FREE);
    }

    @Override
    /**
     * Method used to get a Textual representation of the Cache
     * @return Textual representation of the Cache
     */
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