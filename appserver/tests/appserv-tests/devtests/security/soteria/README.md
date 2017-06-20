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

This is integration devtests for JSR 375 RI(soteria).
The sample apps are taken from [Soteria](https://github.com/javaee-security-spec/soteria) repository.
Follow the below instructions to run the tests.
1. set APS_HOME to appserv-tests dir.
2. set M2_HOME to maven Home.
3. set S1AS_HOME to glassfish installation. The S1AS_HOME should contain glassfish directory.
4. $S1AS_HOME/bin/asadmin start-domain
5. Run mvn clean verify
6. $S1AS_HOME/bin/asadmin stop-domain

Known Issue:
1.Aruillian gf container: 
------------
Jun 05, 2017 3:50:22 PM org.jboss.arquillian.container.glassfish.clientutils.GlassFishClientUtil getResponseMap
SEVERE: exit_code: FAILURE, message: An error occurred while processing the request. Please see the server logs for details. [status: SERVER_ERROR reason: Service Unavailable]
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 6.173 s <<< FAILURE! - in org.glassfish.soteria.test.AppMemBasicIT
[ERROR] org.glassfish.soteria.test.AppMemBasicIT  Time elapsed: 6.165 s  <<< ERROR!
com.sun.jersey.api.container.ContainerException: exit_code: FAILURE, message: An error occurred while processing the request. Please see the server logs for details. [status: SERVER_ERROR reason: Service Unavailable]

Jun 05, 2017 3:50:22 PM org.jboss.arquillian.container.glassfish.managed_3_1.GlassFishServerControl$1 run
WARNING: Forcing container shutdown
Stopping container using command: [java, -jar, /media/sameerpandit/WLS/javaEE/tt/glassfish5/glassfish/../glassfish/modules/admin-cli.jar, stop-domain, -t]
------------

Resolve this by running the test with a fresh $S1AS_HOME.
