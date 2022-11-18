public class MyAppProto 
{
    private Header header;
    private DataField dataField;
    private boolean clientMSG;

    public MyAppProto(String MsgID, String Flags, String Name, String TypeOfValue)
    {
        this.header = new Header(MsgID, Flags);
        this.dataField = new DataField(Name, TypeOfValue);
        this.clientMSG = true;
    }

    public MyAppProto(String Msg, String Flags, int responseCode, int numberOfValues, int numberOfAuthorities, int numberOfExtraValues, String Name, String TypeOfValue, int nrv, int nav, int nev)
    {
        this.header = new Header(Msg, Flags, responseCode, numberOfValues, numberOfAuthorities, numberOfExtraValues);
        this.dataField = new DataField(Name, TypeOfValue, nrv, nav, nev);
        this.clientMSG = false;
    }

    public void PutValue (String value) {this.dataField.PutValue(value);}
    public void PutAuthority (String value) {this.dataField.PutAuthority(value);}
    public void PutExtraValue (String value) {this.dataField.PutExtraValue(value);}

    @Override
    public String toString()
    {
        return this.header.toString() + this.dataField.toString();
    }

    public static void main (String[] args)
    {
        MyAppProto m = new MyAppProto("1", "Q+R", "nestle.sonso.", "NS");
        System.out.println(m);
        MyAppProto r = new MyAppProto("1", "Q+R", 0, 3, 3, 6, "nestle.sonso.", "NS", 3, 3, 6);
        r.PutValue("nestle.sonso. NS ns1.nestle.sonso. 86400");
        r.PutValue("nestle.sonso. NS ns2.nestle.sonso. 86400");
        r.PutValue("nestle.sonso. NS ns3.nestle.sonso. 86400");
        r.PutAuthority("nestle.sonso. NS ns1.nestle.sonso. 86400");
        r.PutAuthority("nestle.sonso. NS ns2.nestle.sonso. 86400");
        r.PutAuthority("nestle.sonso. NS ns3.nestle.sonso. 86400");
        r.PutExtraValue("nestle.sonso. NS ns1.nestle.sonso. 86400");
        r.PutExtraValue("nestle.sonso. NS ns2.nestle.sonso. 86400");
        r.PutExtraValue("nestle.sonso. NS ns3.nestle.sonso. 86400");
        r.PutExtraValue("sp.nestle.sonso. CNAME ns1.nestle.sonso. 86400");
        r.PutExtraValue("ss1.nestle.sonso. CNAME ns2.nestle.sonso. 86400");
        r.PutExtraValue("ss2.nestle.sonso. CNAME ns3.nestle.sonso. 86400");
        System.out.println(r);
    }
}
