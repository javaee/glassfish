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

            This is the JAXR-API project.

Important***
Required software to build JAXR-API 1.0_03
-----------------------------------

To be able to build JAXR-API 1.0_03 you need to have the
following software:
  
library dependencies for building jaxr module
---------------------------------------------
put these jars in JAXR-API_HOME/misc/lib 

1) activation.jar, 2) xml4j.jar, 3) xerces.jar, 4) soap.jar, 
5) servlet.jar, 6) parser.jar, 7) mail.jar, 8) jaxp.jar, 
9) breezetk.jar .

You can download
----------------

Tasks required before building the project.
===========================================
    1)User should have jdk1.5.0 or above on this machine.
    2)user should have ant 1.6.5 installed on his machine.
 

  Run the following Ant task in the following order:
    
    clean           Clean away generated or copied files by removing build dir.

    compile         It will compile the source code. 
            
    javadoc         It will generate the javadocs for this project.

    package 	    It will generate the jaxr-api.jar file in ./build/lib dir. 
    
    
    
    
