#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

S1AS_HOME="C:\Sun\Appserver7"

JAVA_HOME=$(S1AS_HOME)/jdk

JAVA=$(JAVA_HOME)/bin/java
JAVAC=$(JAVA_HOME)/bin/javac
RMIC=$(JAVA_HOME)/bin/rmic

WORKDIR=classes
CLASSPATH=$(WORKDIR):$(S1AS_HOME)/lib/appserv-rt.jar:$(S1AS_HOME)/lib/j2ee.jar:$(S1AS_HOME)/lib/appserv-ext.jar

DELEGATES=-Djavax.rmi.CORBA.UtilClass=com.sun.corba.ee.impl.oa.poa.ShutdownUtilDelegate

ORB_PROPERTIES=\
	-Dorg.omg.CORBA.ORBClass=com.sun.corba.ee.impl.orb.ORBImpl \
	-Dorg.omg.CORBA.ORBSingletonClass=com.sun.corba.ee.impl.orb.ORBSingleton

INTERCEPTORS=-Dorg.omg.PortableInterceptor.ORBInitializerClass.corba.ORBInitializerImpl

LD_LIBRARY_PATH=$(S1AS_HOME)/lib

HOST=`hostname`
PORT=1050

all	: clean build run

clean	:
	- rm -rf $(WORKDIR)

build 	: compile stubs

compile	:
	mkdir -p $(WORKDIR)
	$(JAVAC) -classpath $(CLASSPATH) -d $(WORKDIR) *.java

stubs	:
	$(RMIC) -poa -iiop -classpath $(CLASSPATH) -d $(WORKDIR) \
	corba.RemoteTestImpl

run	: start-appserver runtest

#replace with command to start appserver
#temp using tnameserv
start-appserver :
	$(JAVA_HOME)/bin/tnameserv -ORBInitialPort $(PORT) &
	sleep 10

runtest :
	$(JAVA) -classpath $(CLASSPATH) $(ORB_PROPERTIES) $(DELEGATES) \
	$(INTERCEPTORS) -Djava.library.path=$(LD_LIBRARY_PATH) \
	-Dorg.omg.CORBA.ORBInitRef=NameService=corbaloc:iiop:1.2@$(HOST):$(PORT)/NameService \
	corba.Server corbaname::$(HOST):$(PORT)#RemoteTest

