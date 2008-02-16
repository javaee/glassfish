

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
package org.apache.jasper.compiler;


import java.util.Vector;
import java.util.Hashtable;

import org.apache.jasper.JasperException;

/**
 * Repository of {page, request, session, application}-scoped beans 
 *
 * @author Mandar Raje
 */
class BeanRepository {

    private Vector sessionBeans;
    private Vector pageBeans;
    private Vector appBeans;
    private Vector requestBeans;
    private Hashtable beanTypes;
    private ClassLoader loader;
    private ErrorDispatcher errDispatcher;

    /*
     * Constructor.
     */    
    public BeanRepository(ClassLoader loader, ErrorDispatcher err) {

        this.loader = loader;
	this.errDispatcher = err;

	sessionBeans = new Vector(11);
	pageBeans = new Vector(11);
	appBeans = new Vector(11);
	requestBeans = new Vector(11);
	beanTypes = new Hashtable();
    }
        
    public void addBean(Node.UseBean n, String s, String type, String scope)
	    throws JasperException {

	if (scope == null || scope.equals("page")) {
	    pageBeans.addElement(s);	
	} else if (scope.equals("request")) {
	    requestBeans.addElement(s);
	} else if (scope.equals("session")) {
	    sessionBeans.addElement(s);
	} else if (scope.equals("application")) {
	    appBeans.addElement(s);
	} else {
	    errDispatcher.jspError(n, "jsp.error.invalid.scope", scope);
	}
	
	putBeanType(s, type);
    }
            
    public Class getBeanType(String bean) throws JasperException {
	Class clazz = null;
	try {
	    clazz = loader.loadClass ((String)beanTypes.get(bean));
	} catch (ClassNotFoundException ex) {
	    throw new JasperException (ex);
	}
	return clazz;
    }
      
    public boolean checkVariable (String bean) {
	// XXX Not sure if this is the correct way.
	// After pageContext is finalised this will change.
	return (checkPageBean(bean) || checkSessionBean(bean) ||
		checkRequestBean(bean) || checkApplicationBean(bean));
    }


    private void putBeanType(String bean, String type) {
	beanTypes.put (bean, type);
    }

    private boolean checkPageBean (String s) {
	return pageBeans.contains (s);
    }

    private boolean checkRequestBean (String s) {
	return requestBeans.contains (s);
    }

    private boolean checkSessionBean (String s) {
	return sessionBeans.contains (s);
    }

    private boolean checkApplicationBean (String s) {
	return appBeans.contains (s);
    }

}




