package pt.evolute.utils.sql.backend;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

import pt.evolute.utils.error.ErrorLogger;
import pt.evolute.utils.sql.function.SQLConcat;
import pt.evolute.utils.sql.function.SQLFunction;
import pt.evolute.utils.string.EvoStringUtils;
import pt.evolute.utils.string.UnicodeChecker;

public class PostgreSQLBackend extends DefaultBackend
{
	private boolean useDoubleSlash = true;

	private boolean jdbc4method = true;
	
	public PostgreSQLBackend()
	{
		registerReservedKeyword( "user" );
		registerReservedKeyword( "order" );
		registerReservedKeyword( "table" );
	}
	
	@Override
	public void config( Connection con )
	{
		try
		{
			Statement stm = con.createStatement();
			stm.execute( "SELECT '\\\\'" );
			ResultSet rs = stm.getResultSet();
			if( rs.next() )
			{
				if( rs.getString( 1 ).length() == "\\\\".length() )
				{
					useDoubleSlash = false;
				}
			}
			stm.close();
		}
		catch( SQLException ex )
		{
			ErrorLogger.logException( ex );
		}
	}

	@Override
	public CharSequence escapeUnicode( CharSequence str )
	{
		return getEscapeUnicode() ? UnicodeChecker.parseToUnicode( str, true, useDoubleSlash ) : EvoStringUtils.parsePlica( str );
	}

	@Override
	public boolean isValid(Connection con)
		throws SQLException
	{
		boolean valid = false;
		if( jdbc4method )
		{
			try
			{
				valid = super.isValid( con );
			}
			catch( SQLFeatureNotSupportedException ex )
			{
				jdbc4method = false;
			}
		}
		if( !jdbc4method )
		{
			Statement stm = con.createStatement();
			stm.execute( "SELECT 1" );
			stm.close();
			valid = true;
		}
		return valid;
	}
	
	@Override
	public CharSequence getUserFunctionName( SQLFunction fun )
	{
		String name = fun.getName();
		if( fun instanceof SQLConcat )
		{
			name = "||";
		}
		return name;
	}
}
