
<sc:subMenu value="Deploy" id="deployMenu">
<sc:menuItem value="Enterprise Application" url="#" />
<sc:menuItem value="Web Application" url="/web/webApp/upload.jsf?appType=webApp" />
<sc:menuItem value="EJB Module" url="#" />
<sc:menuItem value="Connector Module" url="#" />
<sc:menuItem value="Lifecycle Module" url="#" />
<sc:menuItem value="App Client Module" url="#" />
<sc:menuItem value="Custom MBean" url="#" />

<sc:subMenu value="JBI" id="jbiMenu">
<sc:menuItem value="Component" url="#" />
<sc:menuItem value="Shared Library" url="#" />
</sc:subMenu>

</sc:subMenu>

		<!--
		<sc:subMenu value="Create" id="createMenu">
		<sc:menuItem value="JDBC Resource" url="#" />
		<sc:menuItem value="JDBC Connection Pool" url="#" />

		<sc:subMenu value="JMS" id="jmsMenu">
		<sc:menuItem value="JMS Connection Factory" url="#" />
		<sc:menuItem value="JMS Destination Resource" url="#" />
		<sc:menuItem value="JMS Host" url="#" />
		<sc:menuItem value="JMS Physical Destination" url="#" />
		</sc:subMenu>

		<sc:menuItem value="JavaMail Session" url="#" />
		<sc:menuItem value="JNDI Custom Resource" url="#" />
		<sc:menuItem value="JNDI External Resource" url="#" />

		<sc:subMenu value="Connectors" id="connectorsMenu">
		<sc:menuItem value="Connection Resource" url="#" />
		<sc:menuItem value="Connection Pool" url="#" />
		<sc:menuItem value="Admin Object Resource" url="#" />
		</sc:subMenu>

		<sc:subMenu value="Security" id="securityMenu">
		<sc:menuItem value="Realm" url="#" />
		<sc:menuItem value="JACC Provider" url="#" />
		<sc:menuItem value="Audit Module" url="#" />
		<sc:menuItem value="Message Security Configuration" url="#" />
		</sc:subMenu>

		<sc:menuItem value="HTTP Listener" url="#" />
		<sc:menuItem value="Virtual Server" url="#" />
		<sc:menuItem value="IIOP Listener" url="#" />
		<sc:menuItem value="Thread Pool" url="#" />
		<sc:menuItem value="Management Rule" url="#" />
		<sc:menuItem value="Node Agent Reference" url="#" />
		<sc:menuItem value="Cluster" url="#" />
		<sc:menuItem value="HTTP Load Balancer" url="#" />
		</sc:subMenu>
		-->

<sc:subMenu value="Edit" id="editMenu">
    <sc:subMenu value="Applications" id="applications">
	<sc:menuItem value="Enterprise Applications" />
	<sc:menuItem value="Web Applications" url="/web/webApp/webApplications.jsf" />
	<sc:menuItem value="EJB Modules" />
	<sc:menuItem value="Lifecycle Modules" />
	<sc:menuItem value="Application Client Modules" />
    </sc:subMenu>
    <sc:subMenu value="Application Server">
	<sc:menuItem value="JVM Settings" />
	<sc:menuItem value="Logger Settings" />
	<sc:menuItem value="Thread Pools" />
    </sc:subMenu>
    <sc:subMenu value="Service">
	<sc:menuItem value="Web Container" url="/web/configuration/webContainerSession.jsf" />
    <sc:menuItem value="HTTP Service" url="/web/configuration/httpService.jsf" />
    <sc:menuItem value="HTTP Listeners" url="/web/configuration/httpListeners.jsf" />
    <sc:menuItem value="Virtual Servers" url="/web/configuration/virtualServers.jsf" />
	<sc:menuItem value="EJB Container" />
	<sc:menuItem value="JMS" />
	<sc:menuItem value="Availability Service" />
	<sc:menuItem value="Transaction Service" />
	<sc:menuItem value="Admin Service" />
	<sc:menuItem value="Connector Service" />
	<sc:menuItem value="Diagnostic Service" />
	<sc:subMenu value="Security">
	    <sc:menuItem value="Realm" />
	    <sc:menuItem value="JACC Provider" />
	    <sc:menuItem value="Thread Pools" />
	</sc:subMenu>
	<sc:subMenu value="Object Request Broker">
	    <sc:menuItem value="IIOP Listeners" />
	</sc:subMenu>
    </sc:subMenu>
    <sc:menuItem value="Clusters" />
    <sc:menuItem value="Management Rules" />
    <sc:menuItem value="Web Services" />
    <sc:menuItem value="HTTP Load Balancers" />
    <sc:menuItem value="Stand-Alone Instances" />
    <sc:menuItem value="Node Agent References" />
    <sc:subMenu value="JBI">
	<sc:menuItem value="Service Assemblies" />
	<sc:menuItem value="Components" />
	<sc:menuItem value="Shared Libraries" />
    </sc:subMenu>
    <sc:menuItem value="Configuration System Properties" />
    <sc:menuItem value="Custom MBeans" />
    <sc:subMenu value="Resources">
	<sc:menuItem value="JDBC Resources" />
	<sc:menuItem value="JDBC Connection Pools" />
	<sc:menuItem value="JMS Connection Factories" />
	<sc:menuItem value="JMS Destination Resources" />
	<sc:menuItem value="JavaMail Sessions" />
	<sc:menuItem value="JNDI Custom Resources" />
	<sc:menuItem value="JNDI External Resources" />
	<sc:subMenu value="Connectors">
	    <sc:menuItem value="Connector Resources" />
	    <sc:menuItem value="Connection Pools" />
	    <sc:menuItem value="Admin Object Resources" />
	</sc:subMenu>
    </sc:subMenu>
</sc:subMenu>
<!--
<sc:subMenu value="View">
    <sc:menuItem value="Bookmarkable URL" />
</sc:subMenu>
-->
<sc:subMenu value="Tools">
    <sc:menuItem value="Site Map" />
    <sc:menuItem value="Common Tasks Page" url="/commonTask.jsf" />
    <sc:menuItem value="Performance Advisor" />
    <sc:menuItem value="Update Center" url="/updateCenter/installed.jsf" />
    <sc:menuItem value="Edit Tags" />
    <sc:menuItem value="Preferences..." />
</sc:subMenu>
<sc:subMenu value="Monitor">
    <sc:menuItem value="Dashboard" />
    <sc:menuItem value="Web Service" />
    <sc:menuItem value="Call Flow" />
    <sc:menuItem value="Configure Monitoring" />
</sc:subMenu>
<!--
    Favorite Tags? Or do we want to bookmark a specific page? Both?
<sc:subMenu value="Bookmarks">
    <sc:menuItem value="Bookmark This Page" />
    <sc:menuItem value="Manage Bookmarks" />
</sc:subMenu>
-->
<sc:subMenu value="Help">
    <sc:menuItem value="Online Help" />
    <sc:menuItem value="Support" />
    <sc:menuItem value="Forums" />
    <sc:menuItem value="Report a Problem" />
    <!-- FIXME: registration link should toggle w/ rss page -->
    <sc:menuItem value="Register" url="/sysnet/registration.jsf" />
    <sc:menuItem value="About Plugins..." />
    <sc:menuItem value="About..." />
</sc:subMenu>
