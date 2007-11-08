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

package com.sun.enterprise.management.agent;

import java.security.*;
import java.rmi.RemoteException;
import java.util.*;
import java.io.ObjectInputStream;
import javax.ejb.*;
import javax.management.*;
import javax.management.j2ee.*;
import javax.naming.*; 

/**
 * @ejbHome <{com.sun.enterprise.management.agent.MEJBHome}>
 * @ejbRemote <{com.sun.enterprise.management.agent.MEJB}>
 *
 *  @author Hans Hrasna
 */
public class MEJBBean implements SessionBean {
    private SessionContext ctx;
    private MEJBHelper mejbHelper = null;
    
    public void setSessionContext(SessionContext context) {
        ctx = context;
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    public void ejbCreate() throws CreateException {
        this.mejbHelper = MEJBHelper.getMEJBHelper();
    }

    // javax.management.j2ee.Management implementation starts here

    /**
     * Gets the names of managed objects controlled by the MEJB. This method
     * enables any of the following to be obtained: The names of all managed objects,
     * the names of a set of managed objects specified by pattern matching on the
     * <CODE>ObjectName</CODE> and/or a Query expression, a specific managed object name (equivalent to
     * testing whether an managed object is registered). When the object name is
     * null or no domain and key properties are specified, all objects are selected (and filtered if a
     * query is specified). It returns the set of ObjectNames for the managed objects selected.
     * @param name The object name pattern identifying the managed objects to be retrieved. If
     * null or no domain and key properties are specified, all the managed objects registered will be retrieved.
     * @param query The query expression to be applied for selecting managed objects. If null
     * no query expression will be applied for selecting managed objects.
     * @return  A set containing the ObjectNames for the managed objects selected.
     * If no managed object satisfies the query, an empty list is returned.
     */
    public Set queryNames(ObjectName name, QueryExp query) throws RemoteException {
        try{
            return this.mejbHelper.queryNames(name,query);
        }catch(Exception ex){
            throw new RemoteException(this.toString() +"::queryNames", ex);
        }
    }

    /**
     * Checks whether an MBean, identified by its object name, is already registered with the MBean server.
     * @param name The object name of the MBean to be checked.
     * @return  True if the MBean is already registered in the MBean server, false otherwise.
     */
    public boolean isRegistered(ObjectName name) throws RemoteException {
        try{
            return this.mejbHelper.isRegistered(name);
        }catch(Exception ex){
            throw new RemoteException(this.toString() +"::isRegistered", ex);
        }
    }

    /** Returns the number of MBeans registered in the MBean server. */
    public Integer getMBeanCount() throws RemoteException {
        try{
            return this.mejbHelper.getMBeanCount();
        }catch(Exception ex){
            throw new RemoteException(this.toString() +"::getMBeanCount", ex);
        }
    }

    /**
     * This method discovers the attributes and operations that an MBean exposes for management.
     * @param name The name of the MBean to analyze
     * @return  An instance of <CODE>MBeanInfo</CODE> allowing the retrieval of all attributes and operations of this MBean.
     * @exception IntrospectionException An exception occurs during introspection.
     * @exception InstanceNotFoundException The MBean specified is not found.
     * @exception ReflectionException An exception occurred when trying to invoke the getMBeanInfo of a Dynamic MBean.
     */
    public MBeanInfo getMBeanInfo(ObjectName name) throws javax.management.InstanceNotFoundException,
        javax.management.IntrospectionException, javax.management.ReflectionException, 
	java.io.IOException, RemoteException {
        return this.mejbHelper.getMBeanInfo(name);
    }

    /**
     * Gets the value of a specific attribute of a named MBean. The MBean is identified by its object name.
     * @param name The object name of the MBean from which the attribute is to be retrieved.
     * @param attribute A String specifying the name of the attribute to be retrieved.
     * @return  The value of the retrieved attribute.
     * @exception AttributeNotFoundException The attribute specified is not accessible in the MBean.
     * @exception MBeanException  Wraps an exception thrown by the MBean's getter.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown when trying to invoke the setter.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The object name in
     * parameter is null or the attribute in parameter is null.
     */
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException,
        javax.management.AttributeNotFoundException, javax.management.InstanceNotFoundException,
        javax.management.ReflectionException, java.io.IOException, RemoteException {
        return this.mejbHelper.getAttribute(name,attribute);
    }

    /**
     * Enables the values of several attributes of a named MBean. The MBean is identified by its object name.
     * @param name The object name of the MBean from which the attributes are retrieved.
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of the retrieved attributes.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception ReflectionException An exception occurred when trying to invoke the getAttributes method of a Dynamic MBean.
     * @exception RuntimeOperationsException Wrap a <CODE>java.lang.IllegalArgumentException</CODE>: The object name in
     * parameter is null or attributes in parameter is null.
     */
    public javax.management.AttributeList getAttributes(ObjectName name, String[] attributes)
        throws javax.management.InstanceNotFoundException, javax.management.ReflectionException, java.io.IOException, RemoteException {
        return this.mejbHelper.getAttributes(name,attributes);
    }

    /**
     * Sets the value of a specific attribute of a named MBean. The MBean is identified by its object name.
     * @param name The name of the MBean within which the attribute is to be set.
     * @param attribute The identification of the attribute to be set and the value it is to be set to.
     * @return  The value of the attribute that has been set.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception AttributeNotFoundException The attribute specified is not accessible in the MBean.
     * @exception InvalidAttributeValueException The value specified for the attribute is not valid.
     * @exception MBeanException Wraps an exception thrown by the MBean's setter.
     * @exception ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown when trying to invoke the setter.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The object name in
     * parameter is null or the attribute in parameter is null.
     */
    public void setAttribute(ObjectName name, javax.management.Attribute attribute)
        throws javax.management.InstanceNotFoundException, javax.management.AttributeNotFoundException,
        javax.management.InvalidAttributeValueException, MBeanException,
        javax.management.ReflectionException, java.io.IOException, RemoteException {
	this.mejbHelper.setAttribute(name,attribute);
    }

    /**
     * Sets the values of several attributes of a named MBean. The MBean is identified by its object name.
     * @param name The object name of the MBean within which the attributes are to be set.
     * @param attributes A list of attributes: The identification of the
     * attributes to be set and  the values they are to be set to.
     * @return  The list of attributes that were set, with their new values.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception ReflectionException An exception occurred when trying to invoke the getAttributes method of a Dynamic MBean.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The object name in
     * parameter is null or attributes in parameter is null.
     */
    public javax.management.AttributeList setAttributes(ObjectName name, javax.management.AttributeList attributes)
        throws javax.management.InstanceNotFoundException, javax.management.ReflectionException, java.io.IOException, RemoteException {
        return this.mejbHelper.setAttributes(name,attributes);
    }

    /**
     * Invokes an operation on an MBean.
     * @param name The object name of the MBean on which the method is to be invoked.
     * @param operationName The name of the operation to be invoked.
     * @param params An array containing the parameters to be set when the operation is invoked
     * @param signature An array containing the signature of the operation. The class objects will
     * be loaded using the same class loader as the one used for loading the MBean on which the operation was invoked.
     * @return  The object returned by the operation, which represents the result ofinvoking the operation
     * on the MBean specified.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception MBeanException  Wraps an exception thrown by the MBean's invoked method.
     * @exception ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method.
     */
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
        throws javax.management.InstanceNotFoundException, MBeanException,
        javax.management.ReflectionException, java.io.IOException, RemoteException {
        return this.mejbHelper.invoke(name,operationName,params,signature);
    }

    /**
     * Returns the default domain used for naming the managed object.
     * The default domain name is used as the domain part in the ObjectName
     * of managed objects if no domain is specified by the user.
     */
    public String getDefaultDomain() throws java.io.IOException, RemoteException {
        return this.mejbHelper.getDefaultDomain();
    }

    /*
     * returns the ListenerRegistry implementation for this MEJB
     */

    public ListenerRegistration getListenerRegistry() throws RemoteException {
        return this.mejbHelper.getListenerRegistry();
    }

}
