package pt.evolute.utils.object;

/**
 *
 * @author fpalma
 */
public class InvalidPropertyException
		extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	/** Creates a new instance of InvalidPropertyException */
	public InvalidPropertyException( String property, String message )
	{
		super( "Invalid property: " + property + "\n" + message );
	}
	
}
