1. This is a simple test to test the behaviour of External and Shared Services when an application that uses these services are enabled
and disabled.Re-uses the basic-jpa war(renamed in this to ext-shared-service-enable-disable-test.war) with a few additions to the glassfish-services.xml
The test creates 2 shared service: 1 for lb and 1 for db. The application references these services.
The test basically aims at testing the following commands after disabling and enabling the application:
        i. delete-external-service
        ii. delete-shared-service
        iii. stop-external-service
        iv.stop-shared-service
 This test creates a table ZOO_DIRECTORY which displays the a list of animals when called  from the servlet.

2. The context root for this application is "/basic-shared-service-test"

Please refer ../README.txt for more generic guidelines.
