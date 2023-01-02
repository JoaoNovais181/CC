import java.nio.ByteBuffer;

/**
 * Implementation of {@code Header}
 *
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class Header
{
    /**
     * Message id and flags of the querie
     */
    private String MsgID, Flags;
    /**
     * Response code, number of values, authorities and extra values
     */
    private int responseCode, numberOfValues, numberOfAuthorities, numberOfExtraValues;

    /**
     * Constructs a header using the parameters
     * @param MsgID
     * @param Flags
     */
    public Header(String MsgID, String Flags)
    {
        this.MsgID = MsgID;
        this.Flags = Flags;
        this.responseCode = 0;
        this.numberOfValues = 0;
        this.numberOfAuthorities = 0;
        this.numberOfExtraValues = 0;
    }

    /**
     * Constructs a header using the specified parameters
     * @param MsgID
     * @param Flags
     * @param responseCode
     * @param numberOfValues
     * @param numberOfAuthorities
     * @param numberOfExtraValues
     */
    public Header(String MsgID, String Flags, int responseCode, int numberOfValues, int numberOfAuthorities, int numberOfExtraValues)
    {
        this.MsgID = MsgID;
        this.Flags = Flags;
        this.responseCode = responseCode;
        this.numberOfValues = numberOfValues;
        this.numberOfAuthorities = numberOfAuthorities;
        this.numberOfExtraValues = numberOfExtraValues;
    }

    /**
     * Constructs a header using the string representation of a querie
     * @param paramsString
     */
    public Header(String paramsString)
    {
        String[] params = paramsString.split(",");
        this.MsgID = params[0];
        this.Flags = params[1];
        this.responseCode = Integer.parseInt(params[2]);
        this.numberOfValues = Integer.parseInt(params[3]);
        this.numberOfAuthorities = Integer.parseInt(params[4]);
        this.numberOfExtraValues = Integer.parseInt(params[5]);
    }

    /**
     * Method to get the Message ID
     * @return Message ID
     */
    public String getMsgID() { return this.MsgID; }
    /**
     * Method to get the Flags
     * @return Flags
     */
    public String getFlags() { return this.Flags; }
    /**
     * Method to get the Response Code
     * @return Response Code
     */
    public int getResponseCode() { return this.responseCode; }
    /**
     * Method used to get the number of values
     * @return the number of values
     */
    public int getNumberOfValues() { return this.numberOfValues; }
    /**
     * Method used to get the number of authorities values
     * @return the number of authorities values
     */
    public int getNumberOfAuthorities() { return this.numberOfAuthorities; }
    /**
     * Method used to get the number of extra values
     * @return the number of extra values
     */
    public int getNumberOfExtraValues() { return this.numberOfExtraValues; }

    // msgid -> 16 bits, flags -> 4 bits  (podia ser 2 mas nao consigo fazer com 2 por enquanto), rest -> 8 bits
    // public byte[] Encode()
    // {
    //     ByteBuffer bb = ByteBuffer.allocate(7);
    //     short id = Short.valueOf(this.MsgID);
    //     bb.putShort(id);
        
    //     byte flags;
    //     if (this.Flags.equals("Q")) flags = 0b001;        // se flag for Q entao fica com o valor 001 em binario
    //     else if (this.Flags.equals("R")) flags = 0b010;   // se flag for R entao fica com o valor 010 em binario
    //     else if (this.Flags.equals("A")) flags = 0b100;   // se flag for A entao fica com o valor 100 em binario
    //     else if (this.Flags.equals("Q+R")) flags = 0b011;   // se flag for Q+R entao fica com o valor 011 em binario
    //     else return null;   // mudar isto 
    //     bb.put(flags);

    //     byte rc = (byte)this.responseCode;
    //     byte nov = (byte)(this.numberOfValues - 127), noa = (byte)(this.numberOfAuthorities - 127), noev = (byte)(this.numberOfExtraValues - 127);
    //     bb.put(rc);
    //     bb.put(nov);
    //     bb.put(noa);
    //     bb.put(noev);


    //     return bb.array();
    // }

    // public static Header Decode (byte[] encodedHeader)
    // {
    //     String MsgId, Flags;
    //     int responseCode, numberOfValues, numberOfAuthorities, numberOfExtraValues;

    //     short id = ByteBuffer.wrap(new byte[]{encodedHeader[0], encodedHeader[1]}).getShort();
    //     MsgId = "" + id;

    //     if (encodedHeader[2]==0b001) Flags = "Q";
    //     else if (encodedHeader[2]==0b010) Flags = "R";
    //     else if (encodedHeader[2]==0b100) Flags = "A";
    //     else if (encodedHeader[2]==0b011) Flags = "Q+R";
    //     else return null; // mudar isto

    //     responseCode = (int)encodedHeader[3];
    //     numberOfValues = (int)encodedHeader[4] + 127;
    //     numberOfAuthorities = (int)encodedHeader[5] + 127;
    //     numberOfExtraValues = (int)encodedHeader[6] + 127;

    //     return new Header(MsgId, Flags, responseCode, numberOfValues, numberOfAuthorities, numberOfExtraValues);
    // }

    @Override
    /**
     * Method used to get a String representation of the Header
     * @return a String representation of the Header
     */
    public String toString()
    {
        return  this.MsgID + "," + this.Flags + "," + this.responseCode + ","
                + this.numberOfValues + "," + this.numberOfAuthorities + "," + this.numberOfExtraValues + ";";
    }

    /**
     * Method used to get a prettier String representation of the Header
     * @return a prettier String representation of the Header
     */
    public String prettyString()
    {
        return "# Header\nMESSAGE-ID = " + this.MsgID + ", FLAGS = " + this.Flags + ", RESPONSE-CODE = " + this.responseCode + ", N-VALUES = " + this.numberOfValues + 
                ", N-AUTHORITIES = " + this.numberOfAuthorities + ", N-EXTRA-VALUES = " + this.numberOfExtraValues + ";\n";
    }

    /**
     * Method used to set the value of the response code
     * @param responseCode value to set the response code
     */
    public void setResponseCode(int responseCode) { this.responseCode = responseCode; }
    /**
     * Method used to set the message ID
     * @param MsgID the message ID to set
     */
    public void setMsgID(String MsgID) { this.MsgID = MsgID; }
}