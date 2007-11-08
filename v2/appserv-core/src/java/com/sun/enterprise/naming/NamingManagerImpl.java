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

package com.sun.enterprise.naming;

import java.util.*;
import java.net.*;
import java.io.*;
import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;
import java.security.PrivilegedActionException;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import javax.enterprise.deploy.shared.ModuleType;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Service;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import org.omg.CORBA.ORB;

import com.sun.enterprise.*;
import com.sun.enterprise.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.webservice.WsUtil;
import com.sun.enterprise.webservice.ServiceInvocationHandler;
import com.sun.enterprise.webservice.JAXWSServiceDelegate;
import com.sun.enterprise.webservice.WSContainerResolver;
import com.sun.enterprise.distributedtx.UserTransactionImpl;
import com.sun.enterprise.naming.java.*;
import com.sun.enterprise.log.Log;


import com.sun.ejb.Container;
import com.sun.ejb.EJBUtils;

import java.util.logging.*;
import com.sun.logging.*;

/**
 * This is the manager that handles all naming operations including
 * publishObject as well as binding environment props, resource and
 * ejb references in the namespace.
 */
public final class NamingManagerImpl implements NamingManager 
{

    static Logger _logger=LogDomains.getLogger(LogDomains.JNDI_LOGGER);

    public static final String IIOPOBJECT_FACTORY =
	    "com.sun.enterprise.naming.factory.IIOPObjectFactory";

    public static final String JAVA_COMP_STRING = "java:comp/env/";

    private static final String CONTEXT_SEPARATOR = "/";
    private static final String ID_SEPARATOR = "_";

    // J2EE Component type
    private static final int UNKNOWN_COMPONENT = 0;
    private static final int EJB_COMPONENT = 1;
    private static final int WEB_COMPONENT = 2;
    private static final int APP_CLIENT_COMPONENT = 3;

    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(NamingManagerImpl.class);
  
    private InitialContext initialContext;
    private InitialContext cosContext;   

    private InvocationManager im = null;
    private Switch theSwitch     = null;
    private NameParser nameParser = new SerialNameParser();

    // This is synchronized.
    private Hashtable namespaces;

    public static final String EIS_STRING = "/eis/";

    private static final String CORBANAME = "corbaname:";

    private static final String IIOPURL = "iiop://";

 
    public NamingManagerImpl() throws NamingException
    {
        this( new InitialContext() );
    }

    /**
     * Create the naming manager. Creates a new initial context.
     */
    public NamingManagerImpl(InitialContext ic) throws NamingException
    {
        initialContext = ic;
	namespaces = new Hashtable();

        theSwitch = Switch.getSwitch();
        im = theSwitch.getInvocationManager();
    }

    /** 
     * Get the initial naming context.
     */
    public Context getInitialContext()
    {
        return initialContext; 
    }


    public NameParser getNameParser()
    {
	return nameParser;
    }

    /**
     * Get cosContext which is the root of the COSNaming namespace.
     * Setting java.naming.corba.orb is necessary to prevent the
     * COSNaming context from creating its own ORB instance.
     */

    private InitialContext getCosContext() throws NamingException {
	if (cosContext == null) {
	    Hashtable cosNamingEnv = new Hashtable ();
	    cosNamingEnv.put("java.naming.factory.initial",
			     "com.sun.jndi.cosnaming.CNCtxFactory");
	    ORB orb = ORBManager.getORB();
	    cosNamingEnv.put("java.naming.corba.orb", orb);
	    cosContext = new InitialContext(cosNamingEnv);
	}
	return cosContext;

    }
    /**
     * Publish a name in the naming service.
     * @param the Name that the object is bound as.
     * @param the Object that needs to be bound.
     * @param rebind flag
     * @exception NamingException if there is a naming exception.
     */
    public void publishObject(String name, Object obj, boolean rebind) 
	    throws NamingException
    {
        Name nameobj = new CompositeName(name);
        publishObject(nameobj, obj, rebind);
    }

