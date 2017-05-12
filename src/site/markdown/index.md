[//]: # " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. "
[//]: # "  "
[//]: # " Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved. "
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

<p><var class="icon-ok-sign" style="color: #7F7F7F"></var>
HK2 is an implementation of JSR-330 in a JavaSE environment.
<br/><a href="http://jcp.org/aboutJava/communityprocess/final/jsr330/">JSR-330</a> defines services and injection points that can be dynamically discovered at runtime and which allow for Inversion of Control (IoC) and dependency injection (DI).</p>
<p><var class="icon-ok-sign" style="color: #7F7F7F"></var>
HK2 provides an API for control over its operation and has the ability to automatically load services into the container.
<br/>It is the foundation for the GlassFish V3 and V4 application servers as well as other products.
</p><p><var class="icon-ok-sign" style="color: #7F7F7F"></var>
HK2 also has powerful features that can be used to perform tasks such as looking up services or customizing you injections, as well as several extensibility
features allowing the users to connect with other container technologies<br/>
<br/>The following list gives an overview of some of the things that can be customized or extended with HK2:
<ul>
<li>Custom lifecycles and scopes</li>
<li>Events</li>
<li>AOP and other proxies</li>
<li>Custom injection resolution</li>
<li>Assisted injection</li>
<li>Just In Time injection resolution</li>
<li>Custom validation and security</li>
<li>Run Level Services</li>
</ul>
</p>

---

<h2><a class="headerlink" href="getting-started.html">
    <var class="icon-compass"></var> Getting started!
</a></h2>
Read the [introduction](introduction.html) and [get started][gettingstarted] with HK2.


<h2><a class="headerlink" href="api-overview.html">
    <var class="icon-book"></var> API
</a></h2>
[Learn][api] more about the HK2 API, or [browse][javadoc] the javadoc.

<h2><a class="headerlink" href="extensibility.html">
    <var class="icon-cloud-download"></var> Features
</a></h2>
[Learn][extensibility] more about the features of HK2


<h2><a class="headerlink" href="integration.html">
    <var class="icon-tags"></var> Integration
</a></h2>
HK2 is well integrated with [GlassFish][glassfish], [Spring][spring] and others !

[api]: api-overview.html
[gettingstarted]: getting-started.html
[contpage]: contribute.html
[spring]: http://www.springsource.org
[glassfish]: http://glassfish.org
[javadoc]: apidocs/index.html
[extensibility]: extensibility.html
