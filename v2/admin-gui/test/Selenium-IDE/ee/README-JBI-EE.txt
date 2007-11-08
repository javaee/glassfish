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

    Tasks Before Running Selenium Tests	for EE profile	(On JBI Nodes)
    ==================================================

    1)These tests assume that the user is logged into the console.
      So in order to run these tests you should be logged into the 
      glassfish cluster profile at https://localhost:4848

    2)These tests have been tested only on firefox2.0 browser with 
     selenium-ide which can be downloaded from
     http://www.openqa.org/selenium-ide/download.action
     Once the extension is installed launch the IDE as shown here
     http://wiki.openqa.org/download/attachments/400/Selenium+IDE.swf?version=1
     and load the tests.

     3) All Tests are intended to be run with locale en. As they assert presence of 
     text in english, these tests will definitely fail when console is brought up
     with any other non-english locale. 
     
    4)jbi-cluster-show-sa-comp-sl test needs at least one cluster in the setup. This tests
      test the show pages of Service assemblies, components and shared libraries. These
      pages are present in EE profile only and can be accessed from the cluster nodes.
      User needs to chose a cluster and then the JBI tab to see the Service Assemblies,
      Componetns and Shared Libraries tab.


    5)jbi-server-show-sa-comp-sl checks the show pages of topmost listed Service Assembly,
    Component and Library pages . These list pages can be reached by clicking Stand-alone
    Instances in left frame tree. Choose a server and click the JBI tab to see three tabs
    for Service Assemblies, Compoents and Shared Libraries.
    Basically it checks for presence of 3, 5 and 3 tabs  respectively on each Deployment, 
    Component and Library show pages.

    6)jbi-install-negative-sa-comp-sl tests the install wizard for Service Assemblies ,
    Components and  Shared Libraries when launched from child nodes of JBI Node. All the hardcoded
    paths need to be changed in your path before launching the test. Please replace 
    C:\gf-home-ee-0301  with your glassfish workspace. This test
    tests few negative use cases using the corrupt archives available in the $glassfish/admin-gui/
    test/data directory. This test tests both the upload and copy path and checks whether user
    gets right feedback in alert message or not. This test also tests whether the cancel button
    of all three wizards, first and second step work right

    7) Failures caused in Slenium TestRunner or IDE due to waitForCondition statements getting
    Timedout can be ignored . These statements have been introduced only to add delays to let
    the new page load completely.

    8)These tests are based on id of the components. In case there is a
    woodstock upgrade or developer had to change the id and did not update the test
    immediately, ids of corresponding components/widgets on the page
    could change . In case of failures please use xpath to modify them in local path
    to continue, if you encounter failures prohibiting them to complete. And also 
    please file abug    we will take care of it.

    9) Though the tests have been run repeatedly (Selenium TestRunner /Fastest speed)
    with success on same browser/machine.
    Let us know if you see any reproducible issues in tests repeatition
           