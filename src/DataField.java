import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@code DataField}, used to represent the Data field on a DNS query
 * @author Bianca Araújo do Vale a95835
 * @author João Carlos Fernandes Novais a96626
 * @author Nuno Miguel Leite da Costa a96897 
 */
public class DataField 
{
    /**
     * Strings that represent the Name and Type of value the querie is searching for
     */
    String Name, TypeOfValue;
    /**
     * {@link List}s that represent the values, authorities and extra values on the querie
     */
    List<String> responseValues, authoritiesValues, extraValues;

    /**
     * Constructs a new DataField using the provided name and type
     * @param Name Name to search for
     * @param TypeOfValue Type of value to search for
     */
    public DataField(String Name, String TypeOfValue)
    {
        this.Name = Name;
        this.TypeOfValue = TypeOfValue;
        this.responseValues = new ArrayList<>();
        this.authoritiesValues = new ArrayList<>();
        this.extraValues = new ArrayList<>();
    }

    /**
     * Construcs a new DataField using the provided arguments
     * @param Name Name to search for
     * @param TypeOfValue Type of the value to search for
     * @param nrv Number of response values
     * @param nav Number of authoritative values
     * @param nev Number of extra values
     */
    public DataField(String Name, String TypeOfValue, int nrv, int nav, int nev)
    {
        this.Name = Name;
        this.TypeOfValue = TypeOfValue;
        this.responseValues = new ArrayList<>();
        this.authoritiesValues = new ArrayList<>();
        this.extraValues = new ArrayList<>();
    }

    /**
     * Constructs a new DataField using a String
     * @param paramsString String to build the new DataField from
     */
    public DataField(String paramsString)
    {
        String[] params = paramsString.split(",");
        this.Name = params[0];
        this.TypeOfValue = params[1];
		this.responseValues = new ArrayList<>();
		this.authoritiesValues = new ArrayList<>();
		this.extraValues = new ArrayList<>();
    }

    /**
     * Put a response value into the responseValues list
     * @param value Value to put into the responseValues list
     */
    public void PutValue (String value)
    {
        this.responseValues.add(value);
    }

    /**
     * Put a authoritative value into the authoritatiesValues list
     * @param value Value to put into the authoritatiesValues list
     */
    public void PutAuthority (String authority)
    {
        this.authoritiesValues.add(authority);
    }

    /**
     * Put a extra value into the extraValues list
     * @param value Value to put into the extraValues list
     */
    public void PutExtraValue (String extraValue)
    {
        this.extraValues.add(extraValue);
    }

    @Override
    /**
     * String representation of the DataField
     * @return String representation of the DataField
     */
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

    /**
     * Method used to retrieve the value of Name 
     * @return the value of Name
     */
    public String getName()
    {
        return this.Name;
    }

    /**
     * Method used to retrieve the value of TypeOfValue 
     * @return the value of TypeOfValue
     */
    public String getTypeOfValue()
    {
        return this.TypeOfValue;
    }

    /**
     * Method used to retrieve the response values
     * @return the response values
     */
    public List<String> getResponseValues() { return this.responseValues; }

    /**
     * Method used to retrieve the authorities values
     * @return the authorities values
     */
    public List<String> getAuthoritiesValues() { return this.authoritiesValues; }

    /**
     * Method used to retrieve the extra values
     * @return the extra values
     */
    public List<String> getExtraValues() { return this.extraValues; }

    /**
     * String representation of the DataField presented to the {@link Client}
     * @return String representation of the DataField presented to the {@link Client}
     */
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
