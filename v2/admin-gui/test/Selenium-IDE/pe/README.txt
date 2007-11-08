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

    Tasks Before Running Selenium Tests
    =====================================

    1)These tests assume that the user is logged into the console.
      So in order to run these tests you should be logged into the 
      glassfish developer profile at http://localhost:4848

    2)These tests have been tested only on firefox2.0 browser with 
     selenium-ide which can be downloaded from
     http://www.openqa.org/selenium-ide/download.action
     Once the extension is installed launch the IDE as shown here
     http://wiki.openqa.org/download/attachments/400/Selenium+IDE.swf?version=1
     and load the tests.
     
    3)You need to change the hardcoded path to JBI Archives in the test
    named install-negative-pe-sa-comp-sl file, as it points to archives in 
    test data directory in the your hard drive's glassfishworkspace. 
    This test assumes that two components (soap binding, javaSE) and
    one library(SunWSDL) is already installed and listed in JBI Tables .
    This tests checks for validation error messages:
    The validation error messages when user attempts to upload/copy empty/invalid
    jbi archives. 


    4)These tests are based on id of the components. In case there is a
    woodstock upgrade, ids of corresponding components/widgets on the page
    could change . In case of failures please use xpath to modify them in local path
    to continue, if you encounter failures prohibiting them to complete. And also 
    please file abug    we will take care of it.

    5) Though the tests have been run repeatedly with success on same browser/machine,
    Let us know if you see any reproducible issues in tests repeatition

    6) This tests have been tested with only Us english locale.

    