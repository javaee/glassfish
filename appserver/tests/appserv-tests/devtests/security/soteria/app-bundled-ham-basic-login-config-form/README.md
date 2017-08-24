### app-bundled-ham-basic-login-config-form

**If an application bundles its own `HttpAuthenticationMechanism`, then for authentication, the container will rely on the bundled mechanism and will ignore the `login-config` element in deployment descriptor of the application.

In this sample app, the `BASIC` authentication mechanism defined in `HttpAuthenticationMechanism` takes precedencce over `FORM` authentication present in `login-config`.
