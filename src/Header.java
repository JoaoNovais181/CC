import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

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

    // msgid -> 16 bits, flags -> 4 bits  (podia ser 2 mas nao consigo fazer com 2 por enquanto), rest -> 8 bits
    public byte[] Encode()
    {
        ByteBuffer bb = ByteBuffer.allocate(56);
        short id = Short.valueOf(this.MsgID);
        bb.putShort(id);
        
        byte flags;
        if (this.Flags.equals("Q")) flags = 0b01;        // se flag for Q entao fica com o valor 01 em binario
        else if (this.Flags.equals("R")) flags = 0b10;   // se flag for R entao fica com o valor 10 em binario
        else if (this.Flags.equals("A")) flags = 0b11;   // se flag for A entao fica com o valor 11 em binario
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

    public static void main(String[] args)
    {
        Header header = new Header("1", "Q");
        System.out.println(Arrays.toString(header.Encode()));
    }
}