Automatic MBAPI Deployment Tests (Byron Nevins, 2004)

These tests can deploy any arbitrary list of samples.  That requires a slight amount of work
on your part.  What's documented here is the simplest possible tests.

The tests will deploy one each of jar, rar, war, ear and one dir-deploy of a web module.
The tests do 3 steps for each sample:

1)undeploy (in case it already exists on the server or you killed these tests in the middle earlier)
2)deploy
3)undeploy

The results of the tests are printed to the screen and are formatted and place in a text file: 
DeploymentTests.out

You can pause in-between the (2) deploy and (3) undeploy steps in order to run the app, check
domain.xml, etc.  The pause is implemented with a (GUI) message box so don't try it on a dumb terminal
or a telnet session, etc.

Details:

1) Since you're reading this you've already done step 1 which is to checkout appserv-tests tree!
2) possibly edit DeploymentTests.properties.  This is where you set admin username/password, targets,
server, pauseAfterDeploy, etc.  If you use the most common settings -- username/password="admin"/"adminadmin"
server=localhost, target="server", no pause-after-deploy -- then you don't need to touch the file at all.
3)asant runtests