    /**
     * Publish a name in the naming service.  
     * @param the Name that the object is bound as.
     * @param the Object that needs to be bound.
     * @param rebind flag
     * @exception NamingException if there is a naming exception.
     */    
    public void publishObject(Name name, Object obj, boolean rebind) 
	throws NamingException
    {
	
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"Publish object " + obj + " using name " + name);
	}
	
        Object serialObj = obj;
	
        if ( isCOSNamingObj( obj ) ) {
	    
            // Create any COS naming sub-contexts in name 
            // that don't already exist.
            createSubContexts( name, getCosContext() );
	    
            if( rebind ) {
                getCosContext().rebind(name, obj);
            }
            else {
                getCosContext().bind(name, obj);
            }

            // Bind a reference to it in the SerialContext using
            // the same name. This is needed to allow standalone clients
	    // to lookup the object using the same JNDI name.
	    // It is also used from bindObjects while populating ejb-refs in 
	    // the java:comp namespace.
            serialObj = new Reference("reference",
                                      new StringRefAddr("url", name.toString()),
                                      IIOPOBJECT_FACTORY, null);
        } // End if -- CORBA object

        if( rebind ) {
            initialContext.rebind(name, serialObj);
        }
        else {
            initialContext.bind(name, serialObj);
        }
    }        

    /**
     * Remove an object from the naming service.
     * @param the Name that the object is bound as.
     * @exception Exception
     */
    public void unpublishObject(String name) throws NamingException
    {

        Object obj = initialContext.lookup(name);

        if ( isCOSNamingObj( obj ) ) {
            getCosContext().unbind(name);
        }

        initialContext.unbind(name);

        // XXX Clean up sub-contexts???
    }


    /**
     * Remove an object from the naming service.
     * @param the Name that the object is bound as.
     * @exception Exception
     */
    public void unpublishObject(Name name) throws NamingException
    {
        this.unpublishObject(name.toString());    
    }

    /**
     * Create any sub-contexts in name that don't already exist.
     * @param the Name containing sub-contexts to create
     * @param context in which sub-contexts should be created
     * @exception Exception
     */
    private void createSubContexts(Name name, Context rootCtx) throws NamingException {

        int numSubContexts = name.size() - 1;
        Context currentCtx = rootCtx;
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"Creating sub contexts for " + name);
        }

        for(int subCtxIndex = 0; subCtxIndex < numSubContexts; subCtxIndex++) {
            String subCtxName = name.get(subCtxIndex);
            try {

                Object obj = currentCtx.lookup(subCtxName);

                if( obj == null ) {
                    // @@@ thought it should throw NameNotFound when
                    // context doesn't exist...
                    if( _logger.isLoggable(Level.FINE)) {	
                        _logger.log(Level.FINE,"name == null");	
                    }
                    // Doesn't exist so create it.
                    Context newCtx = currentCtx.createSubcontext(subCtxName);
                    currentCtx     = newCtx;
                }
                else if( obj instanceof Context ) {
                    // OK -- no need to create it.
                    currentCtx = (Context) obj;
                }
                else {
                    // Context name clashes with existing object.
                    throw new NameAlreadyBoundException(subCtxName);
                }
            }
            catch(NameNotFoundException e) {	
                _logger.log(Level.FINE,"name not found", e);

                // Doesn't exist so create it.
                Context newCtx = currentCtx.createSubcontext(subCtxName);
                currentCtx     = newCtx;
            }
        } // End for -- each sub-context

        return;
    }

    /**
     * This method enumerates the env properties, ejb and resource
     * references etc for a J2EE component and binds them in the component's
     * java:comp namespace.
     * This method is synchronized to avoid any possibility of concurrent
     * deployment-time calls.
     */
    public synchronized String bindObjects(JndiNameEnvironment env) 
	throws NamingException
    {
        String componentId = getMangledIdName(env);
      
	// Note: HashMap is not synchronized. The namespace is populated
	// at deployment time by a single thread, and then on there are
	// no structural modifications (i.e. no keys added/removed). 
	// So the namespace doesnt need to be synchronized.
	HashMap namespace = new HashMap();
	namespaces.put(componentId, namespace);

	// put entries for java:, java:comp and java:comp/env
	namespace.put("java:", new javaURLContext("java:", null)); 
	namespace.put("java:comp", new javaURLContext("java:comp", null));
	namespace.put("java:comp/env", new javaURLContext("java:comp/env", null));

	for (Iterator itr = env.getEnvironmentProperties().iterator(); 
             itr.hasNext();) {
            

	    EnvironmentProperty next = (EnvironmentProperty) itr.next();
            String logicalJndiName   = descriptorToLogicalJndiName(next);

            // Skip any env-entries that have not been assigned a value.
            if( !next.hasAValue() ) {
                continue;
            }

	    Object valueObject       = next.getValueObject();


	    if (_logger.isLoggable(Level.FINE)) {
	        _logger.log(Level.FINE,localStrings.getLocalString("naming.bind",
                      "Binding name:{0}" , new Object[] {logicalJndiName}));
            }

            if (namespace.put(logicalJndiName, valueObject) != null) {
	        _logger.log(Level.WARNING,
                    localStrings.getLocalString("naming.alreadyexists",
                     "Reference name [{0}] already exists in {1}", 
                      new Object[] {next.getName(), getApplicationName(env)}));
            }

	    bindIntermediateContexts(namespace, logicalJndiName);
	}

        //JmsDestinationReferenceDescriptor actually represents all Admin objects.
        for (Iterator itr = env.getJmsDestinationReferenceDescriptors().iterator(); 
             itr.hasNext();) {
	    JmsDestinationReferenceDescriptor next = 
                (JmsDestinationReferenceDescriptor) itr.next();
            String logicalJndiName   = descriptorToLogicalJndiName(next);

            String destinationName   = next.getJndiName();
            Object destinationObject = null;
            if (next.isEJBContext()) {
                // Need to delay ejb context lookup until runtime
                destinationObject = 
                    new J2EEEnvWrapper(next, J2EEEnvWrapper.EJB_CONTEXT);
            } else {
                try {
                    destinationObject = initialContext.lookup(destinationName);
                }
                catch(NamingException ne) {

                    _logger.log(Level.SEVERE, 
                     "enterprise_naming.notfound_jmsdestination", destinationName);
                    _logger.log(Level.SEVERE, ne.getClass().getName(), ne);
        	    // For embedded resource adapters, admin object can be 
                    // created after deployment. Doesnt throw exception, 
                    // because deployment/should succeed.
		    destinationObject = null;
                }

                if (destinationObject == null) {
	            destinationObject = new J2EEEnvWrapper (destinationName, 
		                    J2EEEnvWrapper.MUTABLE_RESOURCE_REF);
	        }
            }

            if( destinationObject instanceof javax.jms.Queue ) {
                if( !next.getRefType().equals("javax.jms.Queue") && 
                    !next.getRefType().equals("javax.jms.Destination")) {
                    throw new InvalidNameException(localStrings.getLocalString("naming.destinationRefTypeMismatch", "", new Object[] {next.getName(), next.getRefType()}));
                }
            } else if( destinationObject instanceof javax.jms.Topic ) {
                if( !next.getRefType().equals("javax.jms.Topic") && 
                    !next.getRefType().equals("javax.jms.Destination")) {
                    throw new InvalidNameException(localStrings.getLocalString("naming.destinationRefTypeMismatch", "", new Object[] {next.getName(), next.getRefType()}));
                }
            }
            
	    if (_logger.isLoggable(Level.FINE)) {
		      _logger.log(Level.FINE,localStrings.getLocalString("naming.bind",
                      "Binding name:{0}" , new Object[] {logicalJndiName}));
            }

            if ( namespace.put(logicalJndiName, destinationObject) != null) {
	        _logger.log(Level.WARNING,
                     localStrings.getLocalString("naming.alreadyexists",
                     "Reference name [{0}] already exists in {1}", 
                      new Object[] {next.getName(), getApplicationName(env)}));
            }

	    bindIntermediateContexts(namespace, logicalJndiName);
	}

	for (Iterator itr = env.getEjbReferenceDescriptors().iterator(); 
             itr.hasNext();) {
	    EjbReferenceDescriptor next = (EjbReferenceDescriptor) itr.next();  	    
	    String logicalJndiName      = descriptorToLogicalJndiName(next);
            
	    if (_logger.isLoggable(Level.FINE)) {
		      _logger.log(Level.FINE,localStrings.getLocalString
                               ("naming.bind", "Binding name:{0}",
                                new Object[] {logicalJndiName}));
            }

	    Object home = null;
	    if ( next.isLocal() ) { // an ejb-local-ref
		// Create EJB_LOCAL_REF wrapper.  Actual ejb local ref
                // lookup resolution is done lazily.
		home = new J2EEEnvWrapper(next,J2EEEnvWrapper.EJBLOCAL_REF);

	    }
	    else { // an ejb-ref
		home = new J2EEEnvWrapper(next, J2EEEnvWrapper.EJB_REF);

		// We dont do a COSNaming lookup here because COSNaming lookup
		// internally calls "is_a" on the EJB which causes problems
		// if the EJB requires authentication.
	    }

            if (namespace.put(logicalJndiName, home) != null) {
	        _logger.log(Level.WARNING,
                    localStrings.getLocalString("naming.alreadyexists",
                     "Reference name [{0}] already exists in {1}", 
                      new Object[] {next.getName(), getApplicationName(env)}));
            }

	    bindIntermediateContexts(namespace, logicalJndiName);
	}
        
        for (Iterator itr = env.getMessageDestinationReferenceDescriptors().
                 iterator(); itr.hasNext();) {
            MessageDestinationReferenceDescriptor next =
                (MessageDestinationReferenceDescriptor) itr.next();
            String logicalJndiName = descriptorToLogicalJndiName(next);

            String destinationName = null;
            // when this message ref is linked to a logical destination
            if( next.isLinkedToMessageDestination() ) {
                destinationName = 
                    next.getMessageDestination().getJndiName();
            // when this message ref is to a physical destination
            } else {
                destinationName = next.getJndiName();
            } 
            
            // if this message reference has been resolved
            if (destinationName != null) {
		_logger.fine("NamingManagerImpl : destinationName = "+ destinationName );
                try {
                    Object adminObject = 
                        initialContext.lookup(destinationName);
		    _logger.fine("NamingManagerImpl : binding " + logicalJndiName + " to " +adminObject);

                    _logger.log(Level.INFO, localStrings.getLocalString(
                        "naming.bind", "Binding name:`{0}`",
                        new Object[] {logicalJndiName}));
                    namespace.put(logicalJndiName, adminObject);
                    bindIntermediateContexts(namespace, logicalJndiName);
                } catch(Exception e) {
                    String msg = localStrings.getLocalString
                        ("naming.invalidDestination",
                         "Invalid Destination:`{0} for {1}`",
                         new Object[] {destinationName, logicalJndiName});
                    _logger.log(Level.SEVERE, "naming.invalidDestination", 
				new Object[] {destinationName, logicalJndiName});
                    NamingException ne = new NamingException();
                    ne.initCause(e);
                    throw ne;
                }
            } else {
                String msg = 
                    localStrings.getLocalString("naming.unresolvedmsgdestref",
                        "Message Destination Reference {0} has not been" +
                        " resolved",
                        new Object[] { logicalJndiName });
                _logger.log(Level.SEVERE, "naming.unresolvedmsgdestref", 
			    new Object[] {logicalJndiName});
                throw new NamingException(msg);
           }
        }
        
        for (Iterator itr = env.getResourceReferenceDescriptors().iterator();
        itr.hasNext();) {
            ResourceReferenceDescriptor next =
            (ResourceReferenceDescriptor) itr.next();
            
            next.checkType();
            
            String logicalResRef    = next.getName(); // "MyAccountBean"
            String logicalJndiName  = descriptorToLogicalJndiName(next);
            
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,localStrings.
                getLocalString("naming.bind", "Binding name:{0}",
                new Object[] {logicalJndiName}));
            }
            
            String resJndi = next.getJndiName();

            if(resJndi == null || resJndi.equals("")) {
                throw new InvalidNameException
                    ("Real JNDI name cannot be " +
                    "empty for " + logicalResRef);
            }
            
            Object obj = null;
            
            if (next.isMailResource()) {
                obj = new J2EEEnvWrapper(resJndi, J2EEEnvWrapper.MAIL_REF);
            }
            else if (next.isURLResource()) {
                String url = next.getJndiName();
                try {
                    obj = new java.net.URL(url);
                }
                catch(MalformedURLException e) {
                    throw new InvalidNameException
                    (localStrings.getLocalString("naming.malformedURL",
                    "Malformed URL:'{0}'",
                    new Object[] {url}));
                }
                obj = new J2EEEnvWrapper(obj, J2EEEnvWrapper.MUTABLE);
            }
            
            //IMPORTANT!!! this needs to be before the test for JDBC as
            //a connector could have a type of javax.sql.DataSource
            //else if (next.isResourceConnectionFactory()) {
            else if (isConnector(logicalJndiName)) {
                obj = new J2EEEnvWrapper(resJndi,
                J2EEEnvWrapper.RESOURCE_ADAPTER_REF);
            }
            
            // adding check for jdbc resource
            // IMPORTANT!!! this needs to be after the test for connectors as
            // a connector could have a type of javax.sql.DataSource
            else if (next.isJDBCResource()) {

                try {
                    Object res = initialContext.lookup(resJndi);
                    obj = new J2EEEnvWrapper(res, J2EEEnvWrapper.JDBC_REF);
                } catch( NamingException ne ) {
                    String msg = localStrings.getLocalString
                    ("naming.unresolved.warning", "",
                    new Object[] {logicalJndiName, resJndi});
                    _logger.log(Level.FINE,msg);
                }
                if ( obj == null ) { // perhaps the resource is not yet deployed
                    obj = new J2EEEnvWrapper(resJndi,
                    J2EEEnvWrapper.MUTABLE_RESOURCE_REF);
                }
            } 
	    else if (next.isJMSConnectionFactory()) {
		try {
		    Object res = initialContext.lookup(resJndi);
		    obj = new J2EEEnvWrapper(res, J2EEEnvWrapper.RESOURCE_ADAPTER);
		} catch (NamingException ne) {
                    String msg = localStrings.getLocalString
                    ("naming.unresolved.warning", "",
                    new Object[] {logicalJndiName, resJndi});
                    _logger.log(Level.FINE,msg);
		}
                if ( obj == null ) { // perhaps the resource is not yet deployed
                    obj = new J2EEEnvWrapper(resJndi,
                    J2EEEnvWrapper.MUTABLE_RESOURCE_REF);
		}
	    }

	    else if (next.isORB()) {
		_logger.fine(" we have an ORB resource " + next);
		
		// Need to delay ejb context lookup until runtime
                obj = new J2EEEnvWrapper(next, J2EEEnvWrapper.ORB_RESOURCE);
	    }

            else if(next.isWebServiceContext()) {
                Object wsc = new com.sun.enterprise.webservice.WebServiceContextImpl();
                obj = new J2EEEnvWrapper(wsc, J2EEEnvWrapper.WEBSERVICE_CONTEXT);
            }

            else {
                try {		 
                    // Find the actual object to which this reference is mapped
                    Object res = initialContext.lookup(resJndi);
                    obj = new J2EEEnvWrapper(res, J2EEEnvWrapper.MUTABLE);
                }
                catch( NamingException ne ) {
                    String msg = localStrings.getLocalString
                    ("naming.unresolved.warning", "",
                    new Object[] {logicalJndiName, resJndi});
                    _logger.log(Level.FINE,msg);
                    // TN - allow unresolved reference at deployment time
                }
                if ( obj == null ) { // perhaps the resource is not yet deployed
                    obj = new J2EEEnvWrapper(resJndi,
                    J2EEEnvWrapper.MUTABLE_RESOURCE_REF);
                }
            }
            
            // Add the resource to the component's local java:comp namespace
            if ( namespace.put(logicalJndiName, obj) != null) {
                _logger.log(Level.WARNING,
                localStrings.getLocalString("naming.alreadyexists",
                "Reference name [{0}] already exists in {1}",
                new Object[] {next.getName(), getApplicationName(env)}));
            }

            bindIntermediateContexts(namespace, logicalJndiName);
        }
        
        for (Iterator itr = env.getServiceReferenceDescriptors().iterator(); 
             itr.hasNext();) {
	    ServiceReferenceDescriptor next = 
                (ServiceReferenceDescriptor) itr.next();

            if(next.getMappedName() != null) {
                next.setName(next.getMappedName());
            }
            String logicalJndiName = descriptorToLogicalJndiName(next);

            // Set WSDL File URL here if it null (happens during server restart)
            if((next.getWsdlFileUrl() == null)  && 
                (next.getWsdlFileUri() != null)) {
                try {
                    if(next.getWsdlFileUri().startsWith("http")) {
                        // HTTP URLs set as is
                        next.setWsdlFileUrl(new URL(next.getWsdlFileUri()));
                    } else if((new File(next.getWsdlFileUri())).isAbsolute()) {
                        // Absolute WSDL file paths set as is
                        next.setWsdlFileUrl((new File(next.getWsdlFileUri())).toURL());
                    } else {
                        // Relative WSDL file paths prefixed with module dir
                        BaseManager mgr=null;
                        if(next.getBundleDescriptor().getApplication().isVirtual()) {
                            ModuleType mType = next.getBundleDescriptor().getModuleType();
                            if(mType.equals(ModuleType.WAR)) {
                                mgr = DeploymentServiceUtils.getInstanceManager(DeployableObjectType.WEB);
                            } else if(mType.equals(ModuleType.EAR)) {
                                mgr = DeploymentServiceUtils.getInstanceManager(DeployableObjectType.APP);
                            } else if(mType.equals(ModuleType.EJB)) {
                                mgr = DeploymentServiceUtils.getInstanceManager(DeployableObjectType.EJB);
                            } else if(mType.equals(ModuleType.CAR)) {
                                mgr = DeploymentServiceUtils.getInstanceManager(DeployableObjectType.CAR);
                            }
                            String deployedDir = 
                                mgr.getLocation(next.getBundleDescriptor().getApplication().getRegistrationName());
                            if(deployedDir != null) {
                                File fileURL;
                                if(next.getBundleDescriptor().getApplication().isVirtual()) {
                                    fileURL = new File(deployedDir+File.separator+next.getWsdlFileUri());
                                } else {
                                    fileURL = new File(deployedDir+File.separator+
                                            next.getBundleDescriptor().getModuleDescriptor().getArchiveUri().replaceAll("\\.", "_") +
                                            File.separator +next.getWsdlFileUri());
                                }
                                next.setWsdlFileUrl(fileURL.toURL());
                            }
                        }
                    }
                } catch (Throwable mex) {
                    throw new NamingException(mex.getLocalizedMessage());
                }
            }
            
            // Delay creation of service ref until access time
            // to avoid classloading difficulties.  (E.g. Setting
            // up service-ref for web component requires classes
            // that could be in .war or in .war's library jar.
            // Better to let context classloader handle it later...)
            Object serviceRefObject = 
                new J2EEEnvWrapper(next, J2EEEnvWrapper.SERVICE_REF);
                                                         
	    _logger.log(Level.INFO, "naming.bind",
		new Object[] {logicalJndiName});
	    namespace.put(logicalJndiName, serviceRefObject);
	    bindIntermediateContexts(namespace, logicalJndiName);
	}        

        for (EntityManagerFactoryReferenceDescriptor next : 
                 env.getEntityManagerFactoryReferenceDescriptors()) {

            String logicalJndiName = descriptorToLogicalJndiName(next);

            // Delay creation of actual object until lookup time.
            Object factoryRefObject = 
                new J2EEEnvWrapper(next, 
                                   J2EEEnvWrapper.ENTITY_MANAGER_FACTORY_REF);
                                                         
	    _logger.log(Level.INFO, "naming.bind",
		new Object[] {logicalJndiName});
	    namespace.put(logicalJndiName, factoryRefObject);
	    bindIntermediateContexts(namespace, logicalJndiName);
	}

        for (EntityManagerReferenceDescriptor next : 
                 env.getEntityManagerReferenceDescriptors()) {

            String logicalJndiName = descriptorToLogicalJndiName(next);

            // Delay creation of actual object until lookup time.
            Object entityManagerRefObject = 
                new J2EEEnvWrapper(next, J2EEEnvWrapper.ENTITY_MANAGER_REF);
                                                         
	    _logger.log(Level.INFO, "naming.bind",
		new Object[] {logicalJndiName});
	    namespace.put(logicalJndiName, entityManagerRefObject);
	    bindIntermediateContexts(namespace, logicalJndiName);
	}

	return componentId;
    }

    private void bindIntermediateContexts(HashMap namespace, String name)
	throws NamingException
    {
	// for each component of name, put an entry into namespace
	name = name.substring("java:comp/".length());
	StringTokenizer toks = new StringTokenizer(name, "/", false);
	String partialName="java:comp";
	while ( toks.hasMoreTokens() ) {
	    String tok = toks.nextToken();
	    partialName = partialName + "/" + tok;
	    if ( namespace.get(partialName) == null ) {
        
		namespace.put(partialName, new javaURLContext(partialName, null)); 
	    }
        }
    }


    /**
     * This method enumerates the env properties, ejb and resource
     * references and unbinds them from the java:comp namespace.
     */
    public void unbindObjects(JndiNameEnvironment env) throws NamingException
    {
        String componentId = getMangledIdName(env);
	namespaces.remove(componentId); // remove local namespace cache
    }

    
    /**
     * Recreate a context for java:comp/env or one of its sub-contexts given
     * the context name.  
     */
    public Context restoreJavaCompEnvContext(String contextName) 
        throws NamingException 
    {
        if( !contextName.startsWith("java:" ) ) {
            throw new NamingException("Invalid context name [" + contextName
               + "]. Name must start with java:");
        }

        return new javaURLContext(contextName, null);        
    }

    public Object lookup(String name) throws NamingException
    {
        return lookup(name, null);
    }

    /**
     * This method is called from SerialContext class. 
     * The serialContext instance that was created by the appclient's 
     * Main class is passed so that stickiness is preserved.
     * Called from javaURLContext.lookup, for java:comp names.
    */
    public Object lookup(String name, SerialContext serialContext) 
	throws NamingException {
	_logger.fine("serialcontext in NamingManagerImpl.." + serialContext);
	Context ic = null;
	
	if (serialContext != null) {
	    ic = serialContext;	  
	} else {
	    ic = initialContext;
	}
	
        //initialContext is used as ic in case of PE while 
        //serialContext is used as ic in case of EE/SE
        if (_logger.isLoggable(Level.FINE))
	    _logger.log(Level.FINE,"NamingManagerImpl : looking up name : " + name);
	
	// Get the component id and namespace to lookup
	String componentId = getComponentId();
	
	HashMap namespace = (HashMap)namespaces.get(componentId);
	
	Object obj = namespace.get(name);
	
	if ( obj == null )
	    throw new NameNotFoundException("No object bound to name " + name);
	
	if ( obj instanceof J2EEEnvWrapper ) {
	    J2EEEnvWrapper wr = (J2EEEnvWrapper)obj;	
	    switch ( wr.type ) {
	    case J2EEEnvWrapper.MUTABLE: 
		// XXX is this copy necessary ?
		
		obj= NamingUtils.makeCopyOfObject(wr.object); 
		break;
		
	    case J2EEEnvWrapper.JDBC_REF: 
		obj = wr.object; 
		break;
		
	    case J2EEEnvWrapper.MAIL_REF: 
		
		// MailConfiguration config = (MailConfiguration)wr.object;
		String resJndi = (String)wr.object;
		MailConfiguration config =
		    (MailConfiguration)ic.lookup(resJndi);
		
		// Note: javax.mail.Session is not serializable,
		// but we need to get a new instance on every lookup.
		javax.mail.Session s = javax.mail.Session.getInstance(
								      config.getMailProperties(), null);
		s.setDebugOut(new PrintStream(new MailLogOutputStream()));
		s.setDebug(true);
		obj = s;
		break;
		
	    case J2EEEnvWrapper.ORB_RESOURCE:
		
		ResourceReferenceDescriptor resRef =
		    (ResourceReferenceDescriptor) wr.object;
		if (resRef.getSharingScope().equals(
			   ResourceReferenceDescriptor.RESOURCE_SHAREABLE)) {
		    //jndi name is java:comp/ORB
		    _logger.fine("ORB resource is shareable" + resRef.getJndiName());
		    obj = ic.lookup(resRef.getJndiName()); 		   
		} else {
		    //init a new ORB with empty args and an empty Properties obj
		    obj = ORB.init(new String[]{}, new Properties());
		    _logger.fine("ORB resource is unshareable" + obj);
		}
		break;
		
	    case J2EEEnvWrapper.EJB_REF: 
		// Lookup the target bean's EJBHome ref using JNDI name.
		// If the string is a "corbaname:...." URL
		// the lookup happens thru the corbanameURL context,
		// else it happens thru the COSNaming context.
		
		// if ic is equal to initialContext then we are in 
		//PE environment
		// else we should use sticky serialContext's private 
		//field cosContext which is an IC pointing to that 
		//specific orb's CosNaming Service.
		
		/* MEJB lookup involved one more level of indirection
		 * hence, the following check.
		 * if name starts with corbaname: then use the 
		 * initialcontext pointing to CosNaming Service 
		 * (sticky or local)
		 * else use sticky SerialContext or InitialContext 
		 * that is set in NamingManagerImpl()
		 */
		EjbReferenceDescriptor remoteEjbRef = 
		    (EjbReferenceDescriptor) wr.object;
		
		// Get actual jndi-name from ejb module.
		String remoteJndiName = 
		    EJBUtils.getRemoteEjbJndiName(remoteEjbRef);
		
		if( _logger.isLoggable(Level.FINE) ) {
		    _logger.fine("EJB_REF..." + remoteEjbRef);
		}
		
		if (remoteJndiName.startsWith(CORBANAME)) {
		    ORB orb = ORBManager.getORB();		
		    obj = (Object) orb.string_to_object(remoteJndiName);
		} else {
		    if (remoteJndiName.startsWith(IIOPURL)) {
		        Properties env = new Properties();
			env.put("java.naming.corba.orb",ORBManager.getORB());
			Context ctx = new InitialContext(env);
			obj = ctx.lookup(remoteJndiName);
		    } else {
		        obj = ic.lookup(remoteJndiName);
		    }
		}
		
		obj = EJBUtils.resolveEjbRefObject(remoteEjbRef, obj);
		
		//if serialContext is null, no loadbalancing is 
		//happening. So cache the ref obj. else dont, 
		//inorder to do load balancing
		if (serialContext == null) {
		    if( EJBUtils.isEjbRefCacheable(remoteEjbRef) ) {
			namespace.put(name, obj); 
		    }
		}
		
		break;
		
	    case J2EEEnvWrapper.EJBLOCAL_REF: 
		
		EjbReferenceDescriptor localEjbRef = 
		    (EjbReferenceDescriptor) wr.object;
		
		// @@@ revisit api
		obj = EJBUtils.resolveEjbRefObject(localEjbRef, null);
		
		// No need to check load-balancing when determining whether
		// to cache ejb-local-ref object b/c ejb local-refs are
		// only available to components running within the server.
		// However, still need to check with ejb module to determine
		// whether the ref itself is cacheable.  
		if( EJBUtils.isEjbRefCacheable(localEjbRef) ) {
		    namespace.put(name, obj); 
		}
		
		break;
		
	    case J2EEEnvWrapper.EJB_CONTEXT:
		
		JmsDestinationReferenceDescriptor jmsRef =
		    (JmsDestinationReferenceDescriptor) wr.object;
		
		// Get current ejb context.  This should never be cached
		// within the namespace.
		obj = Switch.getSwitch().getContainerFactory().
                    getEJBContextObject(jmsRef.getRefType());
		    
		
		break;
		
	    case J2EEEnvWrapper.MUTABLE_RESOURCE_REF: 
		
		// lookup resource using JNDI name
		
		obj = ic.lookup((String)wr.object);
                
		// replace wr with a MUTABLE wrapper for future lookups.
		// Note: this is not a structural modification, 
		// so no synchronization needed.
		wr = new J2EEEnvWrapper(obj, J2EEEnvWrapper.MUTABLE);
		
		//if serialContext is null, no loadbalancing is 
		//happening. So cache the ref obj. else dont, 
		//in order to do load balancing
		if (serialContext == null) {
		    namespace.put(name, wr);
		}
		break;
		
	    case J2EEEnvWrapper.RESOURCE_ADAPTER: 
		
		obj=wr.object; 
		
		break;   
		
	    case J2EEEnvWrapper.RESOURCE_ADAPTER_REF: 
		
		// lookup resource using JNDI name
		
		obj = ic.lookup((String)wr.object);
		
		break;            
		
	    case J2EEEnvWrapper.SERVICE_REF :
		ServiceReferenceDescriptor desc = 
		    (ServiceReferenceDescriptor) wr.object;
		obj = getClientServiceObject(desc);
		// Replace wrapper with actual service ref object
		namespace.put(name, obj);
		break;	       
		
	    case J2EEEnvWrapper.ENTITY_MANAGER_FACTORY_REF :
		EntityManagerFactoryReferenceDescriptor emfRefDesc = 
		    (EntityManagerFactoryReferenceDescriptor) wr.object;
		obj = new EntityManagerFactoryWrapper(emfRefDesc);
		
		// Do not cache resulting object so that each lookup
		// will create a new wrapper object.
		
		break; 
		
	    case J2EEEnvWrapper.ENTITY_MANAGER_REF :
		EntityManagerReferenceDescriptor emRefDesc = 
		    (EntityManagerReferenceDescriptor) wr.object;
		obj = new EntityManagerWrapper(emRefDesc);
		
		// Do not cache resulting object so that each lookup
		// will create a new wrapper object.
		
		break; 

            case J2EEEnvWrapper.WEBSERVICE_CONTEXT :
                // lookup resource using JNDI name; if not present
                obj = wr.object;
                break;
	    }
	}
	if (obj instanceof com.sun.enterprise.naming.java.javaURLContext) {
	    if (serialContext != null) {
	        //in EE mode 
	        return ((com.sun.enterprise.naming.java.javaURLContext)obj).addStickyContext(serialContext);
	    }
	}
	return obj;
    }
    
    private void resolvePortComponentLinks(ServiceReferenceDescriptor desc) 
        throws Exception {
            
        // Resolve port component links to target endpoint address.
        // We can't assume web service client is running in same VM
        // as endpoint in the intra-app case because of app clients.
        //
        // Also set port-qname based on linked port's qname if not 
        // already set.
        for(Iterator iter = desc.getPortsInfo().iterator(); iter.hasNext();) {
            ServiceRefPortInfo portInfo = (ServiceRefPortInfo) iter.next();

            if( portInfo.isLinkedToPortComponent() ) {
                WebServiceEndpoint linkedPortComponent =
                    portInfo.getPortComponentLink();
                
                // XXX-JD we could at this point try to figure out the 
                // endpoint-address from the ejb wsdl file but it is a 
                // little complicated so I will leave it for post Beta2
                if( !(portInfo.hasWsdlPort()) ) {
                    portInfo.setWsdlPort(linkedPortComponent.getWsdlPort());
                }
            }
        }
    }
    
    private Object initiateInstance(Class svcClass, ServiceReferenceDescriptor desc) 
                                                    throws Exception {
        
        java.lang.reflect.Constructor cons = svcClass.getConstructor(new Class[]{java.net.URL.class, 
                                    javax.xml.namespace.QName.class});
        com.sun.enterprise.webservice.ServiceRefDescUtil descUtil = 
           new com.sun.enterprise.webservice.ServiceRefDescUtil();
        descUtil.preServiceCreate(desc);
        WsUtil wsu = new WsUtil();
        URL wsdlFile = wsu.privilegedGetServiceRefWsdl(desc);
        // Check if there is a catalog for this web service client
        // If so resolve the catalog entry
        String genXmlDir;
        if(desc.getBundleDescriptor().getApplication() != null) {
            genXmlDir = desc.getBundleDescriptor().getApplication().getGeneratedXMLDirectory();
           if(!desc.getBundleDescriptor().getApplication().isVirtual()) {
                String subDirName = desc.getBundleDescriptor().getModuleDescriptor().getArchiveUri();
                genXmlDir += (File.separator+subDirName.replaceAll("\\.",  "_"));
            }
        } else {
            // this is the case of an appclient being run as class file from command line
            genXmlDir = desc.getBundleDescriptor().getModuleDescriptor().getArchiveUri();
        }
        File catalogFile = new File(genXmlDir,
                desc.getBundleDescriptor().getDeploymentDescriptorDir() +
                    File.separator + "jax-ws-catalog.xml");
        if(catalogFile.exists()) {
            wsdlFile = wsu.resolveCatalog(catalogFile, desc.getWsdlFileUri(), null);
        }        
        Object obj =   
           cons.newInstance(wsdlFile, desc.getServiceName());
        descUtil.postServiceCreate();
        return obj;
        
    }
    
    private Object getClientServiceObject(ServiceReferenceDescriptor desc) 
      throws NamingException {

        Class serviceInterfaceClass = null;
        Object returnObj = null;
        WsUtil wsUtil = new WsUtil();

        try {

            WSContainerResolver.set(desc);
            
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            
            serviceInterfaceClass = cl.loadClass(desc.getServiceInterface());

            resolvePortComponentLinks(desc);

            Service serviceDelegate = null;
            javax.xml.ws.Service jaxwsDelegate = null;
            Object injValue = null;

            if( desc.hasGeneratedServiceInterface() || desc.hasWsdlFile() ) {

                String serviceImplName  = desc.getServiceImplClassName();
                if(serviceImplName != null) {
                    Class serviceImplClass  = cl.loadClass(serviceImplName);
                    serviceDelegate = (Service) serviceImplClass.newInstance();
                } else {
                  
                    // The target is probably a post JAXRPC-1.1- based service;
                    // If Service Interface class is set, check if it is indeed a subclass of Service
                    // initiateInstance should not be called if the user has given javax.xml.ws.Service itself
                    // as the interface through DD
                    if(javax.xml.ws.Service.class.isAssignableFrom(serviceInterfaceClass) &&
                        !javax.xml.ws.Service.class.equals(serviceInterfaceClass)) {
                        // OK - the interface class is indeed the generated service class; get an instance
                        injValue = initiateInstance(serviceInterfaceClass, desc);
                    } else {
                        // First try failed; Try to get the Service class type from injected field name
                        // and from there try to get an instance of the service class
                        
                        // I assume the at all inejction target are expecting the SAME service 
                        // interface, therefore I take the first one.
                        if (desc.isInjectable()) {
                            
                            InjectionTarget target = desc.getInjectionTargets().iterator().next();
                            Class serviceType = null;
                            if (target.isFieldInjectable()) {
                                java.lang.reflect.Field f = target.getField();
                                if(f == null) {
                                    String fName = target.getFieldName();
                                    Class targetClass = cl.loadClass(target.getClassName());
                                    try {
                                        f = targetClass.getDeclaredField(target.getFieldName());
                                    } catch(java.lang.NoSuchFieldException nsfe) {}// ignoring exception
                                } 
                                serviceType = f.getType();
                            }
                            if (target.isMethodInjectable()) {
                                Method m = target.getMethod();
                                if(m == null) {
                                    String mName = target.getMethodName();
                                    Class targetClass = cl.loadClass(target.getClassName());
                                    try {
                                        m = targetClass.getDeclaredMethod(target.getMethodName());
                                    } catch(java.lang.NoSuchMethodException nsfe) {}// ignoring exception
                                } 
                                if (m.getParameterTypes().length==1) {
                                    serviceType = m.getParameterTypes()[0];
                                }
                            }
                            if (serviceType!=null){
                                Class loadedSvcClass = cl.loadClass(serviceType.getCanonicalName());
                                injValue = initiateInstance(loadedSvcClass, desc);
                            }
                        }
                    }
                    // Unable to get hold of generated service class -> try the Service.create avenue to get a Service
                    if(injValue == null) {
                        // Here create the service with WSDL (overridden wsdl if wsdl-override is present)
                        // so that JAXWS runtime uses this wsdl @ runtime
                        javax.xml.ws.Service svc = 
                            javax.xml.ws.Service.create((new WsUtil()).privilegedGetServiceRefWsdl(desc),
                                desc.getServiceName());
                        jaxwsDelegate = new JAXWSServiceDelegate(desc, svc, cl);
                    } 
                }
                    
                if( desc.hasHandlers() ) {
                    // We need the service's ports to configure the
                    // handler chain (since service-ref handler chain can
                    // optionally specify handler-port association) 
                    // so create a configured service and call getPorts
                    Service configuredService = 
                        wsUtil.createConfiguredService(desc);
                    Iterator ports = configuredService.getPorts();
                    wsUtil.configureHandlerChain
                        (desc, serviceDelegate, ports, cl);
                }
                
                // check if this is a post 1.1 web service
                if(javax.xml.ws.Service.class.isAssignableFrom(serviceInterfaceClass)) {
                    // This is a JAXWS based webservice client; 
		    // process handlers and mtom setting
		    // moved test for handlers into wsUtil, in case
		    // we have to add system handler

		    javax.xml.ws.Service service = 
			(injValue != null ? 
			 (javax.xml.ws.Service) injValue : jaxwsDelegate);

		    if (service != null) {
                        // Now configure client side handlers
			wsUtil.configureJAXWSClientHandlers(service, desc);
		    }
                    // the requested resource is not the service but one of its port.
                    if (injValue!=null && desc.getInjectionTargetType()!=null) {
                        Class requestedPortType = service.getClass().getClassLoader().loadClass(desc.getInjectionTargetType());
                        injValue = service.getPort(requestedPortType);
                    }
                    
                }

            } else {
                // Generic service interface / no WSDL
                QName serviceName = desc.getServiceName();
                if( serviceName == null ) {
                    // ServiceFactory API requires a service-name.
                    // However, 109 does not allow getServiceName() to be
                    // called, so it's ok to use a dummy value.
                    serviceName = new QName("urn:noservice", "servicename");
                }
                ServiceFactory serviceFac = ServiceFactory.newInstance();
                serviceDelegate = serviceFac.createService(serviceName);
            } 

            // Create a proxy for the service object.
            // Get a proxy only in jaxrpc case because in jaxws the service class is not
            // an interface any more            
            InvocationHandler handler = null;
            if(serviceDelegate != null) {
                handler = new ServiceInvocationHandler(desc, serviceDelegate, cl);
                returnObj = Proxy.newProxyInstance
                    (cl, new Class[] { serviceInterfaceClass }, handler);                
            } else if(jaxwsDelegate != null) {
                returnObj = jaxwsDelegate; 
            } else if(injValue != null) {
                returnObj = injValue;
            }
        } catch(PrivilegedActionException pae) {
            _logger.log(Level.WARNING, "", pae);
            NamingException ne = new NamingException();
            ne.initCause(pae.getCause());
            throw ne;
        } catch(Exception e) {
            _logger.log(Level.WARNING, "", e);
            NamingException ne = new NamingException();
            ne.initCause(e);
            throw ne;
        } finally {
            WSContainerResolver.unset();
        }
        
        return returnObj;
    }
    


    public NamingEnumeration list(String name) throws NamingException
    {
	ArrayList list = listNames(name);
	return new NamePairsEnum(this, list.iterator());
    }

    public NamingEnumeration listBindings(String name) throws NamingException
    {
	ArrayList list = listNames(name);
	return new BindingsEnum(this, list.iterator());	
    }

    private ArrayList listNames(String name) throws NamingException
    {
	// Get the component id and namespace to lookup
	String componentId = getComponentId();
	HashMap namespace = (HashMap)namespaces.get(componentId);

	Object obj = namespace.get(name);

	if ( obj == null )
	    throw new NameNotFoundException("No object bound to name " + name);

	if ( !(obj instanceof javaURLContext) )
	    throw new NotContextException(name + " cannot be listed");

	// This iterates over all names in entire component namespace,
	// so its a little inefficient. The alternative is to store
	// a list of bindings in each javaURLContext instance.
	ArrayList list = new ArrayList();
	Iterator itr = namespace.keySet().iterator();
	if ( !name.endsWith("/") )
	    name = name + "/";
	while ( itr.hasNext() ) {
	    String key = (String)itr.next(); 
	    // Check if key begins with name and has only 1 component extra
	    // (i.e. no more slashes)
	    if ( key.startsWith(name) 
		 && key.indexOf('/', name.length()) == -1 )
		list.add(key);
	}
	return list;
    }


    /**
     * Get the component id from the Invocation Manager.
     * @return the component id as a string.
     */
    private String getComponentId() throws NamingException
    {
        String id = null;

        ComponentInvocation ci  = im.getCurrentInvocation();
        if (ci == null) {
            throw new NamingException("invocation exception ");
        }

        try {
            Object containerContext = ci.getContainerContext();

            if(containerContext == null) {
                throw new NamingException("No container context");
            }
	    if ( containerContext instanceof Container )
		return ((Container)containerContext).getComponentId();

	    JndiNameEnvironment desc = (JndiNameEnvironment) 
			    theSwitch.getDescriptorFor(containerContext);
	    id = getMangledIdName(desc);

        } catch(InvocationException e) {
            NamingException ine = new NamingException("invocation exception");
            ine.initCause(e);
            throw ine;
        }
        return id;
    }


    /**
     * Generate the name of an environment property in the java:comp/env 
     * namespace.  This is the lookup string used by a component to access
     * its environment.
     */
    private String descriptorToLogicalJndiName(Descriptor descriptor) {
        return JAVA_COMP_STRING + descriptor.getName();
    }

    private int getComponentType(JndiNameEnvironment env) {
        int componentType = UNKNOWN_COMPONENT;
        if(env instanceof EjbDescriptor) {
            componentType = EJB_COMPONENT;
        } else if (env instanceof WebBundleDescriptor) {
            componentType = WEB_COMPONENT;
        } else if (env instanceof ApplicationClientDescriptor) {
            componentType = APP_CLIENT_COMPONENT;
        } else {
            throw new IllegalArgumentException("Unknown component type" +
                                               env);
        }
        return componentType;
    }

    /**
     * Generate a unique id name for each J2EE component.  
     */
    private String getMangledIdName(JndiNameEnvironment env) {
	String id = null;
        int componentType = getComponentType(env);

        switch(componentType) {
        case EJB_COMPONENT :
            // EJB component
	    EjbDescriptor ejbEnv = (EjbDescriptor) env;

            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                            "Application:" + ejbEnv.getApplication());
            }
            
            // Make jndi name flat so it won't result in the creation of
            // a bunch of sub-contexts.
            String flattedJndiName = ejbEnv.getJndiName().replace('/', '.');

            EjbBundleDescriptor ejbBundle = ejbEnv.getEjbBundleDescriptor();
	    id = ejbEnv.getApplication().getName() + ID_SEPARATOR + 
                ejbBundle.getModuleDescriptor().getArchiveUri() 
                + ID_SEPARATOR +
                ejbEnv.getName() + ID_SEPARATOR + flattedJndiName +
                ejbEnv.getUniqueId();

            break;

        case WEB_COMPONENT :
            WebBundleDescriptor webEnv = (WebBundleDescriptor) env;
	    id = webEnv.getApplication().getName() + ID_SEPARATOR + 
                webEnv.getContextRoot();
            break;

        case APP_CLIENT_COMPONENT :
            ApplicationClientDescriptor appEnv = 
		(ApplicationClientDescriptor) env;
	    id = "client" + ID_SEPARATOR + appEnv.getName() + 
                ID_SEPARATOR + appEnv.getMainClassName();
            break;
        }

        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Component Id: " + id);
        }
        return id;
    }
    
    private boolean isCOSNamingObj(Object obj) {
        return ((obj instanceof java.rmi.Remote) ||
                (obj instanceof org.omg.CORBA.Object));
    }

    private String getApplicationName(JndiNameEnvironment env) {
        String appName = "";
        int componentType = getComponentType(env);
        String moduleName = "";

        switch(componentType) {
        case EJB_COMPONENT :
            // EJB component
	    EjbDescriptor ejbEnv = (EjbDescriptor) env;
            EjbBundleDescriptor ejbBundle = ejbEnv.getEjbBundleDescriptor();
	    appName = "ejb ["+
                ejbEnv.getApplication().getRegistrationName();
            moduleName = ejbEnv.getName();
            if (moduleName == null || moduleName.equals("")) {
                appName = appName+"]";
            }
            else {
                appName = appName+":"+ejbEnv.getName()+"]";
            }
            break;
        case WEB_COMPONENT :
            WebBundleDescriptor webEnv = (WebBundleDescriptor) env;
	    appName = "web module ["+
                webEnv.getApplication().getRegistrationName();
            moduleName = webEnv.getContextRoot();
            if (moduleName == null || moduleName.equals("")) {
                appName = appName+"]";
            }
            else {
                appName = appName+":"+webEnv.getContextRoot()+"]";
            }
            break;
        case APP_CLIENT_COMPONENT :
            ApplicationClientDescriptor appEnv = 
		(ApplicationClientDescriptor) env;
	    appName =  "client ["+appEnv.getName() + 
                ":" + appEnv.getMainClassName()+"]";
            break;
        }
        return appName;
    }


    private boolean isConnector(String logicalJndiName){
	    return (logicalJndiName.indexOf(EIS_STRING)!=-1);
    }  
}
    

