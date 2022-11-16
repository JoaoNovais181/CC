import java.nio.ByteBuffer;
import java.util.BitSet;

public class Header
{
    private String MsgID, Flags;
    private int responseCode;
    private byte numberOfValues, numberOfAuthorities, numberOfExtraValues;

    public Header(String MsgID, String Flags)
    {
        this.MsgID = MsgID;
        this.Flags = Flags;
        this.responseCode = 0;
        this.numberOfValues = 0;
        this.numberOfAuthorities = 0;
        this.numberOfExtraValues = (byte) 255;
    }

    public Header(String MsgID, String Flags, int responseCode, byte numberOfValues, byte numberOfAuthorities, byte numberOfExtraValues)
    {
        this.MsgID = MsgID;
        this.Flags = Flags;
        this.responseCode = responseCode;
        this.numberOfValues = numberOfValues;
        this.numberOfAuthorities = numberOfAuthorities;
        this.numberOfExtraValues = numberOfExtraValues;
    }

    // msgid -> 16 bits, flags -> 2 bits, rest -> 8 bits
    // public byte[] Encode ()
    // {
    //     ByteBuffer bb = ByteBuffer.allocate(42);
    //     BitSet bs = new BitSet(42);
    //     int msgid = Integer.parseInt(this.MsgID);
    //     bb.putInt(msgid, msgid);
    // }

    public static void main (String[] args) 
    {
        System.out.println((byte)255);
    }
}