import java.util.List;
import java.util.Random;

public class MyAppProto 
{
    private Header header;
    private DataField dataField;

    public MyAppProto(String MsgID, String Flags, String Name, String TypeOfValue)
    {
        this.header = new Header(MsgID, Flags);
        this.dataField = new DataField(Name, TypeOfValue);
    }

    public MyAppProto(String Flags, String Name, String TypeOfValue)
    {
        Random r = new Random(ProcessHandle.current().pid());
        this.header = new Header(""+r.nextInt(65535), Flags);
        this.dataField = new DataField(Name, TypeOfValue);
    }

    public MyAppProto(String Msg, String Flags, int responseCode, int numberOfValues, int numberOfAuthorities, int numberOfExtraValues, String Name, String TypeOfValue)
    {
        this.header = new Header(Msg, Flags, responseCode, numberOfValues, numberOfAuthorities, numberOfExtraValues);
        this.dataField = new DataField(Name, TypeOfValue, numberOfValues, numberOfAuthorities, numberOfExtraValues);
    }

    public MyAppProto(byte[] bytes)
    {
        String msg = new String(bytes).trim();
        String[] parts = msg.split(";");

        this.header = new Header(parts[0]);
        this.dataField = new DataField(parts[1]);

        if (parts.length > 2)
        {
            String[] aux = parts[2].trim().split(",");
            for (String str : aux)
            {
                this.dataField.PutValue(str);
            }
            aux = parts[3].trim().split(",");
            for (String str : aux)
            {
                this.dataField.PutAuthority(str);
            }
            aux = parts[4].trim().split(",");
            for (String str : aux)
            {
                this.dataField.PutExtraValue(str);
            }
        }
    }

    public String getMsgID() { return this.header.getMsgID(); }
    public String getFlags() { return this.header.getFlags(); }
    public int getResponseCode() { return this.header.getResponseCode(); }
    public int getNumberOfValues() { return this.header.getNumberOfValues(); }
    public int getNumberOfAuthorities() { return this.header.getNumberOfAuthorities(); }
    public int getNumberOfExtraValues() { return this.header.getNumberOfExtraValues(); }
    public String getName() { return this.dataField.getName(); }
    public String getTypeOfValue() { return this.dataField.getTypeOfValue(); }
    public List<String> getResponseValues() { return this.dataField.getResponseValues(); }
    public List<String> getAuthoritiesValues() { return this.dataField.getAuthoritiesValues(); }
    public List<String> getExtraValues() { return this.dataField.getExtraValues(); }

    public void setResponseCode(int responseCode) { this.header.setResponseCode(responseCode); }
    public void setMsgID(String MsgID) { this.header.setMsgID(MsgID); }

    public void PutValue (String value) {this.dataField.PutValue(value);}
    public void PutAuthority (String value) {this.dataField.PutAuthority(value);}
    public void PutExtraValue (String value) {this.dataField.PutExtraValue(value);}

    @Override
    public String toString()
    {
        return this.header.toString() + this.dataField.toString();
    }

    public String prettyString()
    {
        return this.header.prettyString() + this.dataField.prettyString();
    }


//    public static void main (String[] args)
//    {
//        MyAppProto m = new MyAppProto("1", "Q+R", "nestle.sonso.", "NS");
//        System.out.println(m);
//        MyAppProto r = new MyAppProto("1", "Q+R", 0, 3, 3, 6, "nestle.sonso.", "NS", 3, 3, 6);
//        r.PutValue("nestle.sonso. NS ns1.nestle.sonso. 86400");
//        r.PutValue("nestle.sonso. NS ns2.nestle.sonso. 86400");
//        r.PutValue("nestle.sonso. NS ns3.nestle.sonso. 86400");
//        r.PutAuthority("nestle.sonso. NS ns1.nestle.sonso. 86400");
//        r.PutAuthority("nestle.sonso. NS ns2.nestle.sonso. 86400");
//        r.PutAuthority("nestle.sonso. NS ns3.nestle.sonso. 86400");
//        r.PutExtraValue("nestle.sonso. NS ns1.nestle.sonso. 86400");
//        r.PutExtraValue("nestle.sonso. NS ns2.nestle.sonso. 86400");
//        r.PutExtraValue("nestle.sonso. NS ns3.nestle.sonso. 86400");
//        r.PutExtraValue("sp.nestle.sonso. CNAME ns1.nestle.sonso. 86400");
//        r.PutExtraValue("ss1.nestle.sonso. CNAME ns2.nestle.sonso. 86400");
//        r.PutExtraValue("ss2.nestle.sonso. CNAME ns3.nestle.sonso. 86400");
//        System.out.println(r);
//
//        String[] test = "asdasd;\n;\n;asdasd;".split(";");
//        for (String str : test) System.out.println(Arrays.toString(str.replace("\n", "").split(",")));
//        System.out.println(Arrays.toString(test));
//    }
}
