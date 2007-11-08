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

package com.sun.appserv.server.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.net.URL;
import java.security.SecureClassLoader;

/**
 * Represents a classloader chain. A ClassLoader chain is a linked list of
 * classloaders. All class requests is searched across the classloader chain in
 * a horizontal manner. A classloader in this chain can either exist within
 * the scope of this chain only or could be shared across multiple chains. 
 *   
 * @author Harsha RA, Sivakumar Thyagarajan
 */
public class ClassLoaderChain extends SecureClassLoader {
    private List<ClassLoader> classLoaderList = new ArrayList<ClassLoader>();
    private ClassLoader parentCL = null;
    private String nameOfCL;
    
    public ClassLoaderChain(){
        super();
    }
    
    public ClassLoaderChain(ClassLoader parent){
        super(parent);
        this.parentCL = parent;
    }
    
    public void setName(String n) {
        this.nameOfCL = n;
    }

    public String getName() {
        return this.nameOfCL;
    }


    public void addToList(ClassLoader cl) {
        this.classLoaderList.add(cl);
    }

    /**
      * Mechanism to remove a classloader from a chain. This is especially 
      * useful in cases like uninstall-addon where a classloader has to be 
      * removed from a ClassLoaderChain dynamically at runtime
      */
    public void removeFromList(ClassLoader cl) {
    	this.classLoaderList.remove(cl);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }
    
    /**
     * Special loadClass method that attempts to a load a class in the chain,
     * but skips the classLoader mentioned as "toSkip" to prevent infinite
     * recursion, while going through the chain.
     * @param name The binary name of the class
     * @param toSkip ClassLoader component in the chain to skip while attempting
     * to load a class.
     * @return The resulting <code>Class</code> object
     * @throws ClassNotFoundException If the class was not found in the other 
     * components of the chain.
     */
    protected Class loadClass(String name, ClassLoader toSkip) 
                                        throws ClassNotFoundException {
        //Iterate through all components in the chain.
        for (Iterator<ClassLoader> iter = classLoaderList.iterator(); 
                                                        iter.hasNext();) {
            ClassLoader element = iter.next();
            
            if(element.equals(toSkip)) { 
                continue;
            }
            
            Class clz = null;
            try {
                //Attempt to load the class from a component in the chain.
                clz = element.loadClass(name);
                if (clz != null) {
                    return clz;
                }                
            } catch (ClassNotFoundException e) {
                //Ignore if a component in the chain fails to load the class.
                //e.printStackTrace();
            }
        }
        //All components in the chain failed to find the class.
        throw new ClassNotFoundException(name);
    }
    

    /** 
     * Loads the class with the specified binary name. 
     * The traditional loadClass has been overridden to check if component
     * classloaders in the chain could find the class.
     */
    @Override
    public Class<?> loadClass(String className,boolean resolve) 
                                        throws ClassNotFoundException {
        //System.out.println(className + " attempted in Chain" + name );
        Class c = findLoadedClass(className);
        if( c != null ) {
            return c;
        }
        //XXX: What happens if parentCL is a chain. - now we have addonchains as the parent
        //of AS chain
//        if( parentCL instanceof ClassLoaderChain ) {
//            //warning message
//            throw new RuntimeException("ClassLoader " + this.toString() 
//                        + " parent is a chain " + parentCL.toString() );
//        }
        if (parentCL != null) {
            try {
                //Delegate to parent of chain first.!
                c = parentCL.loadClass(className);
                if (c != null) {
                    if (resolve) {
                        resolveClass(c);
                    }
                }
            } catch(ClassNotFoundException e) {
                //ignore
            }
            if(c != null) {
                //System.out.println("parent of " + this + "loaded " + className);
                return c;
            }
        }

        //Traverse through all classloaders in the chain to find class
        for (ClassLoader element : classLoaderList) {
            if(element == null)
                continue;
            
            Class clz = null;
            try {
                clz = element.loadClass(className);
                if (clz != null) {
                    if(resolve) {
                        resolveClass(clz);
                    }
                    return clz;
                }                
                //System.out.println(className + "was loaded by"+clz.getClassLoader());
            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
            }
        }
        //All components in the chain failed to find the class.
        throw new ClassNotFoundException(className);
    }
    
    @Override
    protected URL findResource(String name) {
        for(ClassLoader cl:classLoaderList) {
            URL res = cl.getResource(name);
            if (res != null) return res;
        }
        return null;
    }
    
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        for(ClassLoader cl:classLoaderList) {
            Enumeration<URL> res = cl.getResources(name);
            List<URL> al = new ArrayList<URL>();
            while(res.hasMoreElements()) {
                al.add(res.nextElement());
            }
            if (al.size() != 0) {
                return ((new java.util.Vector<URL>(al)).elements());
            }
        }
        return ((new java.util.Vector<URL>()).elements());
    }
    
    @Override
    public String toString() {
        String s = this.nameOfCL + " parentCL :: " + this.parentCL + 
                                                " constituent CLs :: \n";
        for(ClassLoader cl:this.classLoaderList) {
            String nameofCL1 = null;
            if (cl instanceof ASURLClassLoader) {
                //As of now limiting this to just get the name.
                nameofCL1 = ((ASURLClassLoader)cl).getName();
            } else if (cl instanceof ClassLoaderChain) {
                nameofCL1 = ((ClassLoaderChain)cl).getName();
            }
            s +=  " :: " + nameofCL1;
        }
        s += "\n";
        return s;
    }
}


