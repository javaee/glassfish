/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.s1asdev.ejb.stubs.ejbapp;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;

import java.util.*;
//import javax.management.*;
//import javax.management.j2ee.*;

public class DummyMEJBBean {} /**
implements SessionBean {

    private SessionContext sc;

    public DummyMEJBBean() {}

    public void ejbCreate() throws RemoteException {
	System.out.println("In DummyMEJBBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public Set queryNames(ObjectName name, QueryExp query) {
        return new HashSet();
    }

    public boolean isRegistered(ObjectName name) {
        return false;
    }

    public Integer getMBeanCount() {

        // Invoking this method from another app will verify that 
        // the context class loader for this app is set appropriately during
        // the invocation.   The javax.management.j2ee apis are packaged
        // as part of the server's classpath.  That matches the behavior of
        // Bug 6342495, which showed that the context class loader was still
        // set to the calling application when the Home/Remote interfaces were
        // loaded from above the application classloader level.  
        //
        // Calling getClassLoader tests that the context classloader is set
        // appropriately because of the security requirements that J2SE
        // imposes on requesting a class loader.  By default application code
        // in the appserver does not have the RuntimePermission 
        // "getClassLoader".   In that case, requesting a class loader is only
        // allowed if the returned class loader matches the requesting 
        // class' class loader(or is a child of it).  So, if the context 
        // class loader is still incorrectly set to the calling app's 
        // class loader, the call to getContextClassLoader() should throw
        // a security exception.   

        // The advantage to writing the test using
        // javax.management APIs and this behavior is it will work on an
        // out-of-the-box installation of the appserver.  No additions to the
        // server's classpath or the default server.policy file are needed.
        

        System.out.println("In DummyEJBBean::getMBeanCount()");

        System.out.println("My classloader = " + 
                           DummyMEJBBean.class.getClassLoader());

        // This should fail with a security exception if the caller is in
        // a separate app within the same server and the context class
        // loader is still incorrectly set to the caller app's classloader.
        System.out.println("context class loader = " +
                           Thread.currentThread().getContextClassLoader());
        

        return new Integer(0);
    }


    public MBeanInfo getMBeanInfo(ObjectName name) throws IntrospectionException, InstanceNotFoundException, ReflectionException {
        return null;
    }

    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        return null;
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException {
        return null;
    }

    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        return;
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
        return null;
    }

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws  InstanceNotFoundException, MBeanException, ReflectionException {
        return null;
    }

    public String getDefaultDomain() {
        return null;
    }

    public ListenerRegistration getListenerRegistry() {
        return null;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
			      */
