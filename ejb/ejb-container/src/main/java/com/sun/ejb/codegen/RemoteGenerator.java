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
package com.sun.ejb.codegen;

import java.lang.reflect.Method;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.sun.logging.*;
import com.sun.ejb.EJBUtils;

import static java.lang.reflect.Modifier.*;
/*TODO
import static com.sun.corba.ee.spi.orbutil.codegen.Wrapper.*;
import com.sun.corba.ee.spi.orbutil.codegen.Type;
import com.sun.corba.ee.spi.orbutil.codegen.Expression;
import com.sun.corba.ee.impl.orbutil.codegen.ClassGenerator;
*/

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;
import javax.ejb.EntityBean;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * This class is used to generate the RMI-IIOP version of a 
 * remote business interface.
 */

public class RemoteGenerator extends Generator 
    implements ClassGeneratorFactory {

    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(RemoteGenerator.class);


    private Class businessInterface;
    private Method[] bizMethods;
    private String remoteInterfacePackageName;
    private String remoteInterfaceSimpleName;
    private String remoteInterfaceName;

    /**
     * Get the fully qualified name of the generated class.
     * Note: the remote/local implementation class is in the same package 
     * as the bean class, NOT the remote/local interface.
     * @return the name of the generated class.
     */
    public String getGeneratedClass() {
        return remoteInterfaceName;
    }

    // For corba codegen infrastructure
    public String className() {
        return getGeneratedClass();
    }
    
    /**
     * Construct the Wrapper generator with the specified deployment
     * descriptor and class loader.
     * @exception GeneratorException.
     */
    public RemoteGenerator(ClassLoader cl, String businessIntf) 
	throws GeneratorException 
    {
	super();

	try {
	    businessInterface = cl.loadClass(businessIntf);
	} catch (ClassNotFoundException ex) {
	    throw new InvalidBean(
		localStrings.getLocalString(
		"generator.remote_interface_not_found",
		"Remote interface not found "));
	}

        remoteInterfaceName = EJBUtils.getGeneratedRemoteIntfName
            (businessInterface.getName());

	remoteInterfacePackageName = getPackageName(remoteInterfaceName);
        remoteInterfaceSimpleName = getBaseName(remoteInterfaceName);
	
	bizMethods = removeDups(businessInterface.getMethods());
        
        // NOTE : no need to remove ejb object methods because EJBObject
        // is only visible through the RemoteHome view.
    }

    /*TODO
    public ClassGenerator evaluate() {

        _clear();

	if (remoteInterfacePackageName != null) {
	    _package(remoteInterfacePackageName);
        } else {
            // no-arg _package() call is required for default package
            _package();
        } 

        _interface(PUBLIC, remoteInterfaceSimpleName,
                   _t("java.rmi.Remote"), 
                   _t("com.sun.ejb.containers.RemoteBusinessObject"));

        for(int i = 0; i < bizMethods.length; i++) {
	    printMethod(bizMethods[i]);
	}

        _end();

        return _classGenerator() ;

    }
	*/


    /**
     * Generate the code to the specified output stream.
     * @param the output stream
     * @exception GeneratorException on a generation error
     * @exception IOException on an IO error
     */
    public void generate(OutputStream out)
	throws GeneratorException, IOException 
    {
	IndentingWriter p = new IndentingWriter(new OutputStreamWriter(out));

        p.pln("");

	if (remoteInterfacePackageName != null) {
	    p.pln("package " + remoteInterfacePackageName + ";");
        }

        p.pln("");

	p.plnI("public interface " + remoteInterfaceSimpleName + " extends " +
            "java.rmi.Remote , com.sun.ejb.containers.RemoteBusinessObject {");

        p.pln("");

	// each remote method
	for(int i = 0; i < bizMethods.length; i++) {
	    printMethod(p, bizMethods[i]);
	}

	p.pOln("}");
	p.close();
    }

    /*TODO
    private void printMethod(Method m)
    {

        boolean throwsRemoteException = false;
        List<Type> exceptionList = new LinkedList<Type>();
	for(Class exception : m.getExceptionTypes()) {
            exceptionList.add(Type.type(exception));
            if( exception.getName().equals("java.rmi.RemoteException") ) {
                throwsRemoteException = true;
            }
	}
        if( !throwsRemoteException ) {
            exceptionList.add(_t("java.rmi.RemoteException"));
        }

        exceptionList.add(_t("com.sun.ejb.containers.InternalEJBContainerException"));
        _method( PUBLIC | ABSTRACT, Type.type(m.getReturnType()),
                 m.getName(), exceptionList);

        int i = 0;
        for(Class param : m.getParameterTypes()) {
            _arg(Type.type(param), "param" + i);
            i++;
	}

        _end();
    }
	*/

    /**
     * Generate the code for a single method.
     * @param the writer.
     * @param the method to generate code for.
     * @exception IOException.
     */
    private void printMethod(IndentingWriter p, Method m)
	throws IOException
    {
	p.pln("");

	// print method signature and exceptions
	p.p("public " + printType(m.getReturnType()) + " "
		+ m.getName() + "(");
	Class[] params = m.getParameterTypes();
	for(int i = 0; i < params.length; i++) {
	    if (i != 0)
		p.p(", ");
	    p.p(printType(params[i]) + " param" + i);
	}
	p.p(") ");
	Class[] exceptions = m.getExceptionTypes();
        boolean throwsRemoteException = false;
	for(int i = 0; i < exceptions.length; i++) {
	    if (i == 0)
		p.p("throws ");
	    else
		p.p(", ");
            String nextEx = exceptions[i].getName();
	    p.p(nextEx);
            if( nextEx.equals("java.rmi.RemoteException") ) {
                throwsRemoteException = true;
            }
	}
        if( exceptions.length == 0 ) {
            p.p("throws java.rmi.RemoteException");
        } else if (!throwsRemoteException) {
            p.p(", java.rmi.RemoteException");
        }
	p.pln(";");
        p.pln("");
    }

}