// Class for enumerating name/class pairs
class NamePairsEnum implements NamingEnumeration {
    NamingManagerImpl nm;
    Iterator names;

    NamePairsEnum(NamingManagerImpl nm, Iterator names) {
	this.nm = nm;
	this.names = names;
    }

    public boolean hasMoreElements() {
	return names.hasNext();
    }

    public boolean hasMore() throws NamingException {
	return hasMoreElements();
    }

    public Object nextElement() {
	if(names.hasNext()) {
	    try {
		String name = (String)names.next();
		String className = nm.lookup(name).getClass().getName();
		return new NameClassPair(name, className);
	    } catch ( Exception ex ) {
		throw new RuntimeException("Exception during lookup: "+ex);
	    }
	}
	else
	    return null;
    }

    public Object next() throws NamingException {
	return nextElement();
    }

    // New API for JNDI 1.2
    public void close() throws NamingException {
	throw new OperationNotSupportedException("close() not implemented");
    }
}

// Class for enumerating bindings
class BindingsEnum implements NamingEnumeration {
    NamingManagerImpl nm;
    Iterator names;

    BindingsEnum(NamingManagerImpl nm, Iterator names) {
	this.nm = nm;
	this.names = names;
    }

    public boolean hasMoreElements() {
	return names.hasNext();
    }

