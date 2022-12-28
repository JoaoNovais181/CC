import java.util.ArrayList;
import java.util.List;

public class DataField 
{
    String Name, TypeOfValue;
    List<String> responseValues, authoritiesValues, extraValues;

    public DataField(String Name, String TypeOfValue)
    {
        this.Name = Name;
        this.TypeOfValue = TypeOfValue;
        this.responseValues = new ArrayList<>();
        this.authoritiesValues = new ArrayList<>();
        this.extraValues = new ArrayList<>();
    }

    public DataField(String Name, String TypeOfValue, int nrv, int nav, int nev)
    {
        this.Name = Name;
        this.TypeOfValue = TypeOfValue;
        this.responseValues = new ArrayList<>();
        this.authoritiesValues = new ArrayList<>();
        this.extraValues = new ArrayList<>();
    }

    public DataField(String paramsString)
    {
        String[] params = paramsString.split(",");
        this.Name = params[0];
        this.TypeOfValue = params[1];
		this.responseValues = new ArrayList<>();
		this.authoritiesValues = new ArrayList<>();
		this.extraValues = new ArrayList<>();
    }

    public void PutValue (String value)
    {
        this.responseValues.add(value);
    }

    public void PutAuthority (String authority)
    {
        this.authoritiesValues.add(authority);
    }

    public void PutExtraValue (String extraValue)
    {
        this.extraValues.add(extraValue);
    }

    @Override
    public String toString()  // forma concisa
    {
        String r = this.Name + "," + this.TypeOfValue + ";";

		if (this.responseValues.size() != 0)
		{
			for (int i=0 ; i<this.responseValues.size()-1 ; i++)
				r += this.responseValues.get(i) + ",";
			r += this.responseValues.get(this.responseValues.size()-1)+";";
		}
        else r+=";";

		if (this.authoritiesValues.size() != 0)
		{
			for (int i=0 ; i<this.authoritiesValues.size()-1 ; i++)
				r += this.authoritiesValues.get(i) + ",";
			r += this.authoritiesValues.get(this.authoritiesValues.size()-1)+";";
		}
        else r+=";";

		if (this.extraValues.size() != 0)
		{
			for (int i=0 ; i<this.extraValues.size()-1 ; i++)
				r += this.extraValues.get(i) + ",";
			r += this.extraValues.get(this.extraValues.size()-1)+";";
		}
        else r+=";";
    
        return r;
    }

    public String getName()
    {
        return this.Name;
    }

    public String getTypeOfValue()
    {
        return this.TypeOfValue;
    }

    public List<String> getResponseValues() { return this.responseValues; }

    public List<String> getAuthoritiesValues() { return this.authoritiesValues; }

    public List<String> getExtraValues() { return this.extraValues; }

    public String prettyString()
    {
        String r = "# Data: Query Info\nQUERY-INFO.NAME = " + this.Name + ", QUERY-INFO.TYPEOFVALUE = " + this.TypeOfValue + ";\n# Data: List of Response, Authorities and Extra Values\n";

        if (this.responseValues.size() != 0)
		{
			for (int i=0 ; i<this.responseValues.size()-1 ; i++)
				r += "RESPONSE-VALUES = " + this.responseValues.get(i) + ",\n";
			r += "RESPONSE-VALUES = " + this.responseValues.get(this.responseValues.size()-1)+";\n";
		}
        else r+=";\n";

		if (this.authoritiesValues.size() != 0)
		{
			for (int i=0 ; i<this.authoritiesValues.size()-1 ; i++)
				r += "AUTHORITIES-VALUES = " + this.authoritiesValues.get(i) + ",\n";
			r += "AUTHORITIES-VALUES = " + this.authoritiesValues.get(this.authoritiesValues.size()-1)+";\n";
		}
        else r+=";\n";

		if (this.extraValues.size() != 0)
		{
			for (int i=0 ; i<this.extraValues.size()-1 ; i++)
				r += "EXTRA-VALUES = " + this.extraValues.get(i) + ",\n";
			r += "EXTRA-VALUES = " + this.extraValues.get(this.extraValues.size()-1)+";";
		}
        else r+=";";

        return r;
    }
}
