To test lifecycle-listener-modules :

1. Compile the java file with command :

javac -classpath $S1AS_HOME/lib/appserv-rt.jar DeplLifecycleModule.java

2. Create server-instances

3. Create the life-cycle-module with command :

asadmin create-lifecycle-module --user admin --password admin123 --target <your-target> --classname DeplLifecycleModule --classpath $APS_HOME/devtests/deployment/lifecycle-module MyModule

4. Stop and re-start <your-target> and search the server.log of <your-target> for the pattern "DeplLifecycleListener:" - you will find the following :

DeplLifecycleListener: INIT_EVENT
DeplLifecycleListener: STARTUP_EVENT
DeplLifecycleListener: READY_EVENT

5. Create refrence using command :

asadmin create-application-ref --user admin --password asmin123 --target <ref-target> MyModule

6. Stop and re-start <ref-target> and search the server.log of <ref-target> for the pattern "DeplLifecycleListener:" - you will find the following :

DeplLifecycleListener: INIT_EVENT
DeplLifecycleListener: STARTUP_EVENT
DeplLifecycleListener: READY_EVENT

7. Do "asadmin delete-lifecycle-module --user admin --password admin123 --target <your-target> MyModule" - it should fail

8. Do the following :

asadmin delete-application-ref --user admin --password admin123 --target <ref-target> MyModule
asadmin delete-lifecycle-module --user admin --password admin123 --target <your-target> MyModule

Now all the above commands should pass

