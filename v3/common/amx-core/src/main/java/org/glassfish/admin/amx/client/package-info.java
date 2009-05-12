/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
/**
Contains classes and interfaces concerned with connectivity to the
Appserver</p>
<p><big><span style="font-weight: bold;"><font size="+1">Connecting to
the Domain Admin Server (DAS)</font></span></big><a name="Connecting"
 style="font-weight: bold;"></a><br>
<br>
AMX supports connection to the DAS only; it does not support
connections to individual server instances.&nbsp; This makes it simple
to interact with all servers, clusters, etc with a single connection.<br>
</p>
<p>To connect to the server, you will need to determine the following:<br>
</p>
<ul>
  <li> <span style="font-weight: bold;">hostname </span>of the Domain
Admin Server </li>
  <li> RMI administrative <span style="font-weight: bold;">port </span>of
the Domain Admin Server (not the http port used by the admin
GUI).&nbsp; The default is <span style="font-weight: bold;">8686</span>.
  </li>
  <li> admin <span style="font-weight: bold;">user </span></li>
  <li> admin <span style="font-weight: bold;">password </span></li>
  <li> whether TLS/SSL is <span style="font-weight: bold;">enabled or
not</span></li>
</ul>
<p>To determine the RMI admin port and whether TLS is enabled, you can
always view domain.xml. Look for the jmx-connector element; it should
look something like this:</p>
<p> <font face="Courier New, Courier, mono" size="-1">&lt;jmx-connector
accept-all="false" address="0.0.0.0" auth-realm-name="admin-realm"
enabled="true" name="system" <b>port="8686"</b> protocol="rmi_jrmp" <b>security-enabled="true"</b>&gt;</font></p>
<p>In the above example, security is enabled, so <font
 face="Courier New, Courier, mono" size="-1">useTLS</font> must be
true. The RMI administrative port is 8686.</p>
<p>Note that in an EE build, TLS is generally enabled, so <span
 style="font-family: monospace;">useTLS </span>must be true and the <i>trustStore</i>
and <i>truststorePassword</i> are required.&nbsp; Connections will
fail (or hang)&nbsp; if <span style="font-family: monospace;">useTLS </span>is
not set appropriately. In a PE build, TLS is not enabled by default.<br>
<span style="font-family: monospace;"><br style="font-weight: bold;">
</span>Once you have connected to the DAS via an <a
 href="AppserverConnectionSource.html">AppserverConnectionSource</a>
call <a
 href="../DomainRoot.html">getDomainRoot()</a>
to get an instance of DomainRoot.&nbsp; All further interfaces may be
obtained from DomainRoot, directly or indirectly.<br>

 */
@Taxonomy(stability = Stability.UNCOMMITTED)
package org.glassfish.admin.amx.client;

import org.glassfish.admin.amx.annotation.Taxonomy;
import org.glassfish.admin.amx.annotation.Stability;



