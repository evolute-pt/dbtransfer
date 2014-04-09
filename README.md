DBTransfer
==========

## DBTransfer - versatile and fast database transfer tool

## What is DBTransfer?
DBTransfer is a java tool, multi-platform, that aims to transfer any database
techonology to another, using one to one conversion.

## What can I do with DBTransfer?
- Copy data from one database type to another type. Example: you can
  migrate your MSSQL to PostgreSQL, without losing the structure,
including not only the original schema, the constraints, and of course,
the data it self, which is converted to respective data-type.
- Copy one database schema to another.
- Move data from one database to another.
- Compare two databases.

## Downloading and running DBTransfer
For now you, to run DBTransfer, you need to check out the project, and
compile it, using:
- ant jar
After this, you have to run the tool, using:
- java -jar dbtransfer.jar <DBTRANSFER.properties>
