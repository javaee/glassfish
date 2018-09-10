1. This is a simple PaaS Shared Service test.Re-uses the basic-jpa war with a few additions to the glassfish-services.xml
The test creates 2 shared service: 1 for lb and 1 for db. The application references these services.
The test basically aims at the following commands:
        i. create-shared-service
        ii. delete-shared-service
        iii. start-shared-service
        iv.stop-shared-service
 This test creates a table ZOO_DIRECTORY which displays the a list of animals when called  from the servlet.

2. The context root for this application is "/basic-shared-service-test"

Please refer ../README.txt for more generic guidelines.
