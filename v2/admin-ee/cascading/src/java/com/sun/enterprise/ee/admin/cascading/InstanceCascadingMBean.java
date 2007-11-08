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

package com.sun.enterprise.ee.admin.cascading;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.*;
import javax.management.remote.JMXConnector;

/**
 * The InstanceCascadingMBean enables dynamic cascading of server instances
 *
 * @version	1.00
 * @author	Sreenivas Munnangi
 */

public class InstanceCascadingMBean implements DynamicMBean {

    // vars
    private String dClassName = this.getClass().getName();
    private String dDescription = "Implementation of a instance cascading dynamic MBean";

    private MBeanAttributeInfo[] dAttributes = new MBeanAttributeInfo[0];
    private MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[1];
    private MBeanOperationInfo[] dOperations = new MBeanOperationInfo[2];
    private MBeanInfo dMBeanInfo = null;

    // logger
    private static Logger _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
    private static final StringManager _strMgr =
	StringManager.getManager(InstanceCascadingMBean.class);

    /**
     * builds dynamic mbean info
     */
    public InstanceCascadingMBean() {
	buildDynamicMBeanInfo();
    }

    /**
     * Allows the value of the specified attribute of 
     * the Dynamic MBean to be obtained.
     */
    public Object getAttribute(String attribute_name) 
	throws AttributeNotFoundException,
	       MBeanException,
	       ReflectionException {

	// Check attribute_name is not null to avoid NullPointerException later on
	if (attribute_name == null) {
	    throw new RuntimeOperationsException(
		new IllegalArgumentException(_strMgr.getString("attr_name_null")),
                _strMgr.getString("getter_null_attr", dClassName));
	}

	// Check for a recognized attribute_name and call the corresponding getter
	/*
	if (attribute_name.equals("xyz")) {
	    return getXyz();
	} 
	*/

	// If attribute_name has not been recognized throw an AttributeNotFoundException
	throw(new AttributeNotFoundException
                (_strMgr.getString("attr_not_found", attribute_name, dClassName)));
    }

    /**
     * Sets the value of the specified attribute of the Dynamic MBean.
     */
    public void setAttribute(Attribute attribute) 
	throws AttributeNotFoundException,
	       InvalidAttributeValueException,
	       MBeanException, 
	       ReflectionException {

	// Check attribute is not null to avoid NullPointerException later on
	if (attribute == null) {
	    throw new RuntimeOperationsException(
		new IllegalArgumentException(_strMgr.getString("attr_name_null")),
                _strMgr.getString("setter_null_attr", dClassName));
	}

	String name = attribute.getName();
	Object value = attribute.getValue();

	if (name == null) {
	    throw new RuntimeOperationsException(
		new IllegalArgumentException(_strMgr.getString("attr_name_null")),
                _strMgr.getString("setter_null_attr", dClassName));
	}

    }

