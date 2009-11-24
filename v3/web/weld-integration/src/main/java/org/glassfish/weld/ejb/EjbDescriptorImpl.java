/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.weld.ejb;

import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;

import javax.ejb.Local;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.EjbRemovalInfo;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;

import java.lang.reflect.Method;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 */
public class EjbDescriptorImpl<T> implements org.jboss.weld.ejb.spi.EjbDescriptor<T>
{

    private EjbDescriptor ejbDesc;

    public EjbDescriptorImpl(EjbDescriptor e) {
        ejbDesc = e;
    }


    public String getEjbName() {
        return ejbDesc.getName();
    }

    public EjbDescriptor getEjbDescriptor() {
        return ejbDesc;
    }

    public Class<T> getBeanClass() {
        Class beanClassType = null;
	    try {

            beanClassType =
              ejbDesc.getEjbBundleDescriptor().getClassLoader().loadClass(ejbDesc.getEjbClassName());

        } catch(ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        return beanClassType;
    }

   /**
    * Gets the local business interfaces of the EJB
    * 
    * @return An iterator over the local business interfaces
    */
    public Collection<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces() {
       
        Set<BusinessInterfaceDescriptor<?>> localBusIntfs = new HashSet<BusinessInterfaceDescriptor<?>>();

        if( ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) ) {

            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;
            Set<String> localNames = sessionDesc.getLocalBusinessClassNames();

            // Add superinterfaces that are also marked as Local
            Set<String> extraNames = new HashSet<String>();
            for(String local : localNames) {
                try {
                    Class localClass = sessionDesc.getEjbBundleDescriptor().getClassLoader().loadClass(local);
                    addIfLocal(localClass.getInterfaces(), extraNames);
                } catch(ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }

            localNames.addAll(extraNames);

            // Include the no-interface Local view
            if( sessionDesc.isLocalBean() ) {
                localNames.add(sessionDesc.getEjbClassName());
            }


            for(String local : localNames) {
                try {

                    Class localClass = sessionDesc.getEjbBundleDescriptor().getClassLoader().loadClass(local);
                    BusinessInterfaceDescriptor busIntfDesc =
                            new BusinessInterfaceDescriptorImpl(localClass);
                    localBusIntfs.add(busIntfDesc);

                } catch(ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
       
        return localBusIntfs;
    }
   
   /**
    * Get the remove methods of the EJB
    * 
    * @return An iterator over the remove methods
    */
    public Collection<Method> getRemoveMethods() {
        Set<Method> removeMethods = new HashSet<Method>();

        if( ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) ) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;
            if( sessionDesc.isStateful() && sessionDesc.hasRemoveMethods() ) {

                for(EjbRemovalInfo next : sessionDesc.getAllRemovalInfo()) {

                    MethodDescriptor mDesc = next.getRemoveMethod();
                    Method m = mDesc.getMethod(ejbDesc);
                    if( m == null ) {
                        throw new IllegalStateException("Can't resolve remove method " +
                        mDesc + " For EJB " + sessionDesc.getName());
                    }
                    removeMethods.add(m);

                }

            }
        }

       return removeMethods;

    }

   /**
    * Indicates if the bean is stateless
    * 
    * @return True if stateless, false otherwise
    */
    public boolean isStateless() {
	    return (ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) &&
               ((EjbSessionDescriptor) ejbDesc).isStateless());
    }

   /**
    * Indicates if the bean is a EJB 3.1 Singleton
    * 
    * @return True if the bean is a singleton, false otherwise
    */
    public boolean isSingleton() {
       return (ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) &&
               ((EjbSessionDescriptor) ejbDesc).isSingleton());
    }

   /**
    * Indicates if the EJB is stateful
    * 
    * @return True if the bean is stateful, false otherwise
    */
    public boolean isStateful() {
        return (ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) &&
               ((EjbSessionDescriptor) ejbDesc).isStateful());
    }

   /**
    * Indicates if the EJB is and MDB
    * 
    * @return True if the bean is an MDB, false otherwise
    */
    public boolean isMessageDriven() {
	    return (ejbDesc.getType().equals(EjbMessageBeanDescriptor.TYPE));
    }

    /*  enabled for debugging 
    public int hashCode() {
        return getEjbName().hashCode();
    }

    public boolean equals(Object o) {

        boolean equal = false;

        if( (o != null) && (o instanceof EjbDescriptorImpl) ) {

            equal = getEjbName().equals( ((EjbDescriptorImpl)o).getEjbName() );

        }

        return equal;

    }
    */

    private void addIfLocal(Class[] interfaces, Set<String> names) {
        for(Class next : interfaces) {
            if( next.getAnnotation(Local.class) != null ) {
                names.add(next.getName());
            }
            addIfLocal(next.getInterfaces(), names);
        }
    }
}
