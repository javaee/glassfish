<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0">

    <xsl:output method="xml" indent="yes"
        doctype-public="-//Sun Microsystems Inc.//DTD Sun ONE Application Server 8.0//EN"
        doctype-system="http://www.sun.com/software/appserver/dtds/sun-domain_1_2.dtd"/> 

	<xsl:strip-space elements="*" />

    <!-- identity transformation -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- create application entries -->
    <xsl:template match="/domain/applications">
        <xsl:copy>
            <xsl:value-of select="."/>
            <xsl:apply-templates />
            <j2ee-application name="mdb-simple" 
                                enabled="true" 
                                location="${{com.sun.aas.instanceRoot}}/applications/j2ee-apps/mdb-simple" 
                                object-type="user" />
            <j2ee-application name="rmi-simple" 
                                enabled="true" 
                                location="${{com.sun.aas.instanceRoot}}/applications/j2ee-apps/rmi-simple" 
                                object-type="user" />
            <web-module name="webapps-simple" context-root="webapps-simple" 
                                enabled="true" 
                                location="${{com.sun.aas.instanceRoot}}/applications/j2ee-modules/webapps-simple"  
                                object-type="user" />
        </xsl:copy>
    </xsl:template>

    <!-- create resource entries -->
    <xsl:template match="/domain/resources">
        <xsl:copy><xsl:value-of select="."/>
            <xsl:apply-templates />
    <mail-resource debug="false" enabled="true" from="xyz@foo.com" 
                        host="localhost" 
                        jndi-name="mail/Session" 
                        store-protocol="imap" 
                        store-protocol-class="com.sun.mail.imap.IMAPStore" 
                        transport-protocol="smtp" 
                        transport-protocol-class="com.sun.mail.smtp.SMTPTransport" 
                        user="nobody"/>
    <admin-object-resource enabled="true" 
                        jndi-name="jms/MyQueue" 
                        res-adapter="jmsra" 
                        res-type="javax.jms.Queue">
      <property name="Name" value="MyQueue"/>
    </admin-object-resource>
    <connector-resource enabled="true" jndi-name="jms/MyMDBQcf" 
                        pool-name="mdb-simple-connector-pool" object-type="user"/>
    <connector-connection-pool name="mdb-simple-connector-pool"
                        resource-adapter-name="jmsra" 
                        connection-definition-name="javax.jms.QueueConnectionFactory" 
                        steady-pool-size="1"                         
                        max-pool-size="250" 
                        max-wait-time-in-millis="60000"                             
                        pool-resize-quantity="2" 
                        idle-timeout-in-seconds="300" 
                        fail-all-connections="false">
    </connector-connection-pool>                
    
    </xsl:copy>
    </xsl:template>

    <!-- copy event listener property -->
    <xsl:template match="/domain">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*"/>
            <property 
                value="com.sun.enterprise.admin.servermgmt.pe.PESamplesDomainXmlEventListener"
                name="DomainXmlEventListenerClass"/> 
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/domain/servers/server[@name='%%%SERVER_ID%%%']">
        <xsl:copy>
            <xsl:copy-of select="application-ref|@*"/>
      <application-ref virtual-servers="server" ref="mdb-simple" enabled="true" />
      <application-ref virtual-servers="server" ref="rmi-simple" enabled="true" />
      <application-ref virtual-servers="server" ref="webapps-simple" enabled="true" />
            <xsl:copy-of select="resource-ref|@*"/>
      <resource-ref ref="mdb-simple-connector-pool" enabled="true"/>	  
      <resource-ref ref="jms/MyMDBQcf" enabled="true"/>
      <resource-ref ref="mail/Session" enabled="true"/>
      <resource-ref ref="jms/MyQueue" enabled="true"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>