    /**
     * Enables the to get the values of several attributes of the Dynamic MBean.
     */
    public AttributeList getAttributes(String[] attributeNames) {

	// Check attributeNames is not null to avoid NullPointerException later on
	if (attributeNames == null) {
	    throw new RuntimeOperationsException(
		new IllegalArgumentException(_strMgr.getString("attr_array_null")),
                _strMgr.getString("getter_null_attr", dClassName));
	}

	AttributeList resultList = new AttributeList();

	// if attributeNames is empty, return an empty result list
	if (attributeNames.length == 0)
	    return resultList;
        
	// build the result attribute list
	for (int i=0 ; i<attributeNames.length ; i++){
	    try {        
		Object value = getAttribute((String) attributeNames[i]);     
		resultList.add(new Attribute(attributeNames[i],value));
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return resultList;
    }

    /**
     * Sets the values of several attributes of the Dynamic MBean, and returns the
     * list of attributes that have been set.
     */
    public AttributeList setAttributes(AttributeList attributes) {

	// Check attributes is not null to avoid NullPointerException later on
	if (attributes == null) {
	    throw new RuntimeOperationsException(
		new IllegalArgumentException(_strMgr.getString("attr_list_null")),
                _strMgr.getString("setter_null_attr", dClassName));
	}

	AttributeList resultList = new AttributeList();

	// if attributeNames is empty, nothing more to do
	if (attributes.isEmpty())
	    return resultList;

	// for each attribute, try to set it and add to the result list if successfull
	for (Iterator i = attributes.iterator(); i.hasNext();) {
	    Attribute attr = (Attribute) i.next();
	    try {
		setAttribute(attr);
		String name = attr.getName();
		Object value = getAttribute(name); 
		resultList.add(new Attribute(name,value));
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}
	return resultList;
    }

    /**
     * Allows an operation to be invoked on the Dynamic MBean.
     */
    public Object invoke(String operationName, Object params[], String signature[])
	throws MBeanException,
	       ReflectionException {

	// Check operationName is not null to avoid NullPointerException later on
	if (operationName == null) {
	    throw new RuntimeOperationsException(
		new IllegalArgumentException(_strMgr.getString("opr_name_null")),
                _strMgr.getString("opn_null", dClassName));
	}

	// Check for a recognized operation name and call the corresponding operation
	if (operationName.equals("cascadeInstance")){
		if (params[0] == null) {
	            throw new RuntimeOperationsException(
		        new IllegalArgumentException(_strMgr.getString("svr_name_null")),
                        _strMgr.getString("opn_cascade_null", dClassName));
		}
		cascadeInstance((String) params[0]);
		return null;
	} else if (operationName.equals("stopCascadeInstance")){
		if (params[0] == null) {
	            throw new RuntimeOperationsException(
		        new IllegalArgumentException(_strMgr.getString("svr_name_null")),
                        _strMgr.getString("opn_cascade_null", dClassName));
		}
		stopCascadeInstance((String) params[0]);
		return null;
	} else { 
	    // unrecognized operation name:
	    throw new ReflectionException(new NoSuchMethodException(operationName), 
                _strMgr.getString("opn_not_found", operationName, dClassName));
	}
    }

    /**
     * This method provides the exposed attributes and operations of the Dynamic MBean.
     * It provides this information using an MBeanInfo object.
     */
    public MBeanInfo getMBeanInfo() {

	// return the information we want to expose for management:
	// the dMBeanInfo private field has been built at instanciation time,
	return dMBeanInfo;
    }


    /**
     * Build the private dMBeanInfo field,
     * which represents the management interface exposed by the MBean;
     * that is, the set of attributes, constructors, operations and notifications
     * which are available for management. 
     *
     * A reference to the dMBeanInfo object is returned by the getMBeanInfo() method
     * of the DynamicMBean interface. Note that, once constructed, 
     * an MBeanInfo object is immutable.
     */
    private void buildDynamicMBeanInfo() {

	Constructor[] constructors = this.getClass().getConstructors();
	dConstructors[0] = new MBeanConstructorInfo(
		"InstanceCascadingMBean(): Constructs InstanceCascadingMBean object",
		constructors[0]);
        
	MBeanParameterInfo[] params = new MBeanParameterInfo[] {
		new MBeanParameterInfo("instanceName", "java.lang.String", "Server Instance Name")
	};

	dOperations[0] = new MBeanOperationInfo("cascadeInstance",
						"cascade remote server instance",
						params , 
						"void", 
						MBeanOperationInfo.ACTION);
        
	dOperations[1] = new MBeanOperationInfo("stopCascadeInstance",
						"stop cascading remote server instance",
						params , 
						"void", 
						MBeanOperationInfo.ACTION);
        
	dMBeanInfo = new MBeanInfo(dClassName,
				   dDescription,
				   dAttributes,
				   dConstructors,
				   dOperations,
				   new MBeanNotificationInfo[0]);
    }


    // private methods

    /**
     * start cascading instance
     */

    private void cascadeInstance(String instanceName) {

	// get jmx connector
	String domain = ApplicationServer.getServerContext().getDefaultDomainName();
	ConfigContext configContext = AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        try {
	    JMXConnector jmxConnector = ServerHelper.getJMXConnector(configContext, instanceName);
	    // invoke cascadeInstance
	    CascadingLifecycleImpl cli = new CascadingLifecycleImpl();
	    cli.cascadeInstance(domain, instanceName, jmxConnector);
        } catch (ConfigException ce) {
            // connector for this instance not available in the config
            ce.printStackTrace();
            _logger.log(Level.WARNING, "cascading.get_server_connector_config_error", ce);
        } catch (java.io.IOException ioe) {
            // connection cannot be made to this instance
            ioe.printStackTrace();
            _logger.log(Level.WARNING, "cascadingConnectException", ioe);
        }

    }


    /**
     * stop cascading instance
     */

    private void stopCascadeInstance(String instanceName) {
	// invoke stopCascadeInstance
	CascadingLifecycleImpl cli = new CascadingLifecycleImpl();
	cli.stopCascadeInstance(instanceName);
    }

}
