This directory tests hk2 without the use of javassist.  There are no
proxiable scopes or any proxies at all.

This directory DOES have access to the AOP alliance jar, but not javassist.
Compare that to hk2-locator-no-proxies directory which has access to neither.

The intent of this test is to ensure that in this case a reasonable error
exception is thrown when attempting to make the proxy for an AOP proxied
service
