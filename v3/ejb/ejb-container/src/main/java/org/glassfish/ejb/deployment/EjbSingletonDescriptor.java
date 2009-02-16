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

package org.glassfish.ejb.deployment;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Method;

import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.util.EjbVisitor;

import com.sun.enterprise.util.LocalStringManagerImpl;


/**
 * @author Mahesh Kannan
 */
public class EjbSingletonDescriptor
        extends EjbSessionDescriptor {

    private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(EjbSingletonDescriptor.class);

    private List<MethodDescriptor> readLockMethods = new ArrayList<MethodDescriptor>();
    private List<MethodDescriptor> writeLockMethods = new ArrayList<MethodDescriptor>();
    private List<AccessTimeoutHolder> accessTimeoutMethods =
            new ArrayList<AccessTimeoutHolder>();

    private static final String[] _emptyDepends = new String[] {};

    private boolean startupFlag;

    private String[] depends = _emptyDepends;

    private Class singletonClass;

    private boolean isCMC = true;

    private String cmcInXML;


    public EjbSingletonDescriptor() {
        super();
    }

    public void addEjbDescriptor(EjbSingletonDescriptor ejbDesc) {
        super.addEjbDescriptor((EjbDescriptor)ejbDesc);
    }

    @Override
    public String getSessionType() {
	    return SINGLETON;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public boolean isStateless() {
	    return false;
    }

    @Override
    public boolean isStateful() {
        return false;
    }

    public boolean isStartup() {
        return startupFlag;
    }

    public void setStartup() {
        startupFlag = true;
    }

    public String[] getDepends() {
        return depends;
    }

    public void setDepends(String[] dep) {
        this.depends = dep;
    }

    public Class getSingletonClass() {
        return singletonClass;
    }

    public void setSingletonClass(Class singletonClass) {
        this.singletonClass = singletonClass;
    }

    public boolean isContainerManagedConcurrency() {
        return isCMC;
    }

    public void setContainerManagedConcurrency(boolean value) {
        this.isCMC = value;
    }

    public String getCMCInXML() {
        return cmcInXML;
    }

    public void setCMCInXML(String value) {
        cmcInXML = value;
    }

    @Override
    public Set getTxBusinessMethodDescriptors() {
        Set txBusinessMethodDescs = super.getTxBusinessMethodDescriptors();

        /**
         *  TODO Need to revisit his.  For some reason there's a problem with
         *  handling the method descriptor during transaction attribute
         *  processing of @PostConstruct/@PreDestroy.  CMT Singletons with
         *  TX_REQUIRED/REQUIRES_NEW will work fine.  Only TX_NOT_SUPPORTED
         *  will not work yet. 
         *
        // Add Singleton PostConstruct and PreDestroy methods 
        for(LifecycleCallbackDescriptor lcd : getPostConstructDescriptors()) {
            if( lcd.getLifecycleCallbackClass().equals(getEjbClassName())) {
               String methodName = lcd.getLifecycleCallbackMethod();
               MethodDescriptor methodDesc = new MethodDescriptor
                       (methodName, "postConstructMethod", MethodDescriptor.EJB_BEAN);
               // Class name is needed for method resolution
               methodDesc.setClassName(getEjbClassName());
               txBusinessMethodDescs.add(methodDesc);
               break;
            }
        }
        for(LifecycleCallbackDescriptor lcd : getPreDestroyDescriptors()) {
            if( lcd.getLifecycleCallbackClass().equals(getEjbClassName())) {
               String methodName = lcd.getLifecycleCallbackMethod();
               MethodDescriptor methodDesc = new MethodDescriptor
                       (methodName, "ptrDestroyMethod", MethodDescriptor.EJB_BEAN);
               methodDesc.setClassName(getEjbClassName());
               txBusinessMethodDescs.add(methodDesc);
               break;
            }
        }
        **/

        return txBusinessMethodDescs;
    }

    public void addReadLockMethod(MethodDescriptor methodDescriptor) {
        readLockMethods.add(methodDescriptor);
    }

    public void addWriteLockMethod(MethodDescriptor methodDescriptor) {
        writeLockMethods.add(methodDescriptor);
    }

    public List<MethodDescriptor> getReadLockMethods() {
        return new ArrayList<MethodDescriptor>(readLockMethods);
    }

    public List<MethodDescriptor> getWriteLockMethods() {
        return new ArrayList<MethodDescriptor>(writeLockMethods);
    }

    public List<MethodDescriptor> getReadAndWriteLockMethods() {
        List<MethodDescriptor> readAndWriteLockMethods = new ArrayList<MethodDescriptor>();
        readAndWriteLockMethods.addAll(readLockMethods);
        readAndWriteLockMethods.addAll(writeLockMethods);
        return readAndWriteLockMethods;
    }

    public void addAccessTimeoutMethod(MethodDescriptor methodDescriptor, long value,
                                       TimeUnit unit) {
        accessTimeoutMethods.add(new AccessTimeoutHolder(value, unit, methodDescriptor));
    }

    public List<MethodDescriptor> getAccessTimeoutMethods() {
        List<MethodDescriptor> methods = new ArrayList<MethodDescriptor>();
        for(AccessTimeoutHolder holder : accessTimeoutMethods){
            methods.add(holder.method);
        }
        return methods;
    }

    public List<AccessTimeoutHolder> getAccessTimeoutInfo() {
        List<AccessTimeoutHolder> all = new ArrayList<AccessTimeoutHolder>();
        for(AccessTimeoutHolder holder : accessTimeoutMethods){
            all.add(holder);
        }
        return all;
    }

    /**
     * Returns a formatted String of the attributes of this object.
     */
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n ContainerManagedConcurrency ").append(isContainerManagedConcurrency());
    }

    /**
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     *
     * @param aVisitor a visitor to traverse the descriptors
     */
    public void visit(EjbVisitor aVisitor) {
        super.visit(aVisitor);
    }

    public static class AccessTimeoutHolder {
        public AccessTimeoutHolder(long v, TimeUnit u, MethodDescriptor m) {
            value = v;
            unit = u;
            method = m;
        }
        public long value;
        public TimeUnit unit;
        public MethodDescriptor method;
    }

}
