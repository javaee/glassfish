THESE INSTRUCTIONS MIGHT NOT BE UP TO DATE.

____________________________________________________________________________________________________
WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
QuickLook has a bug which will hang early during the ee tests waiting for an http port that doesn't 
exist (it typically waits until it times out on port 38080 in sqe-domain, which is the http port
found in eeconfig/ee-config.properties.  But sqe-domain gets (incorrectly) created with port 8080.

This can happen with a fresh checkout or a later run.

--Lloyd Chambers, Dec 7, 2006
____________________________________________________________________________________________________

SEE:

https://glassfish.dev.java.net/public/GuidelinesandConventions.html#Commit_Procedures


Steps to Execute Quick Look Tests
=================================
Same instructions as: http://appserver.sfbay/as9ee/eng/build/Glassfish_Quicklook_Tests_Instructions.html

1) Checkout workspace (You need to have access to CVS Tunnel for this):
   %cvs -d:pserver:<login>@rejuniper.sfbay.sun.com:/cvs co glassfish/appserv-tests

2) Install SJSAS Server or get Server image ($glassfish.home) bootstrapped using "maven bootstrap" to set this 
   location as S1AS_HOME environment variable, Refer to detailed build instruction on 
   How to build SJSAS: http://appserver.sfbay.sun.com/as9ee/eng/build/index.html using maven. 

3) Environment Settings and Permissions:

   a) Set environment variables in your .cshrc, or .bat file:
     - set APS_HOME <directory where you checked out the workspace including the workspace root name 
           (e.g. /workspace/glassfish/appserv-tests)>
     - set S1AS_HOME <installation directory for appserver (e.g. /Sun/Appserver)>
     - set JAVA_HOME <directory where you installed java 1.5.0 (e.g. /Sun/jdk1.5.0_01)>
     - set MAVEN_HOME <installation directory for Maven 1.0.2 (e.g. /workspace/maven-1.0.2)>

     % setenv PATH $S1AS_HOME/bin;$JAVA_HOME/bin;$MAVEN_HOME/bin;$PATH

     On Windows:
     % set PATH=%S1AS_HOME%\bin;%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

   b) Modify the installation properties under $APS_HOME/config.properties to match your installation. 
        e.g. admin.password, http.port etc.

   c) be sure that the master password has been saved with --savemasterpassword when
     creating the domain.  Or use 'asadmin change-master-password <domain>'.  Failure
     to do so will hang the test when it attempts to start the domain (at least on
     Windows).

   d) [possibly Windows-only]  Check the variable AS_ACC_CONFIG in $INSTALL_DIR/config/asenv.bat.
       If you see extra quote characters, remove them; otherwise all the appclient tests will fail.
           set AS_ACC_CONFIG=D:\appserver\domains\"domain1"\config\sun-acc.xml
      This has been logged as bug #6332733.

4) Run Tests

   Remember to set the above environment variables, if not already set.
   % cd $APS_HOME
   % maven runtest
   Open the $APS_HOME/test_results.html file in a browser and see the results.

5) Run EE Quicklook Tests:
   a) Modify the installation properties under $APS_HOME/eeconfig/ee-config.properties if you want to change 
      default values of admin domain (DAS) that will be created. e.g. admin.password, http.port etc.

    Remember to set the above environment variables, if not already set.
    % cd $APS_HOME
    % maven runtest-ee (This will run PE quicklook on default domain1 and EE quicklook on new DAS)

    To run EE Standalone quicklook tests, do the following:
    % cd $APS_HOME
    % maven runtest-ee-standalone (This will run only EE quicklook on new DAS)

    Open the $APS_HOME/test_results.html file in a browser and see the results.

6) Confirm Test Count and Details:

   Number of test suites: 19
   Number of test cases: 50

7) Running Individual Tests

   The tests are under devtests and sqetests directory.
   You can cd to individual directories to run tests separately.
   You will need to use ANT though.

8) Other details
   - ANT version required is 1.6.5 or higher
   - Test results are located at $APS_HOME/test_results.html
   - To clean up test output manually
     rm *.output
     rm count.txt
     rm test_result*

Last Updated: 01/12/2006

This is a test line for the "cvs -diff u"
