/*
 * FieldIndex.java
 *
 * Created on 6 de Junho de 2005, 13:05
 */

package pt.evolute.utils.sql;

/**
 *
 * @author  fpalma
 */
public class FieldIndex extends Field
{
	protected Operand indexed;
	protected Integer index;
	
	public FieldIndex( Operand indexed, int index )
	{
		this( indexed, new Integer( index ) );
	}
	
	/** Creates a new instance of FieldIndex */
	public FieldIndex( Operand indexed, Integer index )
	{
		super( index.toString() );
		this.indexed = indexed;
		this.index = index;
	}
}
