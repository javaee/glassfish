
How to run the Persistence System Tests
---------------------------------------

This project provides driver classes that run EJB3 entity tests
(outside an EJB3 container).

1. Build the tests with dependencies and ejb resources:

   Run in this directory (persistence-impl/test):

     ant jar

   This builds the persistence runtime and pre-processes (enhances)
   these entity schemas used in the tests:
     ../deployment/test/acme
     ../deployment/test/company


2. Define the JDBC driver properties:

   The default database configured for the tests is Derby.

   If you'd like to run against a different database, you need to define
   these properties in the (personal) property file
      ./nbproject/private/private.properties

run.jdbc.classpath=C:/etc/ojdbc14.jar
run.jdbc.url=jdbc:oracle:thin:@cmpserver.sfbay.sun.com:1521:ORA920
run.jdbc.driver=oracle.jdbc.driver.OracleDriver
run.jdbc.username=dbuser
run.jdbc.password=dbpassword
run.jdbc.tables.acme.create=../deployment/test/acme/src/sql/tables_ora.sql
run.jdbc.tables.acme.drop=../deployment/test/acme/src/sql/drop_ora.sql
run.jdbc.tables.company.create=../deployment/test/company/src/sql/tables_ora.sql
run.jdbc.tables.company.drop=../deployment/test/company/src/sql/drop_ora.sql


3. Run the tests:

   To run *all* tests:

     # loads the 'acme' and 'company' database schemas and runs the tests
     ant run-tests

   To run the 'acme' entity tests:

     # (re)load the 'acme' schema in the database,
     ant load-acme

     # run a very basic acme test
     ant run-acme-test-0

     # (re)load the 'acme' schema in the database,
     ant load-acme

     # run a simple acme test
     ant run-acme-test-1

     # drop the 'acme' schema from the database,
     ant unload-acme

   To run the 'company' entity tests:

     # (re)load the 'company' schema in the database,
     ant load-company

     # run a very basic company test
     ant run-company-test-0

     # (re)load the 'company' schema in the database,
     ant load-company

     # run a simple company test
     ant run-company-test-1

     # drop the 'company' schema from the database,
     ant unload-company
