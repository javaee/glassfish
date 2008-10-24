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
package com.sun.enterprise.config.serverbeans;

import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;

import java.beans.PropertyVetoException;

import org.glassfish.config.support.datatypes.Port;
import org.glassfish.config.support.datatypes.PositiveInteger;
import org.glassfish.config.support.datatypes.NonNegativeInteger;
import org.glassfish.api.amx.AMXConfigInfo;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;

import org.glassfish.quality.ToDo;


/* @XmlType(name = "", propOrder = {
    "ssl",
    "property"
}) */
@AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.HTTPListenerConfig")
@Configured
public interface HttpListener extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true, key=true)
    public String getId();

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) throws PropertyVetoException;

    /**
     * Gets the value of the address property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getAddress();

    /**
     * Sets the value of the address property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAddress(String value) throws PropertyVetoException;

    /**
     * Gets the value of the port property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true, dataType=Port.class)
    public String getPort();

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPort(String value) throws PropertyVetoException;

    /**
     * Gets the value of the externalPort property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(dataType=Port.class)
    public String getExternalPort();

    /**
     * Sets the value of the externalPort property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExternalPort(String value) throws PropertyVetoException;

    /**
     * Gets the value of the family property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="inet")
    public String getFamily();

    /**
     * Sets the value of the family property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFamily(String value) throws PropertyVetoException;

    /**
     * Gets the value of the blockingEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getBlockingEnabled();

    /**
     * Sets the value of the blockingEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBlockingEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the acceptorThreads property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="1", dataType=PositiveInteger.class)
    public String getAcceptorThreads();

    /**
     * Sets the value of the acceptorThreads property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAcceptorThreads(String value) throws PropertyVetoException;

    /**
     * Gets the value of the securityEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getSecurityEnabled();

    /**
     * Sets the value of the securityEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSecurityEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the defaultVirtualServer property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getDefaultVirtualServer();

    /**
     * Sets the value of the defaultVirtualServer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDefaultVirtualServer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the serverName property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getServerName();

    /**
     * Sets the value of the serverName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setServerName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the redirectPort property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(dataType=Port.class)
    public String getRedirectPort();

    /**
     * Sets the value of the redirectPort property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRedirectPort(String value) throws PropertyVetoException;

    /**
     * Gets the value of the xpoweredBy property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true", dataType=Boolean.class)
    public String getXpoweredBy();

    /**
     * Sets the value of the xpoweredBy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setXpoweredBy(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true", dataType=Boolean.class)
    public String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ssl property.
     *
     * @return possible object is
     *         {@link Ssl }
     */
    @Element
    public Ssl getSsl();

    /**
     * Sets the value of the ssl property.
     *
     * @param value allowed object is
     *              {@link Ssl }
     */
    public void setSsl(Ssl value) throws PropertyVetoException;
    
    
