public class DBEntry
{
    enum Type {DEFAULT, SOASP, SOAADMIN, SOASERIAL, SOAREFRESH, SOARETRY, SOAEXPIRE, NS, A, CNAME, MX, PTR};
    private String param, value;
    private Type type;
    private int TTL, priority;

    public DBEntry (String param, Type type, String value, int TTL, int priority)
    {
        this.param = param;
        this.type = type;
        this.value = value;
        this.TTL = TTL;
        this.priority = priority;
    }

    public DBEntry (String param, Type type, String value, int TTL)
    {
        this.param = param;
        this.type = type;
        this.value = value;
        this.TTL = TTL;
        this.priority = 0;
    }

    public static Type stringToType (String str)
    {
        if (str.equals("DEFAULT")) return Type.DEFAULT;
        if (str.equals("SOASP")) return Type.SOASP;
        if (str.equals("SOAADMIN")) return Type.SOAADMIN;
        if (str.equals("SOASERIAL")) return Type.SOASERIAL;
        if (str.equals("SOAREFRESH")) return Type.SOAREFRESH;
        if (str.equals("SOARETRY")) return Type.SOARETRY;
        if (str.equals("SOAEXPIRE")) return Type.SOAEXPIRE;
        if (str.equals("NS")) return Type.NS;
        if (str.equals("A")) return Type.A;
        if (str.equals("CNAME")) return Type.CNAME;
        if (str.equals("MX")) return Type.MX;
        if (str.equals("PTR")) return Type.PTR;
        return Type.DEFAULT;
    }
}
