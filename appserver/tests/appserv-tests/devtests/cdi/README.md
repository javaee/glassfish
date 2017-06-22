[//]: # " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. "
[//]: # "  "
[//]: # " Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " The contents of this file are subject to the terms of either the GNU "
[//]: # " General Public License Version 2 only (''GPL'') or the Common Development "
[//]: # " and Distribution License(''CDDL'') (collectively, the ''License'').  You "
[//]: # " may not use this file except in compliance with the License.  You can "
[//]: # " obtain a copy of the License at "
[//]: # " https://oss.oracle.com/licenses/CDDL+GPL-1.1 "
[//]: # " or LICENSE.txt.  See the License for the specific "
[//]: # " language governing permissions and limitations under the License. "
[//]: # "  "
[//]: # " When distributing the software, include this License Header Notice in each "
[//]: # " file and include the License file at LICENSE.txt. "
[//]: # "  "
[//]: # " GPL Classpath Exception: "
[//]: # " Oracle designates this particular file as subject to the ''Classpath'' "
[//]: # " exception as provided by Oracle in the GPL Version 2 section of the License "
[//]: # " file that accompanied this code. "
[//]: # "  "
[//]: # " Modifications: "
[//]: # " If applicable, add the following below the License Header, with the fields "
[//]: # " enclosed by brackets [] replaced by your own identifying information: "
[//]: # " ''Portions Copyright [year] [name of copyright owner]'' "
[//]: # "  "
[//]: # " Contributor(s): "
[//]: # " If you wish your version of this file to be governed by only the CDDL or "
[//]: # " only the GPL Version 2, indicate your decision by adding ''[Contributor] "
[//]: # " elects to include this software in this distribution under the [CDDL or GPL "
[//]: # " Version 2] license.''  If you don't indicate a single choice of license, a "
[//]: # " recipient has the option to distribute your version of this file under "
[//]: # " either the CDDL, the GPL Version 2 or to extend the choice of license to "
[//]: # " its licensees as provided above.  However, if you add GPL Version 2 code "
[//]: # " and therefore, elected the GPL Version 2 license, then the option applies "
[//]: # " only if the new code is made subject to such option by the copyright "
[//]: # " holder. "

CDI developer tests README
==========================

To checkout CDI devtests
------------------------
- checkout CDI developer tests using the following commands:
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests
cd appserv-tests #this is the directory set later to APS_HOME
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/config
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/lib
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/util
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests
cd devtests
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/cdi

Test setup
----------
- set S1AS_HOME, APS_HOME as appropriate
export APS_HOME=<appserv-tests> directory
export S1AS_HOME=<GlassFish Installation> directory
- start GlassFish
$S1AS_HOME/bin/asadmin start-domain domain1
- start Derby
$S1AS_HOME/bin/asadmin start-database

To run all CDI developer tests
------------------------------
- cd $APS_HOME/devtests/cdi
- ant all
- results can be found at APS_HOME/test_results.html


Test setup teardown
-------------------
- stop GlassFish
$S1AS_HOME/bin/asadmin stop-domain domain1
- asadmin stop-database
$S1AS_HOME/bin/asadmin stop-database

To run a single CDI developer test
----------------------------------
- after performing tasks under "Test setup"
- cd $APS_HOME/devtests/cdi/[test-dir]
- ant all
- perform tasks listed under "Test setup teardown"

To run CDI developer test suite with Security Manager on
---------------------------------------------------------
- start domain and enable security manager by 
asadmin create-jvm-options -Djava.security.manager 
- stop domain
- Add the following permission block to $S1AS_HOME/domains/domain1/config/server.policy
grant codeBase "file:${com.sun.aas.instanceRoot}/applications/-" {
    permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
};
- restart domain
- run tests