@PropertiesDesc(systemProperties=false,
    props={
    @PropertyDesc(name="recycle-objects", defaultValue="true", dataType=Boolean.class,
        description="Recycles internal objects instead of using the VM garbage collector"),
        
    @PropertyDesc(name="reader-threads", defaultValue="0", dataType=NonNegativeInteger.class,
        description="Number of reader threads, which read bytes from the non-blocking socket"),
        
    @PropertyDesc(name="acceptor-queue-length", defaultValue="4096", dataType=NonNegativeInteger.class,
        description="Length of the acceptor thread queue. Once full, connections are rejected"),
        
    @PropertyDesc(name="reader-queue-length", defaultValue="4096", dataType=NonNegativeInteger.class,
        description="Length of the reader thread queue. Once full, connections are rejected"),
        
    @PropertyDesc(name="use-nio-direct-bytebuffer", defaultValue="true", dataType=Boolean.class,
        description="Specifies that the NIO direct is used. In a limited resource environment, " +
                    "it might be faster to use non-direct Java's ByteBuffer by setting a value of false"),
        
    @PropertyDesc(name="authPassthroughEnabled", defaultValue="false", dataType=Boolean.class,
        description="Indicates that this http-listener element receives traffic from an  SSL-terminating proxy server. " +
                    "Overrides the authPassthroughEnabled  property of the parent http-service"),
        
    @PropertyDesc(name="proxyHandler", defaultValue="com.sun.enterprise.web.ProxyHandlerImpl",
        description="Specifies the fully qualified class name of a custom implementation of com.sun.appserv.ProxyHandler." +
                    "Used if the authPassthroughEnabled property of this http-listener and the parent http-service are both true. " +
                    "Overrides any value in the parent http-service element"),
        
    @PropertyDesc(name="proxiedProtocol", values={"ws/tcp", "http", "https", "tls"},
        description="Comma-separated list of protocols that can use the same port. " + 
        "For example, if you set this property to http,https and set the port to 4567, " +
        "you can access the port with either http://host:4567/ or https://host:4567/. " +
        " Specifying this property at the “http-service” on page 42 level overrides settings at the http-listener level. " +
        "If this property is not set at either level, this feature is disabled"),
        
    @PropertyDesc(name="bufferSize", defaultValue="4096", dataType=NonNegativeInteger.class,
        description="Size in bytes of the buffer to be provided for input streams created by HTTP listeners"),
        
    @PropertyDesc(name="connectionTimeout", defaultValue="30", dataType=NonNegativeInteger.class,
        description="Number of seconds HTTP listeners wait after accepting a connection for the request URI line to be presented"),
        
    @PropertyDesc(name="maxKeepAliveRequests", defaultValue="250", dataType=NonNegativeInteger.class,
        description="Maximum number of HTTP requests that can be pipelined until the connection is closed by the server. " +
            "Set this property to 1 to disable HTTP/1.0  keep-alive, as well as HTTP/1.1 keep-alive and pipelining"),
        
    @PropertyDesc(name="traceEnabled", defaultValue="true", dataType=Boolean.class,
        description="Enables the TRACE operation. Set this property to false to make the server less susceptible to cross-site scripting attacks"),
        
    @PropertyDesc(name="cometSupport", defaultValue="false", dataType=Boolean.class,
        description="Enables Comet support for this listener.  If your servlet/JSP page uses Comet technology, " +
            "make sure it is initialized by adding the load-on-startup element to web.xml"),
        
    @PropertyDesc(name="jkEnabled", defaultValue="false", dataType=Boolean.class,
        description="Enablesd/disables mod_jk support."),
        
    @PropertyDesc(name="compression", defaultValue="off", values={"off","on","force"},
        description="Specifies use of HTTP/1.1 GZIP compression to save server bandwidth. " +
            "A positive integer specifies the minimum amount of data required before the output is compressed. " +
            "If the content-length is not known, the output is compressed only if compression is set to 'on' or 'force'" ),
        
    @PropertyDesc(name="compressableMimeType", defaultValue="text/html, text/xml, text/plain",
        description="Comma-separated list of MIME types for which HTTP compression is used"),
        
    @PropertyDesc(name="noCompressionUserAgents", defaultValue="",
        description="Comma-separated list of regular expressions matching user-agents of HTTP clients for which compression should not be used"),
        
    @PropertyDesc(name="compressionMinSize", dataType=NonNegativeInteger.class,
        description="Minimum size of a file when compression is applied"),
        
    @PropertyDesc(name="minCompressionSize", dataType=NonNegativeInteger.class,
        description="Minimum size of a file when compression is applied"),
        
    @PropertyDesc(name="crlFile",
        description="Location of the Certificate Revocation List (CRL) file to consult during SSL client authentication. " +
            "Can be an absolute or relative file path. If relative, it is resolved against domain-dir. If unspecified, CRL checking is disabled"),
        
    @PropertyDesc(name="trustAlgorithm", values="PKIX",
        description="Name of the trust management algorithm (for example, PKIX) to use for certification path validation"),
        
    @PropertyDesc(name="trustMaxCertLength", defaultValue="5", dataType=Integer.class,
        description="Maximum number of non-self-issued intermediate certificates that can exist in a certification path. " +
            "Considered only if trustAlgorithm is set to PKIX. A value of zero implies that the path can only contain a single certificate. " +
            "A value of -1 implies that the path length is unconstrained (no maximum)"),
        
    @PropertyDesc(name="disableUploadTimeout", defaultValue="true", dataType=Boolean.class,
        description="When false, the connection for a servlet that reads bytes slowly is closed after the 'connectionUploadTimeout' is reached"),
        
    @PropertyDesc(name="connectionUploadTimeout", defaultValue="5", dataType=NonNegativeInteger.class,
        description="Specifies the timeout for uploads. Applicable only if 'disableUploadTimeout' is false"),

    /** uriEncoding UTF-8 Specifies the character set used to decode the request URIs received on this 
    HTTP listener. Must be a valid IANA character set naname. */
    @PropertyDesc(name="uriEncoding", defaultValue="UTF-8", values={"UTF-8"},
        description="Character set used to decode the request URIs received on this HTTP listener. " +
            "Must be a valid IANA character set name. Overrides the property of the parent http-service")
})
	@Element("property")
    List<Property> getProperty();
}
