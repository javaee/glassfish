* Test case to make sure that @DSD is available for JPA during prepare() phase. 
* Refer issue : https://glassfish.dev.java.net/issues/show_bug.cgi?id=11795
* This test creates it's own DataSourceDefinition to use XA.
* Uses @DSD exposed by appclient in one of the test-case
* Uses @DSD exposed by ejb in another test-case
* This test-case is forked from appserv-tests/devtests/ejb/ejb30/persistence
