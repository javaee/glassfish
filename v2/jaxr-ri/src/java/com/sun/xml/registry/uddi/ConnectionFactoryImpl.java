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


package com.sun.xml.registry.uddi;

import java.util.Properties;
import java.util.Collection;

import javax.xml.registry.*;
import com.sun.xml.registry.common.util.*;

/**
 * Class Declaration for Class1
 * @see
 * @author Farrukh S. Najmi
 */
public class ConnectionFactoryImpl extends ConnectionFactory {

    private Properties properties;

    /**
     * Default constructor
     */
    public ConnectionFactoryImpl() {
    }

    public void setProperties(Properties properties)
	throws JAXRException {
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
    public Connection createConnection() throws JAXRException, InvalidRequestException {	
        return new ConnectionImpl(properties);
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
    public FederatedConnection createFederatedConnection(Collection connections) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }	
	
}
