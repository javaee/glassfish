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
 package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.node.connector.ConnectorNode;
import com.sun.enterprise.deployment.runtime.connector.SunConnector;
import com.sun.enterprise.deployment.util.ConnectorVisitor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.DescriptorVisitor;

import javax.enterprise.deploy.shared.ModuleType;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
/**
 * Deployment Information for connector
 *
 * <!ELEMENT connector (display-name?, description?, icon?, vendor-name,
 * spec-version, eis-type, version, license?, resourceadapter)>
 *
 * @author Tony Ng
 * @author Qingqing Ouyang
 */
public class ConnectorDescriptor extends BundleDescriptor {
    
    private String  displayName = "";
    private String  connectorDescription = "";
    private String  largeIcon = "";
    private String  smallIcon = "";
    private String  vendorName = "";
    private String  eisType = "";
    //private String  version = "";
    private String resourceAdapterVersion = "";
    private LicenseDescriptor licenseDescriptor = null;

    //connector1.0 old stuff, need clean up
    private Set configProperties;
    private Set authMechanisms;
    private Set securityPermissions;
    private String  managedConnectionFactoryImpl = "";
    private int     transactionSupport = PoolManagerConstants.LOCAL_TRANSACTION;
    private boolean reauthenticationSupport = false;
    private String connectionInterface;
    private String connectionClass;
    private String connectionFactoryInterface;
    private String connectionFactoryClass;
      
    //connector1.5 begin
    private String  resourceAdapterClass = "";
    private EnvironmentProperty configProperty = null;
    private OutboundResourceAdapter outboundRA = null;
    private InboundResourceAdapter  inboundRA = null;
    private Set adminObjects;

    //FIXME "inboundResourceAdapterClass" no longer valid
    //Use "resourceAdapterClass" instead
    private String inboundResourceAdapterClass = "";

    //FIXME remove messagelisteners
    private Set messageListeners;
    //connector1.5 end
    
    // following are all the get and set methods for the 
    // various variables listed above

    public ConnectorDescriptor() {
        this.configProperties = new OrderedSet();
	this.authMechanisms = new OrderedSet();
	this.securityPermissions = new OrderedSet();
	this.adminObjects = new OrderedSet();

        //FIXME.  need to remove the following
	this.messageListeners = new OrderedSet();
    }


    /**
     * @return the default version of the deployment descriptor
     * loaded by this descriptor
     */
    public String getDefaultSpecVersion() {
        return ConnectorNode.SPEC_VERSION;
    }

    ////////////////////////////////////////////////////////////////////
    // The following are access methods for connector1.0's 
    // resourceadapter element.
    // We no longer support them.
    ////////////////////////////////////////////////////////////////////

    //QQ. FIXME.  After verifier stops using this interface,
    //this REALLY needs to be removed. (for 1.0 cases only)

    // The methods for connection and connection factories have now shifted 
    //to OutboundResourceAdapter to maintain backward compatibility
    //Sheetal. These methods should be removed from here once we start 
    //using new DOL

    /**
     * @deprecated 
     */
    public String getConnectionFactoryInterface() {
        throw new UnsupportedOperationException();
    }
    
    /** 
     * @deprecated 
     */
    public void 
    setConnectionFactoryInterface(String connectionFactoryInterface) {
        throw new UnsupportedOperationException();
    }
    
    /** 
     * @deprecated 
     */
    public String 
    getConnectionFactoryImpl() {
        throw new UnsupportedOperationException();
    }
    
