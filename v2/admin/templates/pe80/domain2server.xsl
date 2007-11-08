<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:transform xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
<xsl:output method="xml" version="1.0" doctype-system="%%%DTDREF%%%" doctype-public="-//Sun Microsystems Inc.//DTD Sun ONE Application Server 7.0//EN">
</xsl:output>

<xsl:template match="/domain">
 <xsl:element name = "server">
  <xsl:attribute name = "name">
   <xsl:value-of select = "servers/server[1]/@name"/>
  </xsl:attribute>
  <xsl:attribute name = "locale">
   <xsl:value-of select = "servers/server[1]/@locale"/>
  </xsl:attribute>
  <xsl:attribute name = "log-root">
   <xsl:value-of select = "servers/server[1]/@log-root"/>
  </xsl:attribute>
  <xsl:attribute name = "application-root">
   <xsl:value-of select = "servers/server[1]/@application-root"/>
  </xsl:attribute>
  <xsl:attribute name = "session-store">
   <xsl:value-of select = "servers/server[1]/@session-store"/>
  </xsl:attribute>
  <xsl:apply-templates/>
 </xsl:element>
</xsl:template>

<xsl:template match = "http-service">
 <xsl:element name = "http-service">
  <xsl:attribute name = "qos-metrics-interval-in-seconds">
   <xsl:value-of select = "@qos-metrics-interval-in-seconds"/>
  </xsl:attribute>
  <xsl:attribute name = "qos-recompute-time-interval-in-millis">
   <xsl:value-of select = "@qos-recompute-time-interval-in-millis"/>
  </xsl:attribute>
  <xsl:attribute name = "qos-enabled">
   <xsl:value-of select = "@qos-enabled"/>
  </xsl:attribute>
  <xsl:apply-templates select = "http-listener"/>
  <xsl:apply-templates select = "mime"/>
  <xsl:apply-templates select = "acl"/>
  <xsl:apply-templates select = "virtual-server-class"/>
  <xsl:apply-templates select = "http-qos"/>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "http-listener">
 <xsl:element name = "http-listener">
  <xsl:attribute name = "id">
   <xsl:value-of select = "@id"/>
  </xsl:attribute>
  <xsl:attribute name = "address">
   <xsl:value-of select = "@address"/>
  </xsl:attribute>
  <xsl:attribute name = "port">
   <xsl:value-of select = "@port"/>
  </xsl:attribute>
  <!-- Attribute family is not required -->
  <!--
  <xsl:attribute name = "family">
   <xsl:value-of select = "@family"/>
  </xsl:attribute>
  -->
  <xsl:attribute name = "acceptor-threads">
   <xsl:value-of select = "@acceptor-threads"/>
  </xsl:attribute>
  <xsl:attribute name = "blocking-enabled">
   <xsl:value-of select = "@blocking-enabled"/>
  </xsl:attribute>
  <xsl:attribute name = "security-enabled">
   <xsl:value-of select = "@security-enabled"/>
  </xsl:attribute>
  <xsl:attribute name = "default-virtual-server">
   <xsl:value-of select = "@default-virtual-server"/>
  </xsl:attribute>
  <xsl:attribute name = "server-name">
   <xsl:value-of select = "@server-name"/>
  </xsl:attribute>
  <xsl:attribute name = "enabled">
   <xsl:value-of select = "@enabled"/>
  </xsl:attribute>
  <xsl:apply-templates select = "ssl"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "ssl">
 <xsl:element name = "ssl">
  <xsl:attribute name = "cert-nickname">
   <xsl:value-of select = "@cert-nickname"/>
  </xsl:attribute>
  <xsl:attribute name = "ssl2-enabled">
   <xsl:value-of select = "@ssl2-enabled"/>
  </xsl:attribute>
  <xsl:attribute name = "ssl2-ciphers">
   <xsl:value-of select = "@ssl2-ciphers"/>
  </xsl:attribute>
  <xsl:attribute name = "ssl3-enabled">
   <xsl:value-of select = "@ssl3-enabled"/>
  </xsl:attribute>
  <xsl:attribute name = "ssl3-tls-ciphers">
   <xsl:value-of select = "@ssl3-tls-ciphers"/>
  </xsl:attribute>
  <xsl:attribute name = "tls-enabled">
   <xsl:value-of select = "@tls-enabled"/>
  </xsl:attribute>
  <xsl:attribute name = "tls-rollback-enabled">
   <xsl:value-of select = "@tls-rollback-enabled"/>
  </xsl:attribute>
  <xsl:attribute name = "client-auth-enabled">
   <xsl:value-of select = "@client-auth-enabled"/>
  </xsl:attribute>
  <xsl:apply-templates/>
 </xsl:element>
