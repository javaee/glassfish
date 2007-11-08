README

The entity-persistence tests contain unit tests for entity-persistence (TopLink Essentials) module.

Known Issues
------------
Due to database feature set, the testPessimisticLockHintStartsTransaction() test will fail on Derby with
the following Exception:

Warning: DerbyPlatform does not currently support pessimistic locking

How to run entity-persistence tests
-----------------------------------
The tests are currently configured to build with maven or ant, and to run with ant.

1. Ensure the entity-persistence module has successfully built

2. To build the testing run "maven build" or "ant build". 

3. Prepare target database and test.properties file which contains target database properties.

   By default test.properties file contain properties for JavaDB(Derby), thus you just need to 
   start bundled JavaDB and skip to #4 unless you want to run tests on other database type.
   
   If you want to run tests on other databases, you need to edit test.properties file or prepare 
   another properties file(Another properties files are recommended for other type of databases).

   Edit the following entries:
    jdbc.driver.jar   - JDBC driver jar file of the target database
    db.driver         - the name of the driver class
    db.url            - the JDBC URL to the target database
    db.user           - target database username
    db.pwd            - target database password

   If you use other properties file other than test.properties file, provide 'test.properties' 
   property which points to the your test.properties file.
   This can be done by adding -Dtest.properties option when running tests(see below) or add 
   'test.properties' property to ${user.home}/build.properties file. 

4. Prepare to run ant by configuring your classpath to include a junit jar file.  
   An appropriate junit.jar can be found in ../appserv-tests/lib. The ant tests make use of 
   a junit task and ant requires that junit be on the classpath for this task to run.

5. Run "ant test".
   This will run all tests with test.properties file which contains testing environment.
   You can override this behaviour by using following options.

   Possible options:
    -Dtest.class=test_class_name   If you want to run a single test class. 
    -Dtest.properties=file_name    If you want to run tests with another test.properties file. 

6. To clean the view run "maven clean" or "ant clean"

Drop all tables of database
----------------------------
If you want to drop *all* tables from your database to run the test on a clean database, 
you can use the following ant target. 
Note, that all tables for your user will be dropped, not only those used by these tests.

$ ant clean-db

This will be required in some cases if existing tables or foreign key contraints cause test failures.


-------------------------------
BEGIN: TEST PACKAGING STRUCTURE 
-------------------------------
Starting from 7 Oct 2006, the packaging of tests have change so that we can
avoid bugs like GF #1074, 1131 etc. The documentation below describes the new
structure. Please update it when yo make any change. Read the build.xml
for more information.

1. What are the jar files produced?
-----------------------------------
build produces *6* jar files, viz:
toplink-essentials-tests.jar,
toplink-essentials-annotation-model-tests.jar,
toplink-essentials-xml-only-model-tests.jar,
toplink-essentials-xml-merge-model-tests.jar,
toplink-essentials-ddl-generation-tests.jar and
toplink-essentials-validation-tests.jar.

2. Where are the JUnit classes packaged?
----------------------------------------
*toplink-essentials-tests.jar* contains *only* JUnit test and framework 
classes. i.e. t contains:
oracle/toplink/essentials/testing/tests and 
oracle/toplink/essentials/testing/framework 
packages. It does not contain any model classes or XML files. This is not 
a persistence archive.

3. Where are the model classes packaged?
----------------------------------------
Model classes are packaged in five different files:
a) *toplink-essentials-annotation-model-tests.jar* includes 
oracle/toplink/essentials/testing/models/ classes,
but excludes 
oracle/toplink/essentials/testing/models/cmp3/xml/** and 
oracle/toplink/essentials/testing/models/cmp3/validationfailed/**.

b) *toplink-essentials-xml-only-model-tests.jar* includes 
oracle/toplink/essentials/testing/models/cmp3/xml/, 
but excludes 
oracle/toplink/essentials/testing/models/cmp3/xml/merge/**.

c) *toplink-essentials-xml-merge-model-tests.jar* includes 
oracle/toplink/essentials/testing/models/cmp3/xml/merge/, 
but excludes 
oracle/toplink/essentials/testing/models/cmp3/xml/merge/inherited/**.

d) *toplink-essentials-ddl-generation-tests.jar* includes 
oracle/toplink/essentials/testing/models/cmp3/xml/merge/inherited/**.

e) *toplink-essentials-validation-tests.jar* includes
oracle/toplink/essentials/testing/models/cmp3/validationfailed/.

4. Where are the persistence.xml files packaged?
------------------------------------------------
There are three persistence.xml files. 
a) config/META-INF/persistence.xml is packaged in toplink-essentials-annotation-model-tests.jar. 
 So, this jar is a PU Root. This persistence.xml uses <jar-file>
 element in persistence.xml as shown below to reference model classes from other
 two jars:
       <jar-file>toplink-essentials-xml-only-model-tests.jar</jar-file>
       <jar-file>toplink-essentials-xml-merge-model-tests.jar</jar-file>

b) config/ddl-generation-testmodel/META-INF/persistence.xml is packaged in
toplink-essentials-ddl-generation-tests.jar.

c) config/validation-failed-testmodel/META-INF/persistence.xml is packaged in
toplink-essentials-validation-tests.jar. 

5. How are the mapping XML documents packaged?
----------------------------------------------
Mapping files are distributed among all the jar files as shown below:
toplink-essentials-annotation-model-tests.jar:
META-INF/orm.xml

toplink-essentials-ddl-generation-tests.jar:
META-INF/merge-inherited-beers.xml
META-INF/merge-inherited-certification.xml
META-INF/merge-inherited-consumer.xml
META-INF/merge-inherited-superclasses.xml

toplink-essentials-tests.jar:
none
toplink-essentials-validation-tests.jar:
none
toplink-essentials-xml-merge-model-tests.jar:
META-INF/incomplete-nonowning-entity-mappings.xml
META-INF/incomplete-owning-entity-mappings.xml
META-INF/orm-annotation-merge-advanced-entity-mappings.xml
META-INF/orm-annotation-merge-relationships-entity-mappings.xml

toplink-essentials-xml-only-model-tests.jar:
META-INF/orm.xml (inheritance-entity-mappings.xml is renamed as orm.xml)
META-INF/advanced-entity-mappings.xml
META-INF/inherited-entity-mappings.xml
META-INF/relationships-entity-mappings.xml
META-INF/unidirectional-relationships-entity-mappings.xml

6. Special Notes:
------------------
To test issue #1131 and #589, we rename inheritance-entity-mappings.xml and
package it as orm.xml in toplink-essentials-xml-only-model-tests.jar.

-------------------------------
END: TEST PACKAGING STRUCTURE 
-------------------------------
