
<sc:subMenu value="Deploy" id="deployMenu">
<sc:menuItem value="Enterprise Application" url="#{request.contextPath}/layouttest.jsf?page=Enterprise+Application" />
<sc:menuItem value="Web Application" url="#{request.contextPath}/web/webApp/upload.jsf?appType=webApp" />
<sc:menuItem value="EJB Module" url="#{request.contextPath}/layouttest.jsf?page=EJB+Module" />
<sc:menuItem value="Connector Module" url="#{request.contextPath}/layouttest.jsf?page=Connector+Module" />
<sc:menuItem value="Lifecycle Module" url="#{request.contextPath}/layouttest.jsf?page=Lifecycle+Module" />
<sc:menuItem value="App Client Module" url="#{request.contextPath}/layouttest.jsf?page=App+Client+Module" />
<sc:menuItem value="Custom MBean" url="#{request.contextPath}/layouttest.jsf?page=Custom+MBean" />

<sc:subMenu value="JBI" id="jbiMenu">
<sc:menuItem value="Component" url="#{request.contextPath}/layouttest.jsf?page=Component" />
<sc:menuItem value="Shared Library" url="#{request.contextPath}/layouttest.jsf?page=Shared+Library" />
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
	<sc:menuItem value="Enterprise Applications" url="#{request.contextPath}/layouttest.jsf?page=Enterprise+Applications" />
	<sc:menuItem value="Web Applications" url="#{request.contextPath}/web/webApp/webApplications.jsf" />
	<sc:menuItem value="EJB Modules" url="#{request.contextPath}/layouttest.jsf?page=EJB+Modules" />
	<sc:menuItem value="Lifecycle Modules" url="#{request.contextPath}/layouttest.jsf?page=Lifecycle+Modules" />
	<sc:menuItem value="Application Client Modules" url="#{request.contextPath}/layouttest.jsf?page=Application+Client+Modules" />
    </sc:subMenu>
    <sc:subMenu value="Application Server">
    <sc:menuItem value="General"  url="#{request.contextPath}/appServer/serverInstGeneralPe.jsf" />
	<sc:menuItem value="JVM Settings" url="#{request.contextPath}/appServer/serverInstJvmGeneral.jsf" />
	<sc:menuItem value="Logger Settings" />
	<sc:menuItem value="Thread Pools" />
    </sc:subMenu>
    <sc:subMenu value="Service">
	<sc:menuItem value="Web Container" url="#{request.contextPath}/layouttest.jsf?page=Web+Container" />
	<sc:menuItem value="EJB Container" url="#{request.contextPath}/layouttest.jsf?page=EJB+Container" />
	<sc:menuItem value="JMS" url="#{request.contextPath}/layouttest.jsf?page=JMS" />
	<sc:menuItem value="Availability Service" url="#{request.contextPath}/layouttest.jsf?page=Availability+Service" />
	<sc:menuItem value="Transaction Service" url="#{request.contextPath}/layouttest.jsf?page=Transaction+Service" />
	<sc:menuItem value="Admin Service" url="#{request.contextPath}/layouttest.jsf?page=Admin+Service" />
	<sc:menuItem value="Connector Service" url="#{request.contextPath}/layouttest.jsf?page=Connector+Service" />
	<sc:menuItem value="Diagnostic Service" url="#{request.contextPath}/layouttest.jsf?page=Diagnostic+Service" />
	<sc:subMenu value="Security">
	    <sc:menuItem value="Realm" url="#{request.contextPath}/layouttest.jsf?page=Realm" />
	    <sc:menuItem value="JACC Provider" url="#{request.contextPath}/layouttest.jsf?page=JACC+Provider" />
	    <sc:menuItem value="Thread Pools" url="#{request.contextPath}/layouttest.jsf?page=Thread+Pools" />
	</sc:subMenu>
	<sc:subMenu value="HTTP Service">
	    <sc:menuItem value="HTTP Listeners" url="#{request.contextPath}/layouttest.jsf?page=HTTP+Listeners" />
	    <sc:menuItem value="Virtual Servers" url="#{request.contextPath}/layouttest.jsf?page=Virtual+Servers" />
	</sc:subMenu>
	<sc:subMenu value="Object Request Broker">
	    <sc:menuItem value="IIOP Listeners" url="#{request.contextPath}/layouttest.jsf?page=IIOP+Listeners" />
	</sc:subMenu>
    </sc:subMenu>
    <sc:menuItem value="Clusters" url="#{request.contextPath}/layouttest.jsf?page=Clusters" />
    <sc:menuItem value="Management Rules" url="#{request.contextPath}/layouttest.jsf?page=Management+Rules" />
    <sc:menuItem value="Web Services" url="#{request.contextPath}/layouttest.jsf?page=Web+Services" />
    <sc:menuItem value="HTTP Load Balancers" url="#{request.contextPath}/layouttest.jsf?page=HTTP+Load+Balancers" />
    <sc:menuItem value="Stand-Alone Instances" url="#{request.contextPath}/layouttest.jsf?page=Stand-Alone Instances" />
    <sc:menuItem value="Node Agent References" url="#{request.contextPath}/layouttest.jsf?page=Node+Agent+References" />
    <sc:subMenu value="JBI">
	<sc:menuItem value="Service Assemblies" url="#{request.contextPath}/layouttest.jsf?page=Service+Assemblies" />
	<sc:menuItem value="Components" url="#{request.contextPath}/layouttest.jsf?page=Components" />
	<sc:menuItem value="Shared Libraries" url="#{request.contextPath}/layouttest.jsf?page=Shared+Libraries" />
    </sc:subMenu>
    <sc:menuItem value="Configuration System Properties" url="#{request.contextPath}/layouttest.jsf?page=Configuration+System+Properties" />
    <sc:menuItem value="Custom MBeans" url="#{request.contextPath}/layouttest.jsf?page=Custom+MBeans" />
    <sc:subMenu value="Resources">
	<sc:menuItem value="JDBC Resources" url="#{request.contextPath}/layouttest.jsf?page=JDBC+Resources" />
	<sc:menuItem value="JDBC Connection Pools" url="#{request.contextPath}/layouttest.jsf?page=JDBC+Connection+Pools" />
	<sc:menuItem value="JMS Connection Factories" url="#{request.contextPath}/layouttest.jsf?page=JMS+Connection+Factories" />
	<sc:menuItem value="JMS Destination Resources" url="#{request.contextPath}/layouttest.jsf?page=JMS+Destination+Resources" />
	<sc:menuItem value="JavaMail Sessions" url="#{request.contextPath}/layouttest.jsf?page=JavaMail+Sessions" />
	<sc:menuItem value="JNDI Custom Resources" url="#{request.contextPath}/layouttest.jsf?page=JNDI+Custom+Resources" />
	<sc:menuItem value="JNDI External Resources" url="#{request.contextPath}/layouttest.jsf?page=JNDI+External+Resources" />
	<sc:subMenu value="Connectors">
	    <sc:menuItem value="Connector Resources" url="#{request.contextPath}/layouttest.jsf?page=Connector+Resources" />
	    <sc:menuItem value="Connection Pools" url="#{request.contextPath}/layouttest.jsf?page=Connection+Pools" />
	    <sc:menuItem value="Admin Object Resources" url="#{request.contextPath}/layouttest.jsf?page=Admin+Object+Resources" />
	</sc:subMenu>
    </sc:subMenu>
