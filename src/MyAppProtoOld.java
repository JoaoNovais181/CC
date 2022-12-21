public class MyAppProtoOld {
    private String msgID;
    private String flags;
    private String numbers;

    public MyAppProtoOld (String msgID, String flags, String numbers)
    {
        this.msgID   = msgID;
        this.flags   = flags;
        this.numbers = numbers;
    }

    public MyAppProtoOld (byte[] info)
    {
        String infoStr = new String(info).trim();
        String[] fields = infoStr.split(";");
        this.msgID   = fields[0];
        this.flags   = fields[1];
        this.numbers = fields[2];
    }

    public String getMsgID () { return this.msgID; }

    public String getFlags () { return this.flags; }

    public String getNumbers () { return this.numbers; }

    public void setMsgID (String msgID) { this.msgID = msgID; }

    public void setFlags (String flags) { this.flags = flags; }

    public void setNumbers (String numbers) { this.numbers = numbers; }

    public String toString()
    {
        return this.msgID + ";" + this.flags + ";" + this.numbers;
    }
}