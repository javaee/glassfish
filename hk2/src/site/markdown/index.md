<p style="margin-bottom: 40px;"><var class="icon-ok-sign" style="color: #7F7F7F"></var>
HK2 is JSR-330 compliant and  has useful utilities for marking classes as services and interfaces as contracts.
<br/><a href="http://jcp.org/aboutJava/communityprocess/final/jsr330/">JSR-330</a> defines services and injection points that can be dynamically discovered at runtime and which allow for Inversion of Control (IoC) and dependency injection (DI).
</p>
<p><var class="icon-ok-sign" style="color: #7F7F7F"></var>
HK2 provides an API for fine control over its operation and has the ability to automatically load services into the application.
<br/>It is the foundation for the GlassFish V3 and V4 application servers as well as other products.
</p>
<p><var class="icon-ok-sign" style="color: #7F7F7F"></var>
HK2 also has a powerful API that can be used to perform several tasks such as binding service descriptions into the system and looking up services, as well as several extensibility features allowing the users to customize or change the behavior of HK2.<br/>
<br/>The following list gives an overview of some of the things that can be customized or extended with HK2:
<ul>
<li>Custom scopes</li>
<li>The use of proxies</li>
<li>Custom injection resolution</li>
<li>Assisted injection</li>
<li>Just In Time injection resolution</li>
<li>Custom validation and security</li>
</ul>
</p>

---

<h2><a class="headerlink" href="develop.htm">
    <var class="icon-compass"></var> Getting started!
</a></h2>
[Learn][develop] how to use HK2 in your projects.


<h2><a class="headerlink" href="api.htm">
    <var class="icon-book"></var> API
</a></h2>
[Learn][api] more about the HK2 API, or [browse][javadoc] the javadoc.

<h2><a class="headerlink" href="extensibility.html">
    <var class="icon-cloud-download"></var> Extensibility
</a></h2>
[Learn][extensibility] more about the extensibility features of HK2


<h2><a class="headerlink" href="integration.html">
    <var class="icon-tags"></var> Integration
</a></h2>
HK2 is well integrated with [GlassFish][glassfish], [Guice][guice] and others !

[api]: api.html
[develop]: develop.html
[contpage]: contribute.html
[guice]: http://code.google.com/p/google-guice/
[glassfish]: http://glassfish.org
[javadoc]: apidocs/index.html
[extensibility]: extensibility.html
