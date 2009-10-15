#--- Building 'mx' ---
# create the bin directory with everything in it
# this should be added to pom.xml, build help anyone?
# note that the script assumes that V3 modules are in /v3/glassfishv3/glassfish/modules
mvn install
./mk
