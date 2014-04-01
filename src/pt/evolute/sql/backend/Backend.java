package pt.evolute.sql.backend;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import pt.evolute.ddl.DDLDefaultValue;
import pt.evolute.sql.Expression;
import pt.evolute.sql.Select2;
import pt.evolute.sql.function.SQLFunction;

public interface Backend
{
	public void config( Connection con );
	
	public String[] getInitQuery();
	
	public CharSequence getEscapedFieldName( CharSequence name );
	
	public CharSequence getLimitFieldsPrefix( int limit );
	
	public CharSequence getLimitQuerySuffix( int limit );
	
	public CharSequence getOffsetQueryPrefix( Select2 query );
	
	public CharSequence getOffsetQuerySuffix( int offset );
	
	public String getBoolean( boolean bool );
	
	public String getBegin();
	
	public CharSequence getUserFunctionPrefix();
	
	public CharSequence getUserFunctionName( SQLFunction fun );
	
	public CharSequence portSyntax( CharSequence query );

	public void setEscapeUnicode( boolean escapeUnicode );

	public boolean getEscapeUnicode();

	public CharSequence escapeUnicode( CharSequence str );
	
	public String getDDLConstraintDefinitionPrefix();
	
	public String getDDLCheckConstraint( Expression expression );
	
	public String getDDLUniqueConstraint( List<String> fieldNames );
	
	public String getDDLDefaultValueConstraint( String fieldName, DDLDefaultValue defaultValue );
	
	public String getDDLPrimaryKeyConstraint( List<String> fieldNames );
	
	public String getDDLForeignKeyConstraint( List<String> fieldNames, String foreignTableName, List<String> foreignFieldNames );
	
	public boolean getDDLDefaultValueIsConstraint();

	public boolean supportsILike();
	
	public boolean supportsReturnGeneratedKeys();
	
//	public String getKeepAliveQuery();
	
	public boolean isValid( Connection con )
		throws SQLException;
}
