package pt.evolute.utils.sql;

import pt.evolute.utils.sql.backend.Backend;
import pt.evolute.utils.sql.backend.BackendProvider;

public class Assignment
{
	private final Operand iValue;
	private final Field iField;
	
	private Backend backend = null;
	
	public Assignment( String field, Object value )
	{
		this( new Field( field ), new Operand( value ) );
	}
	
	public Assignment( Field field, Object value )
	{
		this( field, new Operand( value ) );
	}
	
	private Assignment( Field field, Operand value )
	{
		iValue = value;
		iField = field;
	}
	
	@Override
	public String toString()
	{
		if( iValue != null )
		{
			iValue.setBackend( getBackend() );
		}
		if( iField == null )
		{
			return null;
		}
		String value = "";
		if( iValue.getInnerObject() instanceof byte[] )
		{
			value = "?";
		}
		else
		{
			value = iValue.toString();
		}
		return iField.toString() + " = " + value;
	}
	
	public String getLeft()
	{
		return iField.toString();
	}
	
	public String getRight()
	{
		String value = "";
		if( iValue.getInnerObject() instanceof byte[] )
		{
			value = "?";
		}
		else
		{
			value = iValue.toString();
		}
		return value;
	}
	
	public boolean isBatch()
	{
		return iValue.isBatch();
	}
	
	public int getBatchSize()
	{
		return iValue.getBatchSize();
	}
	
	public void currentBatch( int index )
	{
		iValue.currentBatch( index );
	}

	protected Operand getOperand()
	{
		return iValue;
	}
	
	public void setBackend( Backend backend )
	{
		this.backend = backend;
		if( iValue != null  )
		{
			iValue.setBackend( backend );
		}
		if( iField != null )
		{
			iField.setBackend( backend );
		}
	}
	
	protected Backend getBackend()
	{
		if( backend == null )
		{
//			new Exception( "NO BACKEND!!!!" ).printStackTrace( System.out );
			setBackend( BackendProvider.getDefaultBackend() );
		}
		return backend;
	}
}
