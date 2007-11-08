How to build and install the Async examples:

(1) First, install GlassFish.
(2) Start the admin-gui, and go under Resources > JavaMail Session
(3) Create a new one, and enter:

JNDI Name: mail/MailSession
Mail Host: pop.gmail.com [any pop3s account]
Defaul User: user name account
Default Return" your email account.
Session: Check enabled.

Advanced:

Store Protocol: pop3
Store Protocol Class: com.sun.mail.imap.POP3Store

(4) Stop GlassFish.

(5) Update GlassFish jars

% cd glassfish/appserv-http-engine
% maven build-examples

This will add the JavaMailAsyncFilter code to GlasFish main jars. 

(6) Add you email account information in web.xml 

Edit src/examples/org/glassfish/grizzly/async/servlet/web.xml

% maven build-servlet

It will also produce a war file called: asyncJavaMail.war

(6) edit S1AS_HOME/domains/domain1/conf/domain.xml, and add under the java-options section the following:

Under <http-listener ...id="http-listener-1", add:

<property name="reader-threads" value="5"/>

example:

<http-listener acceptor-threads="1" address="0.0.0.0" blocking-enabled="false" default-virtual-server="server" enabled="true" family="inet" id="http-listener-1" port="8080" security-enabled="false" server-name="" xpowered-by="true">
    <property name="reader-threads" value="5"/>
</http-listener>

and under the <java-config..> elements:

<jvm-options>-Dcom.sun.enterprise.web.connector.grizzly.asyncHandlerClass=com.sun.enterprise.web.connector.grizzly.async.DefaultAsyncHandler</jvm-options>

<jvm-options>-Dcom.sun.enterprise.web.connector.grizzly.asyncHandler.ports=8080</jvm-options>

<jvm-options>-Dcom.sun.enterprise.web.connector.grizzly.asyncFilters=org.glassfish.grizzly.async.javamail.JavaMailAsyncFilter</jvm-options>

This will tell Grizzly to start in ARP.

(7) Start GlassFish

(8) Deploy the asyncJavaMail.war

(9) Invoke http://localhost:8080/asyncJavaMail/GetMail







