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
