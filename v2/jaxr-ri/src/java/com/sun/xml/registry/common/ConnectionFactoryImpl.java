/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/


package com.sun.xml.registry.common;

import com.sun.xml.registry.common.util.*;
import java.util.*;
import javax.naming.*;
import javax.xml.registry.*;

/**
 * Class Declaration for Class1
 * @see
 * @author 
 */
public class ConnectionFactoryImpl extends ConnectionFactory implements Referenceable {

    private Properties properties;

    /**
     * Default constructor
     */
    public ConnectionFactoryImpl() {
	// do we need to initialize logs here?
    }
    
    public void setProperties(Properties properties) throws JAXRException {
	this.properties = properties;
    }
    
    public Properties getProperties() throws JAXRException {
        return properties;
    }
    
    /**
     * Create a named connection. Such a connection can be used to
     * communicate with a JAXR provider.
     *
     * @link dependency
     * @label creates
     * @associates <{Connection}>
     */
    public Connection createConnection() throws JAXRException {
        
        //get properties factory impl
        String registryFactoryClassString =
            (String) properties.get("javax.xml.registry.ConnectionFactoryClass");
        
        //get properties factory impl
        String queryManagerURLString =
            (String) properties.get("javax.xml.registry.queryManagerURL");
        String lifeCycleManagerURLString =
            (String) properties.get("javax.xml.registry.lifeCycleManagerURL");
       
        if (queryManagerURLString == null) {
            throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/common/LocalStrings").getString("ConnectionFactoryImpl:Missing_connection_property_javax.xml.registry.queryManagerURL"));
        }
        
	// default factory is currently uddi factory
        if (registryFactoryClassString == null) {
	    registryFactoryClassString = "com.sun.xml.registry.uddi.ConnectionFactoryImpl";
        }
        
        //need Classloader for j2ee integration
        ClassLoader classLoader;
        try {
            classLoader = this.getClass().getClassLoader();
        } catch (Exception x) {
            throw new JAXRException(x.toString(), x);
        }
        
        try {
            Class registryFactoryClass = null;
            if (classLoader == null) {
                registryFactoryClass =
                    Class.forName(registryFactoryClassString);
            } else {
                registryFactoryClass = classLoader.loadClass(registryFactoryClassString);
            }
            ConnectionFactory factory =
                (ConnectionFactory) registryFactoryClass.newInstance();
            factory.setProperties(properties);
            return factory.createConnection();
            
        } catch (java.lang.ClassNotFoundException cnfe) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/common/LocalStrings").getString("ConnectionFactoryImpl:Unable_to_create_connection"), cnfe);
        } catch (java.lang.InstantiationException ie) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/common/LocalStrings").getString("ConnectionFactoryImpl:Unable_to_create_connection"), ie);
        } catch (java.lang.IllegalAccessException iae) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/common/LocalStrings").getString("ConnectionFactoryImpl:Unable_to_create_connection"), iae);
        }
    }
    
    /**
     * Create a Federation.
     *
     * @param properties configuration properties that are either
     * specified by JAXR or provider specific.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param connections Is a Collection of Connection objects. Note that
     * Connection objects may also be Federation objects.
     *
     * @link dependency
     * @label creates
     * @associates <{Federation}>
     */
    public FederatedConnection createFederatedConnection(Collection connections)
    throws JAXRException {
        throw new UnsupportedCapabilityException();
    }
    
    /**
     * Retrieve the reference of this object. Used when
     * binding the object to a registry.
     *
     * @return Reference to the object.
     */
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(
            ConnectionFactoryImpl.class.getName(),
            ConnectionFactoryFactory.class.getName(),
            null);
        return ref;
    }
    
}
