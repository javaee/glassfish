1. This is a simple PaaS Shared and External Service test.Re-uses the basic-jpa war with a few additions to the glassfish-services.xml
The test creates a shared LB service and external DB service (derby):  The application references these services.
The test basically aims at the following commands:
        i. create-shared-service
        ii. delete-shared-service
        iii. start-shared-service
        iv.stop-shared-service
        v. create-external-service
        vi. delete-external-service
 This test creates a table ZOO_DIRECTORY which displays the a list of animals when called  from the servlet.

TODO : Change the pool creation to DAS (CPAS)'s IP Address instead of local host.

2. The context root for this application is "/external-and-shared-service"

Please refer ../README.txt for more generic guidelines.
