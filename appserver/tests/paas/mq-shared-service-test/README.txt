1. This is a simple MQ Shared Service test.The main intent of the test is to check if an MQ shared service is created and used properly.


The test basically aims at testing the register-service-provisioning-engine command.
The test does the following:
    i.  Creates a shared service of type mq.
    ii. Deploys an app which has a reference to this shared service.
        - the app should be able to access the MQ service.



2. The context root for this application is "/web"


Please refer ../README.txt for more generic guidelines.
