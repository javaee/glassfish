1. This is a simple PaaS test for testing the lazy initialisation of any shared service. This test reuses the basic-db application
    -When shared services are created with initmode=lazy, only the configs get created. The service is really provisioned only during
     deployment of an application which references the shared service.

2. The context root for this application is "/lazy-shared-service-test"

3. Follow the general guidelines for running PaaS tests provided at ../README.txt
