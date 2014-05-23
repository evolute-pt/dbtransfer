package pt.evolute.utils.db;

import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.sql.SQLQuery;

public interface Executer
{
	// o executer nao tem connection, so o dbmanager e q tem, 
	// o executer e so uma chave

	public void executeQuery(SQLQuery query, Retriever retriever)
		throws DBException;
	
	public Virtual2DArray executeQuery(SQLQuery query)
		throws DBException;
	// adicionar metodos sincronos (com threads e listener, tipo executeLater )
	
	public void close();
}
