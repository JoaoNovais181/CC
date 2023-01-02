import java.util.List;
import java.util.Random;

/**
 * Implementation of the protocol to use on UDP messages
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class MyAppProto 
{
    /**
     * {@link Header} of the protocol
     */
    private Header header;
    /**
     * {@link DataField} of the protocol.
     */
    private DataField dataField;

    /**
     * Constructor with the specified values
     * @param MsgID message id
     * @param Flags flags
     * @param Name name
     * @param TypeOfValue typeofvalue
     */
    public MyAppProto(String MsgID, String Flags, String Name, String TypeOfValue)
    {
        this.header = new Header(MsgID, Flags);
        this.dataField = new DataField(Name, TypeOfValue);
    }

    /**
     * Constructor with the specified values
     * @param Flags flags
     * @param Name name
     * @param TypeOfValue type of value
     */
    public MyAppProto(String Flags, String Name, String TypeOfValue)
    {
        Random r = new Random(ProcessHandle.current().pid());
        this.header = new Header(""+r.nextInt(65535), Flags);
        this.dataField = new DataField(Name, TypeOfValue);
    }

    /**
     * Constructor with the specified values
     * @param Msg message id
     * @param Flags flags
     * @param responseCode response code
     * @param numberOfValues number of values
     * @param numberOfAuthorities number of authorities
     * @param numberOfExtraValues number of extra values
     * @param Name name
     * @param TypeOfValue type of value
     */
    public MyAppProto(String Msg, String Flags, int responseCode, int numberOfValues, int numberOfAuthorities, int numberOfExtraValues, String Name, String TypeOfValue)
    {
        this.header = new Header(Msg, Flags, responseCode, numberOfValues, numberOfAuthorities, numberOfExtraValues);
        this.dataField = new DataField(Name, TypeOfValue, numberOfValues, numberOfAuthorities, numberOfExtraValues);
    }

    /**
     * Constructor from a byte[]
     * @param bytes byte array to construct from
     */
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

    /**
     * Getter for the Message ID
     * @return Message ID
     */
    public String getMsgID() { return this.header.getMsgID(); }
    /**
     * Getter for the flags 
     * @return Flags
     */
    public String getFlags() { return this.header.getFlags(); }
    /**
     * Getter for the response code
     * @return Response Code
     */
    public int getResponseCode() { return this.header.getResponseCode(); }
    /**
     * Getter for the number of values
     * @return Number of values
     */
    public int getNumberOfValues() { return this.header.getNumberOfValues(); }
    /**
     * Getter for the number of authorities
     * @return Number of authorities
     */
    public int getNumberOfAuthorities() { return this.header.getNumberOfAuthorities(); }
    /**
     * Getter for the number of extra values
     * @return number of extra values
     */
    public int getNumberOfExtraValues() { return this.header.getNumberOfExtraValues(); }
    /**
     * Getter for the name
     * @return Name
     */
    public String getName() { return this.dataField.getName(); }
    /**
     * Getter for the Type of Value
     * @return Type of Value
     */
    public String getTypeOfValue() { return this.dataField.getTypeOfValue(); }
    /**
     * Getter for the response values
     * @return Response Values
     */
    public List<String> getResponseValues() { return this.dataField.getResponseValues(); }
    /**
     * Getter for the Authorities Values
     * @return Authorities Values
     */
    public List<String> getAuthoritiesValues() { return this.dataField.getAuthoritiesValues(); }
    /**
     * Getter for the Extra Values
     * @return Extra Values
     */
    public List<String> getExtraValues() { return this.dataField.getExtraValues(); }

    /**
     * Setter for the Response Code
     * @param responseCode Response Code to set
     */
    public void setResponseCode(int responseCode) { this.header.setResponseCode(responseCode); }
    /**
     * Setter for the Message ID
     * @param MsgID Message ID to set
     */
    public void setMsgID(String MsgID) { this.header.setMsgID(MsgID); }

    /**
     * Method to put a response value
     * @param value value to put
     */
    public void PutValue (String value) {this.dataField.PutValue(value);}
    
    /**
     * Method to put a authority value
     * @param value authority to put
     */
    public void PutAuthority (String value) {this.dataField.PutAuthority(value);}

    /**
     * Method to put a extra value
     * @param value extra value to put
     */
    public void PutExtraValue (String value) {this.dataField.PutExtraValue(value);}

    @Override
    /**
     * Returns a String representation
     * @return String representation
     */
    public String toString()
    {
        return this.header.toString() + this.dataField.toString();
    }

    /**
     * Returns a Pretty String representation
     * @return Pretty String representation
     */
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
