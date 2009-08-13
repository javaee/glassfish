source --verbose ./std-cmd-aliases.mc
source --verbose ./start-test-mbs.mc

# setup our environment
setenv debug=false
cca delim="echo -- --------------------------------------------------------------------------------"
cta -k simple-testee=mc.test:name=cli-support-simple-testee
cta -k support-testee=mc.test:name=cli-support-testee
# run our unit tests first
run-all-tests
load-jmxmp-connector --port 8344 
load-jmxmp-connector --port 8345 test:type=connector,protocol=jmxmp
load-html-adapter --port 7344
load-html-adapter --port 7345 test:type=adapter,protocol=html


# test monitoring
define-monitor --name fooMon --attributes * --interval 1 --stdout mon.tmp *
start-monitor fooMon
list-monitors

# test listening
define-listener --name fooListener --stdout listen.tmp *
start-listener fooListener
startNotif: simple-testee
list-listeners


#test the 'find' command
find simple-testee
find --add support-testee
find --remove support-testee
find --add *
find --current
find --terse last-found
find --wild-prop *prot*=*jmx* *
find --jregex-prop .*prot.*=.*jmx.* *
find --attributes *Millis* *
find --operations *Par* *




# test command aliases
delim
delete-cmd-alias foo
create-cmd-alias foo=echo
list-cmd-aliases

# test 'target'
target *
target
clear-target


# test mbean serves
start-mbean-server foo
list-mbean-servers
stop-mbean-server foo


#test history
clear-history
domains
repeat 1
!!
!dom
!!!
!!!!
repeat
domains
count
history 2
history


# test inspect
delim
target *
inspect --no-description
delim
inspect --all simple-testee
delim
inspect --operations * simple-testee
delim
inspect --operations *foo* --include-empty 
delim
inspect --attributes *
delim
inspect --constructors
delim
inspect --notifications
delim
inspect --summary


#test env commands
setenv foo=bar
env -a f
unsetenv foo=bar
setenv foo=
unsetenv foo

# test the get/set commands
delim
get --verbose * *
get *Current* simple-testee
get NotifMillis simple-testee
get *r
get *w
set NotifMillis=1000 simple-testee

# misc
delim
show-providers
version
-V
--version
delim
target *
validate-mbeans --print-stack-traces
validate-mbeans --attributes
validate-mbeans --operations
validate-mbeans --mbean-info
validate-mbeans --warnings-off

# test invoke
delim
target simple-testee
testString:hello
testString(hello)
simple-testee.testString(hello)

# test the help command
delim
help target
-?
--help
help foo:
help foo()
delim

stop-monitor fooMon
delete-monitor fooMon
stop-listener fooListener
delete-listener fooListener
#--------------------------------------------------------

clear-history
echo "\n\n\n\n\n"
echo "TEST DONE--NO APPARENT ERRORS"


