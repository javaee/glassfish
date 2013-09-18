This directory contains unit tests for the hk2-locator module that needed
to be separated from the hk2-locator directory itself.  At the time
of writing this is due to the fact that the test code needs to perform
operations that would normally require security checks (creating ClassLoaders
and getting/setting the ContextClassLoaders).  Since we want to keep the
set of security permissions granted in hk2-locator to a minimum, these
tests were moved here in order to run without the security manager.
