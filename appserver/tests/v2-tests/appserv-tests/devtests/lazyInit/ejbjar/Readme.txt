Purpose:

To Test if a standalone ejb-jar can be deployed and run.

Note:

build.xml explained:
- You will note that the client has been packaged in an ear file and deployed on the server. This has been done for the purpose of retrieving the stubs. 
- It is possible to retrieve the stubs as an AppClient.jar as the application-client.xml file has references to the stateful session bean.
- The client can be run in two ways: a) Using the AppClient.jar file and the runclient-common target. b) Running it as a standalone java client. The target for the second option has also been provided. The Client.java file is not currently capable of handling the second option. To make it compatible see ejb/stateless/converter sample client.