</xsl:template>

<xsl:template match = "mime">
 <xsl:element name = "mime">
  <xsl:attribute name = "id">
   <xsl:value-of select = "@id"/>
  </xsl:attribute>
  <xsl:attribute name = "file">
   <xsl:value-of select = "@file"/>
  </xsl:attribute>
  <xsl:apply-templates/>
 </xsl:element>
</xsl:template>

<xsl:template match = "acl">
 <xsl:element name = "acl">
  <xsl:attribute name = "id">
   <xsl:value-of select = "@id"/>
  </xsl:attribute>
  <xsl:attribute name = "file">
   <xsl:value-of select = "@file"/>
  </xsl:attribute>
  <xsl:apply-templates/>
 </xsl:element>
</xsl:template>

<xsl:template match = "virtual-server-class">
 <xsl:element name = "virtual-server-class">
  <xsl:attribute name = "id">
   <xsl:value-of select = "@id"/>
  </xsl:attribute>
  <xsl:attribute name = "config-file">
   <xsl:value-of select = "@config-file"/>
  </xsl:attribute>
  <!-- attribute default-object is not required -->
  <!--
  <xsl:attribute name = "default-object">
   <xsl:value-of select = "@default-object"/>
  </xsl:attribute>
  -->
  <xsl:attribute name = "accept-language">
   <xsl:value-of select = "@accept-language"/>
  </xsl:attribute>
  <xsl:attribute name = "enabled">
   <xsl:value-of select = "@enabled"/>
  </xsl:attribute>
  <xsl:apply-templates select = "virtual-server"/>
  <xsl:apply-templates select = "http-qos"/>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "virtual-server">
 <xsl:element name = "virtual-server">
  <xsl:attribute name = "id">
   <xsl:value-of select = "@id"/>
  </xsl:attribute>
  <xsl:attribute name = "http-listeners">
   <xsl:value-of select = "@http-listeners"/>
  </xsl:attribute>
  <xsl:attribute name = "default-web-module">
   <xsl:value-of select = "@default-web-module"/>
  </xsl:attribute>
  <xsl:attribute name = "config-file">
   <xsl:value-of select = "@config-file"/>
  </xsl:attribute>
  <!-- attribute default-object is not required -->
  <!--
  <xsl:attribute name = "default-object">
   <xsl:value-of select = "@default-object"/>
  </xsl:attribute>
  -->
  <xsl:attribute name = "hosts">
   <xsl:value-of select = "@hosts"/>
  </xsl:attribute>
  <xsl:attribute name = "mime">
   <xsl:value-of select = "@mime"/>
  </xsl:attribute>
  <xsl:attribute name = "state">
   <xsl:value-of select = "@state"/>
  </xsl:attribute>
  <xsl:attribute name = "acls">
   <xsl:value-of select = "@acls"/>
  </xsl:attribute>
  <xsl:attribute name = "accept-language">
   <xsl:value-of select = "@accept-language"/>
  </xsl:attribute>
  <!-- attribute default-object is not required -->
  <!--
  <xsl:attribute name = "log-file">
   <xsl:value-of select = "@log-file"/>
  </xsl:attribute>
  -->
  <xsl:apply-templates select = "http-qos"/>
  <xsl:apply-templates select = "auth-db"/>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

 <xsl:template match = "http-qos">
  <xsl:element name = "http-qos">
   <xsl:attribute name = "bandwidth-limit">
    <xsl:value-of select = "@bandwidth-limit"/>
   </xsl:attribute>
   <xsl:attribute name = "enforce-bandwidth-limit">
    <xsl:value-of select = "@enforce-bandwidth-limit"/>
   </xsl:attribute>
   <xsl:attribute name = "connection-limit">
    <xsl:value-of select = "@connection-limit"/>
   </xsl:attribute>
   <xsl:attribute name = "enforce-connection-limit">
    <xsl:value-of select = "@enforce-connection-limit"/>
   </xsl:attribute>
   <xsl:apply-templates/>
 </xsl:element>
