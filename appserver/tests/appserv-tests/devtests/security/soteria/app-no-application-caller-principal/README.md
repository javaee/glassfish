
### app-no-application-caller-principal

**From JSR 375 Specification**

> **1.2.2. Caller Principal Types**

> When no specific application caller principal is supplied during authentication, the caller’s identity should be represented by a single principal, the container’s caller principal.

In this test, no application caller principal is provided when container gets notified about login inside `TestAuthenticationMechanism`.
