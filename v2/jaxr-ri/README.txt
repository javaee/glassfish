#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the License).  You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the license at
# https://glassfish.dev.java.net/public/CDDLv1.0.html or
# glassfish/bootstrap/legal/CDDLv1.0.txt.
# See the License for the specific language governing
# permissions and limitations under the License.
#
# When distributing Covered Code, include this CDDL
# Header Notice in each file and include the License file
# at glassfish/bootstrap/legal/CDDLv1.0.txt.  
# If applicable, add the following below the CDDL Header,
# with the fields enclosed by brackets [] replaced by
# you own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Copyright 2007 Sun Microsystems, Inc. All rights reserved.
#

            This is the JAXR-RI'Reference Implementation' project.

Important***
Required software to build JAXR 1.0.6
-----------------------------------

To be able to build JAXR 1.0.6 you need to have the
following software:  
library dependencies for building jaxr module
---------------------------------------------
To be able to build JAXR 1.0.6 you need to have thefollowing jars

<>activation.jar , ant.jar , dom.jar , FastInfoset.jar , jaas.jar
jaxb1-impl.jar , jaxb-api.jar , jaxb-impl.jar , jaxb-xjc.jar
jsr173_1.0_api.jar ,  mail.jar , namespace.jar , optional.jar
relaxngDatatype.jar , saaj-api.jar , saaj-impl.jar , sax.jar ,
xercesImpl.jar , xsdlib.jar

You can download
----------------
    jaxp jars from jaxp.dev.java.net
    jaxb jars from jaxb.dev.java.net
    saaj jars from saaj.dev.java.net

    mail.jar and activation.jar from java.sun.com
    jaas.jar, FastInfoset.jar from java.sun.com

    ant.jar and optional.jar from apache.org

    namespace.jar, relaxngDatatype.jar, xsdlib.jar - from java.net
    jsr173_api jar from sjsxp.dev.java.net



Tasks required before building the project.

    1)User should have jdk1.5.0 or above on this machine.
    2)user should have ant 1.6.5 installed on his machine.
 

  Run the following Ant task in the following order:
    
    clean           Clean away generated or copied files by removing build dir.

    compile        It will compile the source code and generate the jaxr-Impl.jar file in
                    ./build/lib dir.
    
    samples         It will compile all the samples.     
    
    javadoc         It will generate the javadocs for this project.

Tasks before Running Samples
----------------------------
Important to run these Sample user need RegistryServer it can be UDDI or ebXML registry.    

Go to http://java.sun.com/webservices/downloads/1.5/index.html download jwsdp1.5 pack this jwsdp1.5 pack contain UDDI2.0 Registry.

1) Install jwsdp1.5
3) Install j2ee SDK  http://java.sun.com/javaee/downloads/index.jsp
2) Install the Registry Server as follows:

  a. Navigate to the directory <INSTALL>/jwsdp-1.5/registry-server/webapps.
  b. Copy the files RegistryServer.war and Xindice.war to the directory <JAVAEE_HOME>/domains/domain1/autodeploy/.
  c. Copy the file commons-logging.jar to the directory <JAVAEE_HOME>/lib.
  d. Start the Application Server.

Please refer readme files inside samples dir to know how to run them.