</xsl:template>

<xsl:template match = "auth-db">
 <xsl:element name = "auth-db">
  <xsl:attribute name = "id">
   <xsl:value-of select = "@id"/>
  </xsl:attribute>
  <xsl:attribute name = "database">
   <xsl:value-of select = "@database"/>
  </xsl:attribute>
  <xsl:attribute name = "basedn">
   <xsl:value-of select = "@basedn"/>
  </xsl:attribute>
  <xsl:attribute name = "certmaps">
   <xsl:value-of select = "@certmaps"/>
  </xsl:attribute>
  <xsl:apply-templates/>
 </xsl:element>
</xsl:template>

<xsl:template match = "property">
 <xsl:element name = "property">
  <xsl:attribute name = "name">
   <xsl:value-of select = "@name"/>
  </xsl:attribute>
  <xsl:attribute name = "value">
   <xsl:value-of select = "@value"/>
  </xsl:attribute>
  <xsl:apply-templates select = "description"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "description">
 <xsl:element name = "description">
  <xsl:value-of select = "."/>
 </xsl:element>
</xsl:template>

<xsl:template match = "iiop-service">
 <xsl:element name = "iiop-service">
  <xsl:apply-templates select = "orb"/>
  <xsl:apply-templates select = "ssl-client-config"/>
  <xsl:apply-templates select = "iiop-listener"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "orb">
 <xsl:element name = "orb">
  <xsl:attribute name = "message-fragment-size">
   <xsl:value-of select = "@message-fragment-size"/>
   </xsl:attribute>
  <xsl:attribute name = "steady-thread-pool-size">
   <xsl:value-of select = "@steady-thread-pool-size"/>
  </xsl:attribute>
  <xsl:attribute name = "max-thread-pool-size">
   <xsl:value-of select = "@max-thread-pool-size"/>
  </xsl:attribute>
  <xsl:attribute name = "idle-thread-timeout-in-seconds">
   <xsl:value-of select = "@idle-thread-timeout-in-seconds"/>
  </xsl:attribute>
  <xsl:attribute name = "max-connections">
   <xsl:value-of select = "@max-connections"/>
  </xsl:attribute>
  <xsl:attribute name = "monitoring-enabled">
   <xsl:value-of select = "@monitoring-enabled"/>
  </xsl:attribute>
  <xsl:apply-templates select = "property"/>
</xsl:element>
</xsl:template>

