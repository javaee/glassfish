This is a PaaS application to test atomic deployment.
Orchestrator deploys the application via various steps and some of the steps with the aid of Service Provisioning engines.
This test-case will use FailureInducer exposed by Orchestrator to induce failures at various states of deployment of the application 
and checks whether the deployment is failed and no services/artifacts are left in the application server (DAS).

Please refer ../README.txt for more generic guidelines.
