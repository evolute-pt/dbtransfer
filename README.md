DBTransfer
==========

**[DBTransfer](http://dbtransfer.evo.pt/)** - versatile and fast database transfer tool

## What is DBTransfer?
DBTransfer is a multi-platform java tool, that aims to replicate one
database (JDBC or MS Access) to another (JDBC), using an one to one
conversion.

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
- mvn package

This will create a ./target/dbtransfer-1.0-SNAPSHOT.jar

After this, you have to run the tool, using:

- java -jar dbtransfer-1.0-SNAPSHOT.jar  _**dbtransfer.properties**_

You have to create a properties file, like the ones in the examples.

## What needs to be done.. right away!
Improve the output, ~~and enable Maven~~ on the project. Feel free to help
out!

## Sponsors
This project had the invaluable help of a tool, DBVisualizer, which help
us test everything and connect to any database. DBVisualizer uses JDBC, the same framework as DBTransfer, so it provides a nice way to validate database metadata and testing database connections to use in the properties file.

Our friends at DBVisualizer were very kind to provide to all the
contributers, a valid DBVisualzer Pro license. Thank you for your
support!

DBVisualizer - http://www.dbvis.com