</sc:subMenu>
<!--
<sc:subMenu value="View">
    <sc:menuItem value="Bookmarkable URL" url="#{request.contextPath}/layouttest.jsf?page=Bookmarkable+URL" />
</sc:subMenu>
-->
<sc:subMenu value="Tools">
    <sc:menuItem value="Site Map" url="#{request.contextPath}/layouttest.jsf?page=Site+Map" />
    <sc:menuItem value="Common Tasks Page" url="#{request.contextPath}/commonTask.jsf" />
    <sc:menuItem value="Performance Advisor" url="#{request.contextPath}/layouttest.jsf?page=Performance+Advisor" />
    <sc:menuItem value="Update Center" url="#{request.contextPath}/updateCenter/installed.jsf" />
    <sc:menuItem value="Edit Tags" url="#{request.contextPath}/layouttest.jsf?page=Edit+Tags" />
    <sc:menuItem value="Preferences..." url="#{request.contextPath}/layouttest.jsf?page=Preferences..." />
</sc:subMenu>
<sc:subMenu value="Monitor">
    <sc:menuItem value="Dashboard" url="#{request.contextPath}/layouttest.jsf?page=Dashboard" />
    <sc:menuItem value="Web Service" url="#{request.contextPath}/layouttest.jsf?page=Web+Service" />
    <sc:menuItem value="Call Flow" url="#{request.contextPath}/layouttest.jsf?page=Call+Flow" />
    <sc:menuItem value="Configure Monitoring" url="#{request.contextPath}/layouttest.jsf?page=Configure+Monitoring" />
</sc:subMenu>
<!--
    Favorite Tags? Or do we want to bookmark a specific page? Both?
<sc:subMenu value="Bookmarks">
    <sc:menuItem value="Bookmark This Page" url="#{request.contextPath}/layouttest.jsf?page=Bookmark+This+Page" />
    <sc:menuItem value="Manage Bookmarks" url="#{request.contextPath}/layouttest.jsf?page=Manage+Bookmarks" />
</sc:subMenu>
-->
<sc:subMenu value="Help">
    <sc:menuItem value="Online Help" url="#{request.contextPath}/layouttest.jsf?page=Online+Help" />
    <sc:menuItem value="Support" url="#{request.contextPath}/layouttest.jsf?page=Support" />
    <sc:menuItem value="Forums" url="#{request.contextPath}/layouttest.jsf?page=Forums" />
    <sc:menuItem value="Report a Problem" url="#{request.contextPath}/layouttest.jsf?page=Report+a+Problem" />
    <!-- FIXME: registration link should toggle w/ rss page -->
    <sc:menuItem value="Register" url="#{request.contextPath}/sysnet/registration.jsf" />
    <sc:menuItem value="About Plugins..." url="#{request.contextPath}/layouttest.jsf?page=About+Plugins..." />
    <sc:menuItem value="About..." url="#{request.contextPath}/layouttest.jsf?page=About..." />
</sc:subMenu>