<xsl:template match = "ssl-client-config">
 <xsl:element name = "ssl-client-config">
  <xsl:apply-templates select = "ssl"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "iiop-listener">
 <xsl:element name = "iiop-listener">
  <xsl:attribute name = "id">
   <xsl:value-of select = "@id"/>
  </xsl:attribute>
  <xsl:attribute name = "address">
   <xsl:value-of select = "@address"/>
  </xsl:attribute>
  <xsl:attribute name = "port">
   <xsl:value-of select = "@port"/>
  </xsl:attribute>
  <xsl:attribute name = "enabled">
   <xsl:value-of select = "@enabled"/>
  </xsl:attribute>
  <xsl:apply-templates select = "ssl"/>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "admin-service">
 <xsl:element name = "admin-service">
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "web-container">
 <xsl:element name = "web-container">
  <xsl:attribute name = "monitoring-enabled">
   <xsl:value-of select = "@monitoring-enabled"/>
  </xsl:attribute>
 <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "ejb-container">
 <xsl:element name = "ejb-container">
  <xsl:attribute name = "steady-pool-size">
   <xsl:value-of select = "@steady-pool-size"/>
  </xsl:attribute>
  <xsl:attribute name = "pool-resize-quantity">
   <xsl:value-of select = "@pool-resize-quantity"/>
  </xsl:attribute>
  <xsl:attribute name = "max-pool-size">
   <xsl:value-of select = "@max-pool-size"/>
  </xsl:attribute>
  <xsl:attribute name = "cache-resize-quantity">
   <xsl:value-of select = "@cache-resize-quantity"/>
  </xsl:attribute>
  <xsl:attribute name = "max-cache-size">
   <xsl:value-of select = "@max-cache-size"/>
  </xsl:attribute>
  <xsl:attribute name = "pool-idle-timeout-in-seconds">
   <xsl:value-of select = "@pool-idle-timeout-in-seconds"/>
  </xsl:attribute>
  <xsl:attribute name = "cache-idle-timeout-in-seconds">
   <xsl:value-of select = "@cache-idle-timeout-in-seconds"/>
  </xsl:attribute>
  <xsl:attribute name = "removal-timeout-in-seconds">
   <xsl:value-of select = "@removal-timeout-in-seconds"/>
  </xsl:attribute>
  <xsl:attribute name = "victim-selection-policy">
   <xsl:value-of select = "@victim-selection-policy"/>
  </xsl:attribute>
  <xsl:attribute name = "commit-option">
   <xsl:value-of select = "@commit-option"/>
  </xsl:attribute>
  <xsl:attribute name = "monitoring-enabled">
   <xsl:value-of select = "@monitoring-enabled"/>
  </xsl:attribute>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "mdb-container">
 <xsl:element name = "mdb-container">
  <xsl:attribute name = "steady-pool-size">
   <xsl:value-of select = "@steady-pool-size"/>
  </xsl:attribute>
  <xsl:attribute name = "pool-resize-quantity">
    <xsl:value-of select = "@pool-resize-quantity"/>
  </xsl:attribute>
  <xsl:attribute name = "max-pool-size">
   <xsl:value-of select = "@max-pool-size"/>
  </xsl:attribute>
  <xsl:attribute name = "idle-timeout-in-seconds">
   <xsl:value-of select = "@idle-timeout-in-seconds"/>
  </xsl:attribute>
  <xsl:attribute name = "monitoring-enabled">
   <xsl:value-of select = "@monitoring-enabled"/>
  </xsl:attribute>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "jms-service">
 <xsl:element name = "jms-service">
  <xsl:attribute name = "port">
   <xsl:value-of select = "jms-host/@port"/>
  </xsl:attribute>
  <xsl:attribute name = "admin-user-name">
   <xsl:value-of select = "jms-host/@admin-user-name"/>
  </xsl:attribute>
  <xsl:attribute name = "admin-password">
   <xsl:value-of select = "jms-host/@admin-password"/>
  </xsl:attribute>
  <xsl:attribute name = "init-timeout-in-seconds">
   <xsl:value-of select = "@init-timeout-in-seconds"/>
  </xsl:attribute>
  <xsl:attribute name = "start-args">
   <xsl:value-of select = "@start-args"/>
  </xsl:attribute>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "log-service">
  <xsl:comment> 
   The level attribute on log-service will be INFO (default) 
  </xsl:comment>
 <xsl:element name = "log-service">
  <xsl:attribute name = "file">
   <xsl:value-of select = "@file"/>
  </xsl:attribute>
  <xsl:attribute name = "log-stdout">
   <xsl:value-of select = "@log-stdout"/>
  </xsl:attribute>
  <xsl:attribute name = "log-stderr">
   <xsl:value-of select = "@log-stderr"/>
  </xsl:attribute>
  <xsl:attribute name = "echo-log-messages-to-stderr">
   <xsl:value-of select = "@echo-log-messages-to-stderr"/>
  </xsl:attribute>
  <xsl:attribute name = "create-console">
   <xsl:value-of select = "@create-console"/>
  </xsl:attribute>
  <xsl:attribute name = "log-virtual-server-id">
   <xsl:value-of select = "@log-virtual-server-id"/>
  </xsl:attribute>
  <xsl:attribute name = "use-system-logging">
   <xsl:value-of select = "@use-system-logging"/>
  </xsl:attribute>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "security-service">
 <xsl:element name = "security-service">
  <xsl:attribute name = "default-realm">
   <xsl:value-of select = "@default-realm"/>
  </xsl:attribute>
  <xsl:attribute name = "default-principal">
   <xsl:value-of select = "@default-principal"/>
  </xsl:attribute>
  <xsl:attribute name = "default-principal-password">
   <xsl:value-of select = "@default-principal-password"/>
  </xsl:attribute>
  <xsl:attribute name = "anonymous-role">
   <xsl:value-of select = "@anonymous-role"/>
  </xsl:attribute>
  <xsl:attribute name = "audit-enabled">
   <xsl:value-of select = "@audit-enabled"/>
  </xsl:attribute>
  <!-- attribute log-level is not required -->
  <!--
  <xsl:attribute name = "log-level">
   <xsl:value-of select = "@log-level"/>
  </xsl:attribute>
  -->
  <xsl:apply-templates select = "auth-realm"/>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "auth-realm">
 <xsl:element name = "auth-realm">
  <xsl:attribute name = "name">
   <xsl:value-of select = "@name"/>
  </xsl:attribute>
  <xsl:attribute name = "classname">
   <xsl:value-of select = "@classname"/>
  </xsl:attribute>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "transaction-service">
 <xsl:comment> 
  The level attribute on transaction-service will be INFO (default) 
 </xsl:comment>
 <xsl:element name = "transaction-service">
  <xsl:attribute name = "automatic-recovery">
   <xsl:value-of select = "@automatic-recovery"/>
  </xsl:attribute>
  <xsl:attribute name = "timeout-in-seconds">
   <xsl:value-of select = "@timeout-in-seconds"/>
  </xsl:attribute>
  <xsl:attribute name = "tx-log-dir">
   <xsl:value-of select = "@tx-log-dir"/>
  </xsl:attribute>
  <xsl:attribute name = "heuristic-decision">
    <xsl:value-of select = "@heuristic-decision"/>
  </xsl:attribute>
  <xsl:attribute name = "keypoint-interval">
   <xsl:value-of select = "@keypoint-interval"/>
  </xsl:attribute>
  <xsl:attribute name = "monitoring-enabled">
   <xsl:value-of select = "@monitoring-enabled"/>
  </xsl:attribute>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "java-config">
 <xsl:element name = "java-config">
  <xsl:attribute name = "java-home">
   <xsl:value-of select = "@java-home"/>
  </xsl:attribute>
  <xsl:attribute name = "debug-enabled">
   <xsl:value-of select = "@debug-enabled"/>
  </xsl:attribute>
  <xsl:attribute name = "debug-options">
   <xsl:value-of select = "@debug-options"/>
  </xsl:attribute>
  <xsl:attribute name = "rmic-options">
   <xsl:value-of select = "@rmic-options"/>
  </xsl:attribute>
  <xsl:attribute name = "javac-options">
   <xsl:value-of select = "@javac-options"/>
  </xsl:attribute>
  <xsl:attribute name = "classpath-prefix">
   <xsl:value-of select = "@classpath-prefix"/>
  </xsl:attribute>
  <xsl:attribute name = "server-classpath">
   <xsl:value-of select = "@server-classpath"/>
  </xsl:attribute>
  <xsl:attribute name = "classpath-suffix">
   <xsl:value-of select = "@classpath-suffix"/>
  </xsl:attribute>
  <xsl:attribute name = "native-library-path-prefix">
   <xsl:value-of select = "@native-library-path-prefix"/>
  </xsl:attribute>
  <xsl:attribute name = "native-library-path-suffix">
   <xsl:value-of select = "@native-library-path-suffix"/>
  </xsl:attribute>
  <xsl:attribute name = "bytecode-preprocessors">
   <xsl:value-of select = "@bytecode-preprocessors"/>
  </xsl:attribute>
  <xsl:attribute name = "env-classpath-ignored">
   <xsl:value-of select = "@env-classpath-ignored"/>
  </xsl:attribute>
  <xsl:apply-templates select = "profiler"/>
  <xsl:apply-templates select = "jvm-options"/>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "profiler">
 <xsl:element name = "profiler">
  <xsl:attribute name = "name">
   <xsl:value-of select = "@name"/>
  </xsl:attribute>
  <xsl:attribute name = "classpath">
   <xsl:value-of select = "@classpath"/>
  </xsl:attribute>
  <xsl:attribute name = "native-library-path">
   <xsl:value-of select = "@native-library-path"/>
  </xsl:attribute>
  <xsl:attribute name = "enabled">
   <xsl:value-of select = "@enabled"/>
  </xsl:attribute>
  <xsl:apply-templates select = "jvm-options"/>
  <xsl:apply-templates select = "property"/>
 </xsl:element>
