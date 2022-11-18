import java.nio.ByteBuffer;

public class Header
{
    private String MsgID, Flags;
    private int responseCode, numberOfValues, numberOfAuthorities, numberOfExtraValues;

    public Header(String MsgID, String Flags)
    {
        this.MsgID = MsgID;
        this.Flags = Flags;
        this.responseCode = 0;
        this.numberOfValues = 0;
        this.numberOfAuthorities = 0;
        this.numberOfExtraValues = 0;
    }

    public Header(String MsgID, String Flags, int responseCode, int numberOfValues, int numberOfAuthorities, int numberOfExtraValues)
    {
        this.MsgID = MsgID;
        this.Flags = Flags;
        this.responseCode = responseCode;
        this.numberOfValues = numberOfValues;
        this.numberOfAuthorities = numberOfAuthorities;
        this.numberOfExtraValues = numberOfExtraValues;
    }

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

    // msgid -> 16 bits, flags -> 4 bits  (podia ser 2 mas nao consigo fazer com 2 por enquanto), rest -> 8 bits
    public byte[] Encode()
    {
        ByteBuffer bb = ByteBuffer.allocate(7);
        short id = Short.valueOf(this.MsgID);
        bb.putShort(id);
        
        byte flags;
        if (this.Flags.equals("Q")) flags = 0b001;        // se flag for Q entao fica com o valor 001 em binario
        else if (this.Flags.equals("R")) flags = 0b010;   // se flag for R entao fica com o valor 010 em binario
        else if (this.Flags.equals("A")) flags = 0b100;   // se flag for A entao fica com o valor 100 em binario
        else if (this.Flags.equals("Q+R")) flags = 0b011;   // se flag for Q+R entao fica com o valor 011 em binario
        else return null;   // mudar isto 
        bb.put(flags);

        byte rc = (byte)this.responseCode;
        byte nov = (byte)(this.numberOfValues - 127), noa = (byte)(this.numberOfAuthorities - 127), noev = (byte)(this.numberOfExtraValues - 127);
        bb.put(rc);
        bb.put(nov);
        bb.put(noa);
        bb.put(noev);


        return bb.array();
    }

    public static Header Decode (byte[] encodedHeader)
    {
        String MsgId, Flags;
        int responseCode, numberOfValues, numberOfAuthorities, numberOfExtraValues;

        short id = ByteBuffer.wrap(new byte[]{encodedHeader[0], encodedHeader[1]}).getShort();
        MsgId = "" + id;

        if (encodedHeader[2]==0b001) Flags = "Q";
        else if (encodedHeader[2]==0b010) Flags = "R";
        else if (encodedHeader[2]==0b100) Flags = "A";
        else if (encodedHeader[2]==0b011) Flags = "Q+R";
        else return null; // mudar isto

        responseCode = (int)encodedHeader[3];
        numberOfValues = (int)encodedHeader[4] + 127;
        numberOfAuthorities = (int)encodedHeader[5] + 127;
        numberOfExtraValues = (int)encodedHeader[6] + 127;

        return new Header(MsgId, Flags, responseCode, numberOfValues, numberOfAuthorities, numberOfExtraValues);
    }

    @Override
    public String toString()
    {
        return this.MsgID + "," + this.Flags + "," + this.responseCode + ","
               + this.numberOfValues + "," + this.numberOfAuthorities + "," + this.numberOfExtraValues + ";";
    }

    public static void main(String[] args)
    {
        Header header = new Header("9854", "Q+R");
        byte[] encoded = header.Encode();
        // System.out.println(Arrays.toString(header.Encode()));
        System.out.println(Header.Decode(encoded));
    }
}