    /** 
     * @deprecated 
     */
    public void 
    setConnectionFactoryImpl(String connectionFactoryImpl) {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public String 
    getConnectionInterface() {
        throw new UnsupportedOperationException();
    }
    
    /** 
     * @deprecated 
     */
    public void 
    setConnectionInterface(String connectionInterface) {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public String 
    getConnectionImpl() {
        throw new UnsupportedOperationException();
    }
 
    /** 
     * @deprecated 
     */
    public void 
    setConnectionImpl(String connectionImpl) {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public String getManagedConnectionFactoryImpl() {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public void setManagedConnectionFactoryImpl(String managedConnectionFactoryImpl) {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public boolean supportsReauthentication() {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public String getReauthenticationSupport() {
        throw new UnsupportedOperationException();
    } 
 
    /** 
     * @deprecated 
     */
    public void setReauthenticationSupport(boolean reauthenticationSupport) {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public void setReauthenticationSupport(String reauthSupport) {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public String getTransSupport() {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public int getTransactionSupport() {
        throw new UnsupportedOperationException();
    }
    
    /** 
     * @deprecated 
     */
    public void setTransactionSupport(int transactionSupport) {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public void setTransactionSupport(String support) {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public Set 
    getAuthMechanisms() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @return a set of service-ref from this bundle or null
     * if none
     */
    public Set getServiceReferenceDescriptors() {
        return new OrderedSet();
    }    

    /** 
     *Set of SecurityPermission objects 
     */
    public Set 
    getSecurityPermissions() 
    {
        if (securityPermissions == null) {
            securityPermissions = new OrderedSet();
        }
        return securityPermissions;
    }

    /** 
     * @deprecated 
     */
    public boolean 
    addAuthMechanism(AuthMechanism mech) 
    {
        throw new UnsupportedOperationException();
    }
    
    /** 
     * @deprecated 
     */
    public boolean 
    removeAuthMechanism(AuthMechanism mech) 
    {
        throw new UnsupportedOperationException();
    }

    /** 
     * @deprecated 
     */
    public boolean 
    addAuthMechanism(int mech) 
    {
        throw new UnsupportedOperationException();
    }
    

    /** 
     * @deprecated 
     */
    public boolean 
    removeAuthMechanism(int mech) 
    {
        throw new UnsupportedOperationException();
    }

    /** 
     * Add a SecurityPermission object to the set
     */
    public void 
    addSecurityPermission(SecurityPermission permission) 
    {
	securityPermissions.add(permission);
    }

    /** 
     * Remove a SecurityPermission object to the set
     */  
    public void 
    removeSecurityPermission(SecurityPermission permission) 
    {
	securityPermissions.remove(permission);
    }

    ///////////////////////////////////////////////////////////////////////
    // The following are specific to connector1.5 elements
    // <!ELEMENT resourceadapter (resourceadapter-class, config-property?,
    // outbound-resourceadapter?, inbound-resourceadapter?, adminobject*)>
    ///////////////////////////////////////////////////////////////////////

    //connector1.5 begin

    public String getResourceAdapterClass() {
        return resourceAdapterClass;
    }
    
    public void setResourceAdapterClass (String raClass) {
        resourceAdapterClass = raClass;
    }
    
    /** Set of EnvironmentProperty 
     */
    public Set getConfigProperties() {
        return configProperties;
    }
         
    /** add a configProperty to the set
     */
    public void addConfigProperty(EnvironmentProperty configProperty) {
	this.configProperties.add(configProperty);
    }

    /** remove a configProperty from the set
     */ 
    public void removeConfigProperty(EnvironmentProperty configProperty) {
	configProperties.remove(configProperty);
    }

    public LicenseDescriptor getLicenseDescriptor() {
        return licenseDescriptor;
    }

    public void setLicenseDescriptor (LicenseDescriptor licenseDescriptor) {
        this.licenseDescriptor = licenseDescriptor;
    }

    public OutboundResourceAdapter getOutboundResourceAdapter() {
        return this.outboundRA;
    }
    
    public void 
    setOutboundResourceAdapter (OutboundResourceAdapter outboundRA) {
        this.outboundRA = outboundRA;
    }
    
    public InboundResourceAdapter getInboundResourceAdapter() {
        return this.inboundRA;
    }
    
    public void 
    setInboundResourceAdapter(InboundResourceAdapter inboundRA) {
        this.inboundRA = inboundRA;
    }

    /** 
     *@return admin objects
     */
    public Set getAdminObjects() {
        return adminObjects;
    }

    /** 
     * set admin object
     */
    public void addAdminObject(AdminObject admin) {
        adminObjects.add(admin);
    }

    public void removeAdminObject(AdminObject admin) {
        adminObjects.remove(admin);
    }

    public boolean hasAdminObjects() {
        return adminObjects.size() > 0;
    }

    public boolean getOutBoundDefined() {
	return outboundRA != null;
    }

    //for the purpose of writing this optional node back to XML format.
    //if this node is present then write it else donot.

    public boolean getInBoundDefined() {
        return inboundRA != null;
    }


    //connector1.5 end


    /////////////////////////////////////////////////////////////
    // The following are the accessor methods for elements that
    // are common to both connector1.0 and connector1.5
    /////////////////////////////////////////////////////////////
    

    /** get the connector description
    */
    public String getConnectorDescription() {
        return connectorDescription;
    }
 
    /** set the connector description
    */
    public void setConnectorDescription(String description) {
        connectorDescription = description;
    }


    /** get value for vendorName
    */
    public String getVendorName() {
        return vendorName;
    }
 
    /** set value for vendorName
    */
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }


    /** get eisType
    */
    public String getEisType() {
        return eisType;
    }
 
   
    /** set eisType
    */
    public void setEisType(String eisType) {
        this.eisType = eisType;
    } 

    /** get value for version
    */
    public String getVersion() {
        //return version;
        throw new UnsupportedOperationException();
    }

    /** set value for version
    */ 
    public void setVersion(String version) {
        throw new UnsupportedOperationException();
    }

    /** get value for resourceadapter version (1.5 schema
    */
    public String getResourceAdapterVersion() {
        return resourceAdapterVersion;
    }

    /** set value for resourceadater version (1.5 schema)
    */ 
    public void setResourceAdapterVersion(String resourceAdapterVersion) {
        this.resourceAdapterVersion = resourceAdapterVersion;
    }

    ////////////////////////////////////////////////////////
    // Other MISC methods
    ////////////////////////////////////////////////////////

    /** return name used for deployment
    */
    public String getDeployName() {
	return getModuleDescriptor().getArchiveUri();
    }
            
    
    /** 
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     * 
     * @param a visitor to traverse the descriptors
     */    
    public void visit(DescriptorVisitor aVisitor) {
        if (aVisitor instanceof ConnectorVisitor) {
            visit((ConnectorVisitor) aVisitor);
        } else {
            super.visit(aVisitor);
        }
    }    

    /** 
     * visit the descriptor and all sub descriptors with a 
     * DOL visitor implementation
     * @param a visitor to traverse the descriptors
     */
    public void visit(ConnectorVisitor aVisitor) {
        aVisitor.accept(this);
    }   

    /*
     * Prints entry information.
     */
    private static void printEntry(ZipEntry e)	throws IOException {
        DOLUtils.getDefaultLogger().log(Level.FINE, e.getName());
    }
    
    /*
     * @param type The full qualified name for connection factory interface
     */
    public ConnectionDefDescriptor 
    getConnectionDefinitionByCFType (String type)
    {
        return getConnectionDefinitionByCFType(type, true);
    }

    /*
     * @param type The full qualified name for connection factory interface
     * @param useDefault This param is to support the backward compatibility
     *                   of connector 1.0 resource adapter where there is
     *                   only one connection factory type.  If type is null
     *                   and useDefault is true, the only CF will be returned.
     */
    public ConnectionDefDescriptor 
    getConnectionDefinitionByCFType (String type, boolean useDefault)
    {
        Iterator it = this.outboundRA.getConnectionDefs().iterator();
        while (it.hasNext())
        {
            ConnectionDefDescriptor desc = (ConnectionDefDescriptor) it.next();
            
            if (type == null)
            {
                if (useDefault 
                        && this.outboundRA.getConnectionDefs().size() == 1)
                    return desc;
                else
                    return null;
            }
                    
            if (desc.getConnectionFactoryIntf().equals(type))
                return desc;
        }
        return null;
    }

    public int getNumOfSupportedCFs ()
    {
        return outboundRA.getConnectionDefs().size();
    }

    public AdminObject getAdminObjectByType(String type) 
    {
        Iterator i = getAdminObjects().iterator();
        while (i.hasNext())
        {
            AdminObject ao = (AdminObject) i.next();
            if (type.equals(ao.getAdminObjectInterface()))
                return ao;
        }
        
        return null;
    }

        
    /**
     * A formatted string representing my state.
     */
    public void print(StringBuffer toStringBuffer)
    {
        StringBuffer buf = toStringBuffer;
        super.print(buf);
        
	buf.append("\n displayName : " + super.getName());
	buf.append("\n connector_description : " + connectorDescription);
	buf.append("\n smallIcon : " + super.getSmallIconUri());
	buf.append("\n largeIcon : " + super.getLargeIconUri());
	buf.append("\n vendorName : " + vendorName);
	buf.append("\n eisType : " + eisType);
	//buf.append("\n version : " + version);
	buf.append("\n resourceadapter version : " + resourceAdapterVersion);

        //license info
        if (getLicenseDescriptor() != null) {
 	    buf.append("\n license_description : " + getLicenseDescriptor().getDescription());
       	    buf.append("\n licenseRequired : " + getLicenseDescriptor().getLicenseRequiredValue());
        }

	buf.append("\n resourceAdapterClass : " + resourceAdapterClass);

	buf.append("\n resourceAdapterClass [" + resourceAdapterClass 
                + "] config properties :");
        appendConfigProperties(this.configProperties, buf);
            
        if (this.outboundRA == null)
            buf.append("\n Outbound Resource Adapter NOT available");
        else
        {
            buf.append("\n Outbound Resource Adapter Info : ");

            buf.append("\n connection-definitions: ");
            for (Iterator i = this.outboundRA.getConnectionDefs().iterator(); 
                 i.hasNext();)
            {
                buf.append("\n------------\n");

                ConnectionDefDescriptor conDef = 
                    (ConnectionDefDescriptor) i.next();
                buf.append("MCF : " 
                        + conDef.getManagedConnectionFactoryImpl() + ", ");
                buf.append("\n MCF [" + 
                        conDef.getManagedConnectionFactoryImpl() 
                        + "] config properties :");
                appendConfigProperties(conDef.getConfigProperties(), buf);
                
                buf.append("[CF Interface : " 
                        + conDef.getConnectionFactoryIntf() + "], ");
                buf.append("[CF Class : " 
                        + conDef.getConnectionFactoryImpl() + "], ");
                buf.append("[Connection Interface : " 
                        + conDef.getConnectionIntf() + "], ");
                buf.append("[Connection Class : " 
                        + conDef.getConnectionImpl() + "] ");
                
                buf.append("\n------------\n");
            }

            buf.append("\n transaction-support : " 
                    + this.outboundRA.getTransSupport());
            
            buf.append("\n authentication-mechanism: ");
            for (Iterator i = this.outboundRA.getAuthMechanisms().iterator(); 
                 i.hasNext();) 
            {
                AuthMechanism conf = (AuthMechanism) i.next(); 
                buf.append("\n------------\n");
                buf.append("[Type : " + conf.getAuthMechType() + "], ");
                buf.append("[Interface : " 
                        + conf.getCredentialInterface() + "]");
                buf.append("\n------------" );
            }

            buf.append("\n reauthenticate-support : " 
                    + this.outboundRA.getReauthenticationSupport());

            buf.append("\n security-permission : "); 
            for (Iterator i = 
                     getSecurityPermissions().iterator(); 
                 i.hasNext();) 
            {
                SecurityPermission conf = (SecurityPermission) i.next(); 
                buf.append("\n------------\n");
                buf.append("[persmission : " + conf.getPermission() + "], ");
                buf.append("[discription : " + conf.getDescription() + "]");
                buf.append("\n------------" );
            }

        } //outbound resourceadapter

        if (this.inboundRA == null)
            buf.append("\n Inbound Resource Adapter NOT available");
        else
        {
            buf.append("\n Inbound Resource Adapter Info : ");
            
            buf.append("\n Message Listeners Info : ");
            for (Iterator i = this.inboundRA.getMessageListeners().iterator();
                 i.hasNext();)
            {
                buf.append("\n------------\n");
                MessageListener l = (MessageListener) i.next();
                buf.append("[Type : " + l.getMessageListenerType() + "], ");
                buf.append("[AS Class : " + l.getActivationSpecClass() + "]");
                buf.append("\n------------ ");
            }
            
        } //inbound resourceadapter
        
        
        if (this.adminObjects.size() == 0)
            buf.append("\n Admin Objects NOT available");
        else
        {
            buf.append("\n Admin Objects Info : ");
            for (Iterator i = this.adminObjects.iterator(); i.hasNext();)
            {
                buf.append("\n------------\n");
                AdminObject a = (AdminObject) i.next();
                buf.append("[Type : " + a.getAdminObjectInterface() + "], ");
                buf.append("[Class : " + a.getAdminObjectClass() + "]");
                appendConfigProperties(a.getConfigProperties(), buf);
                buf.append("\n------------ ");
            }
            
        } //admin objects
        
    }

    private StringBuffer appendConfigProperties (Set props, StringBuffer buf)
    {
        buf.append("\n------------");
        for (Iterator i = props.iterator(); i.hasNext();)
        {
            EnvironmentProperty config = (EnvironmentProperty) i.next();
            buf.append("[Name : " + config.getName() + "], ");
            buf.append("[Value: " + config.getValue() + "], ");
            buf.append("[Type : " + config.getType() + "]");
        }
        buf.append("\n------------");
        return buf;
    }
    
    /**
     * @return the module type for this bundle descriptor
     */
    public ModuleType getModuleType() {
        return ModuleType.RAR;
    }
    
    /**
     *@param type message listener type
     */
    public MessageListener getSupportedMessageListener(String type) {
        if (this.inboundRA == null) {
            return null; 
        }
        
        Iterator i = this.inboundRA.getMessageListeners().iterator();
        while (i.hasNext()) {
            MessageListener l = (MessageListener) i.next();
            if ((l.getMessageListenerType()).equals(type)) {
                return l;
            }
        }
        return null;
    }

    /**
     * @deprecated
     */
    public boolean isMessageListenerSupported(String type) {
        throw new UnsupportedOperationException();
    }
    
    /***********************************************************************************************
     * START
     * Deployment Consolidation to Suppport Multiple Deployment API Clients
     * Member Variable: sunConnector
     * Methods: setSunDescriptor, getSunDescriptor
     ***********************************************************************************************/
    
    private SunConnector sunConnector = null; 
    
    /** 
     * This returns the extra ejb sun specific info not in the RI DID.
     *
     * @return object representation of connector deployment descriptor 
     */
    public SunConnector getSunDescriptor(){
        return sunConnector;
    }
    
    /** 
     * This sets the extra connector sun specific info not in the RI DID.
     * 
     * @param connector SunConnector object representation of connector deployment descriptor
     */
    public void setSunDescriptor(SunConnector connector){
        this.sunConnector = connector;
    }

    /*******************************************************************************************
     * END
     * Deployment Consolidation to Suppport Multiple Deployment API Clients
     *******************************************************************************************/     
}

