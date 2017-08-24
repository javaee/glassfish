
### app-container-application-principal-getname

**From JSR 375 Specification**

> **1.2.2. Caller Principal Types**

> When both a container caller principal and an application caller principal are present, the value obtained by calling getName() on both principals MUST be the same.

In this test, application provides its own caller principal,
and hence, subject contains two such principals, one
representing the container and other one representing the application itself.