    public boolean hasMore() throws NamingException {
	return hasMoreElements();
    }

    public Object nextElement() {
	if( names.hasNext() ) {
	    try {
		String name = (String)names.next();
		return new Binding(name, nm.lookup(name));
	    } catch ( Exception ex ) {
		throw new RuntimeException("Exception during lookup: "+ex);
	    }
	}
	else
	    return null;
    }

    public Object next() throws NamingException {
	return nextElement();
    }

    // New API for JNDI 1.2
    public void close() throws NamingException {
	throw new OperationNotSupportedException("close() not implemented");
    }
}

// Class for storing references to EJBHome names
class J2EEEnvWrapper {
    static final int MUTABLE = 1;
    static final int MAIL_REF = 2;
    static final int EJB_REF = 3;
    static final int EJBLOCAL_REF = 4;
    static final int MUTABLE_RESOURCE_REF = 5;

    static final int RESOURCE_ADAPTER = 6;

    static final int RESOURCE_ADAPTER_REF = 7;

    static final int JDBC_REF = 8;

    static final int SERVICE_REF = 9;

    static final int EJB_CONTEXT = 10;

    static final int ENTITY_MANAGER_FACTORY_REF = 11;

    static final int ENTITY_MANAGER_REF = 12;

    static final int ORB_RESOURCE = 13;
    
    static final int WEBSERVICE_CONTEXT = 14;
    
    int type;
    Object object;
 
    J2EEEnvWrapper(Object object, int type) {
	this.object = object;
	this.type = type;
    }
}