</xsl:template>

<xsl:template match = "jvm-options">
 <xsl:element name = "jvm-options">
  <xsl:value-of select = "."/>
 </xsl:element>
</xsl:template>

 <xsl:template match="resources">
 </xsl:template>

	<xsl:template match = "/domain/resources" mode="ref">
			<xsl:apply-templates select = "custom-resource"/>
			<xsl:apply-templates select = "external-jndi-resource"/>
			<xsl:apply-templates select = "jdbc-resource"/>
			<xsl:apply-templates select = "mail-resource"/>
			<xsl:apply-templates select = "jms-resource"/>
			<xsl:apply-templates select = "persistence-manager-factory-resource"/>
			<xsl:apply-templates select = "jdbc-connection-pool"/>
	</xsl:template>
	<xsl:template match = "custom-resource">
		<xsl:element name = "custom-resource">
			<xsl:attribute name = "jndi-name">
				<xsl:value-of select = "@jndi-name"/>
			</xsl:attribute>
			<xsl:attribute name = "res-type">
				<xsl:value-of select = "@res-type"/>
			</xsl:attribute>
			<xsl:attribute name = "factory-class">
				<xsl:value-of select = "@factory-class"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
			<xsl:apply-templates select = "property"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "external-jndi-resource">
		<xsl:element name = "external-jndi-resource">
			<xsl:attribute name = "jndi-name">
				<xsl:value-of select = "@jndi-name"/>
			</xsl:attribute>
			<xsl:attribute name = "jndi-lookup-name">
				<xsl:value-of select = "@jndi-lookup-name"/>
			</xsl:attribute>
			<xsl:attribute name = "res-type">
				<xsl:value-of select = "@res-type"/>
			</xsl:attribute>
			<xsl:attribute name = "factory-class">
				<xsl:value-of select = "@factory-class"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
			<xsl:apply-templates select = "property"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "jdbc-resource">
		<xsl:element name = "jdbc-resource">
			<xsl:attribute name = "jndi-name">
				<xsl:value-of select = "@jndi-name"/>
			</xsl:attribute>
			<xsl:attribute name = "pool-name">
				<xsl:value-of select = "@pool-name"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
			<xsl:apply-templates select = "property"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "mail-resource">
		<xsl:element name = "mail-resource">
			<xsl:attribute name = "jndi-name">
				<xsl:value-of select = "@jndi-name"/>
			</xsl:attribute>
			<xsl:attribute name = "store-protocol">
				<xsl:value-of select = "@store-protocol"/>
			</xsl:attribute>
			<xsl:attribute name = "store-protocol-class">
				<xsl:value-of select = "@store-protocol-class"/>
			</xsl:attribute>
			<xsl:attribute name = "transport-protocol">
				<xsl:value-of select = "@transport-protocol"/>
			</xsl:attribute>
			<xsl:attribute name = "transport-protocol-class">
				<xsl:value-of select = "@transport-protocol-class"/>
			</xsl:attribute>
			<xsl:attribute name = "host">
				<xsl:value-of select = "@host"/>
			</xsl:attribute>
			<xsl:attribute name = "user">
				<xsl:value-of select = "@user"/>
			</xsl:attribute>
			<xsl:attribute name = "from">
				<xsl:value-of select = "@from"/>
			</xsl:attribute>
			<xsl:attribute name = "debug">
				<xsl:value-of select = "@debug"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
			<xsl:apply-templates select = "property"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "jms-resource">
		<xsl:element name = "jms-resource">
			<xsl:attribute name = "jndi-name">
				<xsl:value-of select = "@jndi-name"/>
			</xsl:attribute>
			<xsl:attribute name = "res-type">
				<xsl:value-of select = "@res-type"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
			<xsl:apply-templates select = "property"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "persistence-manager-factory-resource">
		<xsl:element name = "persistence-manager-factory-resource">
			<xsl:attribute name = "jndi-name">
				<xsl:value-of select = "@jndi-name"/>
			</xsl:attribute>
			<xsl:attribute name = "factory-class">
				<xsl:value-of select = "@factory-class"/>
			</xsl:attribute>
			<xsl:attribute name = "jdbc-resource-jndi-name">
				<xsl:value-of select = "@jdbc-resource-jndi-name"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
			<xsl:apply-templates select = "property"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "jdbc-connection-pool">
		<xsl:element name = "jdbc-connection-pool">
			<xsl:attribute name = "name">
				<xsl:value-of select = "@name"/>
			</xsl:attribute>
			<xsl:attribute name = "datasource-classname">
				<xsl:value-of select = "@datasource-classname"/>
			</xsl:attribute>
			<xsl:attribute name = "res-type">
				<xsl:value-of select = "@res-type"/>
			</xsl:attribute>
			<xsl:attribute name = "steady-pool-size">
				<xsl:value-of select = "@steady-pool-size"/>
			</xsl:attribute>
			<xsl:attribute name = "max-pool-size">
				<xsl:value-of select = "@max-pool-size"/>
			</xsl:attribute>
			<xsl:attribute name = "max-wait-time-in-millis">
				<xsl:value-of select = "@max-wait-time-in-millis"/>
			</xsl:attribute>
			<xsl:attribute name = "pool-resize-quantity">
				<xsl:value-of select = "@pool-resize-quantity"/>
			</xsl:attribute>
			<xsl:attribute name = "idle-timeout-in-seconds">
				<xsl:value-of select = "@idle-timeout-in-seconds"/>
			</xsl:attribute>
			<xsl:attribute name = "transaction-isolation-level">
				<xsl:value-of select = "@transaction-isolation-level"/>
			</xsl:attribute>
			<xsl:attribute name = "is-isolation-level-guaranteed">
				<xsl:value-of select = "@is-isolation-level-guaranteed"/>
			</xsl:attribute>
			<xsl:attribute name = "is-connection-validation-required">
				<xsl:value-of select = "@is-connection-validation-required"/>
			</xsl:attribute>
			<xsl:attribute name = "connection-validation-method">
				<xsl:value-of select = "@connection-validation-method"/>
			</xsl:attribute>
			<xsl:attribute name = "validation-table-name">
				<xsl:value-of select = "@validation-table-name"/>
			</xsl:attribute>
			<xsl:attribute name = "fail-all-connections">
				<xsl:value-of select = "@fail-all-connections"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
			<xsl:apply-templates select = "property"/>
		</xsl:element>
	</xsl:template>


        <xsl:template match="applications">
        </xsl:template>
	<xsl:template match = "/domain/applications" mode="ref">
			<xsl:apply-templates select = "lifecycle-module"/>
			<xsl:apply-templates select = "j2ee-application"/>
			<xsl:apply-templates select = "ejb-module"/>
			<xsl:apply-templates select = "web-module" mode="ref"/>
			<xsl:apply-templates select = "connector-module"/>
	</xsl:template>
	<xsl:template match = "lifecycle-module">
		<xsl:element name = "lifecycle-module">
			<xsl:attribute name = "name">
				<xsl:value-of select = "@name"/>
			</xsl:attribute>
			<xsl:attribute name = "class-name">
				<xsl:value-of select = "@class-name"/>
			</xsl:attribute>
			<xsl:attribute name = "classpath">
				<xsl:value-of select = "@classpath"/>
			</xsl:attribute>
			<xsl:attribute name = "load-order">
				<xsl:value-of select = "@load-order"/>
			</xsl:attribute>
			<xsl:attribute name = "is-failure-fatal">
				<xsl:value-of select = "@is-failure-fatal"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
			<xsl:apply-templates select = "property"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "j2ee-application">
		<xsl:element name = "j2ee-application">
			<xsl:attribute name = "name">
				<xsl:value-of select = "@name"/>
			</xsl:attribute>
			<xsl:attribute name = "location">
				<xsl:value-of select = "@location"/>
			</xsl:attribute>
			<xsl:attribute name = "virtual-servers">
				<xsl:value-of select = "@virtual-servers"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "ejb-module">
		<xsl:element name = "ejb-module">
			<xsl:attribute name = "name">
				<xsl:value-of select = "@name"/>
			</xsl:attribute>
			<xsl:attribute name = "location">
				<xsl:value-of select = "@location"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "web-module" mode="ref">
		<xsl:element name = "web-module">
			<xsl:attribute name = "name">
				<xsl:value-of select = "@name"/>
			</xsl:attribute>
			<xsl:attribute name = "context-root">
				<xsl:value-of select = "@context-root"/>
			</xsl:attribute>
			<xsl:attribute name = "location">
				<xsl:value-of select = "@location"/>
			</xsl:attribute>
			<xsl:attribute name = "virtual-servers">
				<xsl:value-of select = "@virtual-servers"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match = "connector-module">
		<xsl:element name = "connector-module">
			<xsl:attribute name = "name">
				<xsl:value-of select = "@name"/>
			</xsl:attribute>
			<xsl:attribute name = "location">
				<xsl:value-of select = "@location"/>
			</xsl:attribute>
			<xsl:apply-templates select = "description"/>
		</xsl:element>
	</xsl:template>

 <xsl:template match="application-refs">
  <xsl:element name="resources"/> <!-- empty element for now! -->
  <xsl:element name="applications">
   <xsl:apply-templates select="application-ref"/>
  </xsl:element>
 </xsl:template>
 
 <xsl:template match="resource-refs">
  <!-- do not match anything, please! -->
  <!--
  <xsl:element name="resources">
   <xsl:apply-templates select="resource-ref"/>
  </xsl:element>
  -->
 </xsl:template>

 <xsl:template match="resource-ref">
  <!-- do not match anything, please! -->
  <!--
  <xsl:apply-templates select="/domain/resources" mode="ref"/>
  -->
 </xsl:template>

 <xsl:template match="application-ref">
  <xsl:apply-templates select="/domain/applications" mode="ref"/>
 </xsl:template>
 <xsl:template match="servers/config-refs">
  <xsl:apply-templates select="config-ref"/>
 </xsl:template>
 <xsl:template match="config-ref">
 </xsl:template>
</xsl:transform>
