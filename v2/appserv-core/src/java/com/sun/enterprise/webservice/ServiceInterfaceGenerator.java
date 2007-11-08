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

package com.sun.enterprise.webservice;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import java.util.logging.Logger;

import javax.jws.WebMethod;

import com.sun.ejb.codegen.Generator;
import com.sun.ejb.codegen.GeneratorException;

import sun.rmi.rmic.IndentingWriter;

import static java.lang.reflect.Modifier.*;
import static com.sun.corba.ee.spi.codegen.Wrapper.*;
import com.sun.corba.ee.spi.codegen.Type;
import com.sun.corba.ee.impl.codegen.ClassGenerator;
import com.sun.ejb.codegen.ClassGeneratorFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;

/**
 * This class is responsible for generating the SEI when it is not packaged 
 * by the application. 
 *
 * @author Jerome Dochez
 */
public class ServiceInterfaceGenerator extends Generator 
    implements ClassGeneratorFactory {

    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(ServiceInterfaceGenerator.class);
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.DPL_LOGGER);
    }
 
    Class sib=null;
    String serviceIntfName;
    String packageName;
    String serviceIntfSimpleName;
    Method[] intfMethods;
    
   /**
     * Construct the Wrapper generator with the specified deployment
     * descriptor and class loader.
     * @exception GeneratorException.
     */
    public ServiceInterfaceGenerator(ClassLoader cl, Class sib) 
	throws GeneratorException, ClassNotFoundException
    {
	super();

        this.sib = sib;
        serviceIntfSimpleName = getServiceIntfName();

	packageName = getPackageName();
        serviceIntfName = packageName + "." + serviceIntfSimpleName;
	
        intfMethods = calculateMethods(sib, removeDups(sib.getMethods()));
        
        // NOTE : no need to remove ejb object methods because EJBObject
        // is only visible through the RemoteHome view.
    }    
    
    public String getServiceIntfName() {
        String serviceIntfSimpleName = sib.getSimpleName();
        if (serviceIntfSimpleName.endsWith("EJB")) {
            return serviceIntfSimpleName.substring(0, serviceIntfSimpleName.length()-3);
        } else {
            return serviceIntfSimpleName+"SEI";
        }
    }
    
    public String getPackageName() {
        return sib.getPackage().getName()+".internal.jaxws";
    }
    
    /**
     * Get the fully qualified name of the generated class.
     * Note: the remote/local implementation class is in the same package 
     * as the bean class, NOT the remote/local interface.
     * @return the name of the generated class.
     */
    public String getGeneratedClass() {
        return serviceIntfName;
    }

    // For corba codegen infrastructure
    public String className() {
        return getGeneratedClass();
    }
    
    private Method[] calculateMethods(Class sib, Method[] initialList) {

        // we start by assuming the @WebMethod was NOT used on this class
        boolean webMethodAnnotationUsed = false;
        List<Method> list = new ArrayList<Method>();
        
        for (Method m : initialList) {
            WebMethod wm = m.getAnnotation(javax.jws.WebMethod.class);
            if (wm!=null && webMethodAnnotationUsed==false) {
                webMethodAnnotationUsed=true;
                // reset the list, this is the first annotated method we find
                list.clear();
            }
            if (wm!=null) {
                list.add(m);
            } else {
                if (!webMethodAnnotationUsed && !m.getDeclaringClass().equals(java.lang.Object.class)) {
                    list.add(m);
                }
            }
        }
        return list.toArray(new Method[0]);
    }

    public ClassGenerator evaluate() {

        _clear();

	if (packageName != null) {
	    _package(packageName);
        }

        _interface(PUBLIC, serviceIntfName);

        for(int i = 0; i < intfMethods.length; i++) {
	    printMethod(intfMethods[i]);
	}

        _end();

        return _classGenerator() ;

    }



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

	if (packageName != null) {
	    p.pln("package " + packageName + ";");
        }

        p.pln("");

	p.plnI("public interface " + serviceIntfSimpleName + " {");

        p.pln("");

	// each remote method
	for(int i = 0; i < intfMethods.length; i++) {
	    printMethod(p, intfMethods[i]);
	}

	p.pOln("}");
	p.close();
    }

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

        _method( PUBLIC | ABSTRACT, Type.type(m.getReturnType()),
                 m.getName(), exceptionList);

        int i = 0;
        com.sun.corba.ee.spi.codegen.Expression expr = null;
        for(Class param : m.getParameterTypes()) {
            expr = _arg(Type.type(param), "param" + i);
            i++;
	}

        _end();
    }


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
