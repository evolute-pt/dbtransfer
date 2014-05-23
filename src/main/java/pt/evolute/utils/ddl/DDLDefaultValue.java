package pt.evolute.utils.ddl;

public class DDLDefaultValue
{
	protected final Integer type;
	protected final String value;
	
	public DDLDefaultValue(Integer type, String value)
	{
		super();
		this.type = type;
		this.value = value;
	}

	/**
	 * @return the type
	 */
	public Integer getType()
	{
		return type;
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}
	
	
}
