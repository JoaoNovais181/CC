
public class DataField 
{
    String Name, TypeOfValue;
    String[] responseValues, authoritiesValues, extraValues;

    public DataField(String Name, String TypeOfValue)
    {
        this.Name = Name;
        this.TypeOfValue = TypeOfValue;
        this.responseValues = null;
        this.authoritiesValues = null;
        this.extraValues = null;
    }

    public DataField(String Name, String TypeOfValue, int nrv, int nav, int nev)
    {
        this.Name = Name;
        this.TypeOfValue = TypeOfValue;
        this.responseValues = new String[nrv];
        for (int i=0 ; i<nrv ; i++) this.responseValues[i] = null;
        this.authoritiesValues = new String[nav];
        for (int i=0 ; i<nav ; i++) this.authoritiesValues[i] = null;
        this.extraValues = new String[nev];
        for (int i=0 ; i<nev ; i++) this.extraValues[i] = null;
    }

    public DataField(String paramsString)
    {
        String[] params = paramsString.split(",");
        this.Name = params[0];
        this.TypeOfValue = params[1];
        this.responseValues = new String[Integer.parseInt(params[2])];
        this.authoritiesValues = new String[Integer.parseInt(params[3])];
        this.extraValues = new String[Integer.parseInt(params[4])];
    }

    public void PutValue (String value)
    {
        for (int i=0 ; i<this.responseValues.length ; i++)
            if (this.responseValues[i] == null) { this.responseValues[i] = value; break; }
    }

    public void PutAuthority (String authority)
    {
        for (int i=0 ; i<this.authoritiesValues.length ; i++)
            if (this.authoritiesValues[i] == null) { this.authoritiesValues[i] = authority; break; }
    }

    public void PutExtraValue (String extraValue)
    {
        for (int i=0 ; i<this.extraValues.length ; i++)
            if (this.extraValues[i] == null) { this.extraValues[i] = extraValue; break; }
    }

    @Override
    public String toString()  // forma concisa
    {
        String r = this.Name + "," + this.TypeOfValue + ";\n";

        if (this.responseValues==null && this.authoritiesValues == null && this.extraValues == null)
            return r;

        for (int i=0 ; i<this.responseValues.length-1 ; i++)
            r += this.responseValues[i] + ",\n";
        r += (this.responseValues==null) ?this.responseValues[this.responseValues.length-1] :"" + ";\n";
        
        for (int i=0 ; i<this.authoritiesValues.length-1 ; i++)
            r += this.authoritiesValues[i] + ",\n";
        r += (this.authoritiesValues==null) ?this.authoritiesValues[this.authoritiesValues.length-1] :"" + ";\n";

        for (int i=0 ; i<this.extraValues.length-1 ; i++)
            r += this.extraValues[i] + ",\n";
        r += (this.extraValues==null) ?this.extraValues[this.extraValues.length-1] :"" + ";\n";
    
        return r;
    }
}
