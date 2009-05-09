# stop in case it's already there
stop-mbean-server test-mbs
close-connection test-mbs
start-mbean-server test-mbs
connect test-mbs

# register our two test MBeans
register-mbean --class com.sun.cli.jmxcmd.support.testee.CLISupportTestee jmxcmd.test:name=cli-support-testee
register-mbean --class com.sun.cli.jmxcmd.support.testee.CLISupportSimpleTestee jmxcmd.test:name=cli-support-simple-testee
