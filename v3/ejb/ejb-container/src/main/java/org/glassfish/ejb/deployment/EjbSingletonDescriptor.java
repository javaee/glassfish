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
import java.util.Iterator;

import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.util.EjbVisitor;

import com.sun.enterprise.util.LocalStringManagerImpl;

import javax.ejb.LockType;

/**
 * @author Mahesh Kannan
 */
public class EjbSingletonDescriptor
        extends EjbSessionDescriptor {

    private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(EjbSingletonDescriptor.class);

    private static final String[] _emptyDepends = new String[] {};

    private boolean startupFlag;

    private String[] depends = _emptyDepends;

    private Class singletonClass;

    private boolean isCMC = true;

    private String cmcInXML;

    private LockType defaultLockType = LockType.WRITE;

    private HashMap<MethodDescriptor, LockType> methodContainerLocks = null;

    public EjbSingletonDescriptor() {
        super();
    }

    public EjbSingletonDescriptor(EjbDescriptor ejbDesc) {
        super(ejbDesc);
    }

    public void addEjbDescriptor(EjbSingletonDescriptor ejbDesc) {
        super.addEjbDescriptor((EjbDescriptor)ejbDesc);
        this.methodContainerLocks = 
             new HashMap<MethodDescriptor, LockType>(ejbDesc.getMethodContainerLocks());
    }

    public boolean isSingleton() {
        return true;
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

    /**
     * Sets the default lock type for the given bean.
     * Throws an Illegal argument if this ejb has ConcurrencyManagementType.BEAN
     */
    public void setDefaultLockType(LockType type) {
        checkLockTypeAllowed();
        System.out.println("@@@@SETTING DEFAULT TO: " + type);
        this.defaultLockType = type;
    }

    public LockType getDefaultLockType() {
        return defaultLockType;
    }

    /**
     * Sets the lock type for the given method descriptor.
     * Throws an Illegal argument if this ejb has ConcurrencyManagementType.BEAN
     */
    public void setCMCLockFor(MethodDescriptor methodDescriptor, LockType lockType) {
        LockType oldValue = getCMCLockFor(methodDescriptor);
        if (oldValue == null || (oldValue != null && !(oldValue.equals(lockType)))) {
            checkLockTypeAllowed();
            System.out.println("@@@@put " + methodDescriptor + " " + lockType);
            //_logger.log(Level.FINE,"put " + methodDescriptor + " " + lockType);
            getMethodContainerLocks().put(methodDescriptor, lockType);
        }
    }

    /**
     * Fetches the assigned lock type object for the given method object or null.
     */
    public LockType getCMCLockFor(MethodDescriptor methodDescriptor) {
        return  getMethodContainerLocks().get(methodDescriptor);
    }

    /**
     * Returns a formatted String of the attributes of this object.
     */
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n ContainerManagedConcurrency ").append(isContainerManagedConcurrency());
        if (isContainerManagedConcurrency()) {
            toStringBuffer.append("\n defaultLockType ").append(defaultLockType);
        }
        toStringBuffer.append("\n methodContainerLocks ").append(getMethodContainerLocks());
    }

    /**
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     *
     * @param aVisitor a visitor to traverse the descriptors
     */
    public void visit(EjbVisitor aVisitor) {
        super.visit(aVisitor);
        for (Iterator e = getMethodContainerLocks().keySet().iterator(); e.hasNext();) {
            MethodDescriptor md = (MethodDescriptor) e.next();
            LockType lt = getMethodContainerLocks().get(md);
            // XXX ??? aVisitor.accept(md, lt);
        }
    }

    private void removeCMCLockFor(MethodDescriptor methodDescriptor) {
        getMethodContainerTransactions().remove(methodDescriptor);
    }

    /**
     * Return a copy of the mapping held internally of method descriptors 
     * to LockType objects.
     */
    private HashMap<MethodDescriptor, LockType> getMethodContainerLocks() {
        if (this.methodContainerLocks == null) {
            this.methodContainerLocks = new HashMap<MethodDescriptor, LockType>();
        }
        return methodContainerLocks;
    }

    private void checkLockTypeAllowed() {
        if (isCMC == false) {
            throw new IllegalArgumentException(localStrings.getLocalString(
                    "enterprise.deployment.exceptionlocktypespecifiedinbeanwithbeanlocktype",
                    "Lock attributes may not be specified on a bean with nean managed lock type" 
                    ));
        }
    }
}
