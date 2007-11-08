#
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

alias jbidebug='$S1AS_HOME/bin/asadmin create-jvm-options --port 4848 -Dcom.sun.jbi.jsf.DEBUG=true'
alias jbinoscaffold='$S1AS_HOME/bin/asadmin delete-jvm-options --port 4848 -Dcom.sun.jbi.jsf.scaf.SCAFFOLD=true:-Dcom.sun.jsftemplating.DEBUG=true'
alias jbirebuild='(cd $APS_HOME/../admin-gui;ant -Dglassfish.home=$S1AS_HOME clean dev-rebuild)'
alias jbiredeploy='$S1AS_HOME/bin/asadmin deploydir --contextroot admin-jsf --name admin-jsf --port 4848 $APS_HOME/../admin-gui/src/docroot'
alias jbiremote='$S1AS_HOME/bin/asadmin create-jvm-options --port 4848 -Dcom.sun.jbi.jsf.scaf.JMXHOST=localhost:-Dcom.sun.jbi.jsf.scaf.JMXPORT=$JMXPORT'
alias jbirestart='$S1AS_HOME/bin/asadmin stop-domain domain1;$S1AS_HOME/bin/asadmin start-domain domain1'
alias jbiscaffold='$S1AS_HOME/bin/asadmin create-jvm-options --port 4848 -Dcom.sun.jbi.jsf.scaf.SCAFFOLD=true:-Dcom.sun.jsftemplating.DEBUG=true'
alias jbitest='(cd $APS_HOME/../admin-gui;ant -Dhtmlunit.home=$HTMLUNIT_HOME -Drhino.home=$RHINO_HOME jbi-test;head -2 build/jbi/TE*.txt)'
alias jbiitest='(cd $APS_HOME/../admin-gui;ant -Dhtmlunit.home=$HTMLUNIT_HOME -Drhino.home=$RHINO_HOME -Djunit.home=$APS_HOME/lib jbi-itest)'
alias jbitestcluster='(cd $APS_HOME/../admin-gui;ant -Dhtmlunit.home=$HTMLUNIT_HOME -Drhino.home=$RHINO_HOME jbi-test-cluster;head -2 build/jbi/TE*.txt)'
alias jbiitestcluster='(cd $APS_HOME/../admin-gui;ant -Dhtmlunit.home=$HTMLUNIT_HOME -Drhino.home=$RHINO_HOME -Djunit.home=$APS_HOME/lib jbi-itest-cluster)'
