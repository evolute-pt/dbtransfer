package pt.evolute.tables;

public interface ColumnizedObject
{
	public <OBJ_CLASS> OBJ_CLASS getValue( int col );
}