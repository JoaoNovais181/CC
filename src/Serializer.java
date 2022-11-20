import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Serializer 
{
    
    public static byte[] Serialize_String (String str)
    {
        return str.getBytes();
    }

    public static String Deserialize_String (byte[] bytes)
    {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] Serialize_Int (int num)
    {
        return ByteBuffer.allocate(4).putInt(num).array();
    }

    public static int Deserialize_Int (byte[] bytes)
    {
        return ByteBuffer.wrap(bytes).getInt();
    }

}
