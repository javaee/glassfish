1. This is a simple PaaS Shared Service test.Re-uses the basic-jpa war.

The test basically aims at testing the register-service-provisioning-engine command.
The test does the following:
    i.  Builds a new plugin of type Database (MyDBPlugin which extends DerbyPlugin) and copies into the
        $GF_HOME/modules directory.
            - Deployment of app should fail.
    ii. Registers one of the plugins as the default SPE
   iii. Deploys app after making one of the plugins as the default SPE- deployment should succeed.
    iv. Unregister the spe
     v. Remove the plugin from $GF_HOME/modules.

 This test creates a table ZOO_DIRECTORY which displays the a list of animals when called  from the servlet.

2. The context root for this application is "/basic-spe-test"

Please refer ../README.txt for more generic guidelines.
