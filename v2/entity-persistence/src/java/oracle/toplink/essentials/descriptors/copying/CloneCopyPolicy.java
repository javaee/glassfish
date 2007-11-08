/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.descriptors.copying;

import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.ObjectBuildingQuery;
import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.descriptors.ObjectBuilder;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedMethodInvoker;

/**
 * <p><b>Purpose</b>: Creates a clone through a clone method.
 */
public class CloneCopyPolicy extends AbstractCopyPolicy {

    /** Allow for clone method to be specified. */
    protected String methodName;
    protected String workingCopyMethodName;
    protected transient Method method;
    protected transient Method workingCopyMethod;

    public CloneCopyPolicy() {
        super();
    }

    /**
     * Clone through calling the clone method.
     */
    public Object buildClone(Object domainObject, Session session) throws DescriptorException {
        // Must allow for null clone method for 9.0.4 deployment XML.
        if (this.getMethodName() == null) {
            return getDescriptor().getObjectBuilder().buildNewInstance();
        }
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return AccessController.doPrivileged(new PrivilegedMethodInvoker(this.getMethod(), domainObject, new Object[0]));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw DescriptorException.illegalAccessWhileCloning(domainObject, this.getMethodName(), this.getDescriptor(), throwableException);
                    } else {
                        throw DescriptorException.targetInvocationWhileCloning(domainObject, this.getMethodName(), this.getDescriptor(), throwableException);

                    }
                }
            } else {
                return PrivilegedAccessHelper.invokeMethod(this.getMethod(), domainObject, new Object[0]);
            }
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileCloning(domainObject, this.getMethodName(), this.getDescriptor(), exception);
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileCloning(domainObject, this.getMethodName(), this.getDescriptor(), exception);
        }
    }

    /**
     * Clone through the workingCopyClone method, or if not specified the clone method.
     */
    public Object buildWorkingCopyClone(Object domainObject, Session session) throws DescriptorException {
        if (this.getWorkingCopyMethodName() == null) {
            //not implemented to perform special operations.
            return this.buildClone(domainObject, session);
        }

        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return AccessController.doPrivileged(new PrivilegedMethodInvoker(this.getWorkingCopyMethod(), domainObject, new Object[0]));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw DescriptorException.illegalAccessWhileCloning(domainObject, this.getMethodName(), this.getDescriptor(), throwableException);
                    } else {
                        throw DescriptorException.targetInvocationWhileCloning(domainObject, this.getMethodName(), this.getDescriptor(), throwableException);
                    }
                }
            } else {
                return PrivilegedAccessHelper.invokeMethod(this.getWorkingCopyMethod(), domainObject, new Object[0]);
            }
        
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileCloning(domainObject, this.getMethodName(), this.getDescriptor(), exception);
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileCloning(domainObject, this.getMethodName(), this.getDescriptor(), exception);
        }
    }

    /**
     * Create a new instance, unless a workingCopyClone method is specified, then build a new instance and clone it.
     */
    public Object buildWorkingCopyCloneFromRow(Record row, ObjectBuildingQuery query) throws DescriptorException {
        // For now must preserve CMP code which builds heavy clones with a context.
        // Also preserve for clients who use the copy policy.
        ObjectBuilder builder = getDescriptor().getObjectBuilder();
        if (getWorkingCopyMethodName() != null) {
            Object original = builder.buildNewInstance();
            builder.buildAttributesIntoShallowObject(original, (AbstractRecord)row, query);
            return buildWorkingCopyClone(original, query.getSession());
        } else {
            return builder.buildNewInstance();
        }
    }

    /**
     * Return the clone method.
     */
    protected Method getMethod() {
        return method;
    }

    /**
     * Return the clone method name.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Return the workingCopyClone method.
     * This is used to clone within a unit of work.
     */
    protected Method getWorkingCopyMethod() {
        return workingCopyMethod;
    }

    /**
     * Return the workingCopyClone method name.
     * This is used to clone within a unit of work.
     */
    public String getWorkingCopyMethodName() {
        return workingCopyMethodName;
    }

    /**
     * Validate and build the methods.
     */
    public void initialize(Session session) throws DescriptorException {
        try {
            // Must allow for null clone method for 9.0.4 deployment XML.
            if (this.getMethodName() != null) {
                this.setMethod(Helper.getDeclaredMethod(this.getDescriptor().getJavaClass(), this.getMethodName(), new Class[0]));
            }
        } catch (NoSuchMethodException exception) {
            session.getIntegrityChecker().handleError(DescriptorException.noSuchMethodWhileInitializingCopyPolicy(this.getMethodName(), this.getDescriptor(), exception));
        } catch (SecurityException exception) {
            session.getIntegrityChecker().handleError(DescriptorException.securityWhileInitializingCopyPolicy(this.getMethodName(), this.getDescriptor(), exception));
        }
        if (this.getWorkingCopyMethodName() != null) {
            try {
                this.setWorkingCopyMethod(Helper.getDeclaredMethod(this.getDescriptor().getJavaClass(), this.getWorkingCopyMethodName(), new Class[0]));
            } catch (NoSuchMethodException exception) {
                session.getIntegrityChecker().handleError(DescriptorException.noSuchMethodWhileInitializingCopyPolicy(this.getMethodName(), this.getDescriptor(), exception));
            } catch (SecurityException exception) {
                session.getIntegrityChecker().handleError(DescriptorException.securityWhileInitializingCopyPolicy(this.getMethodName(), this.getDescriptor(), exception));
            }
        }
    }

    /**
     * Set the clone method.
     */
    protected void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Set the clone method name.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Set the workingCopyClone method.
     * This is used to clone within a unit of work.
     */
    protected void setWorkingCopyMethod(Method method) {
        this.workingCopyMethod = method;
    }

    /**
     * Set the workingCopyClone method name.
     * This is used to clone within a unit of work.
     */
    public void setWorkingCopyMethodName(String methodName) {
        this.workingCopyMethodName = methodName;
    }

    /**
     * Return false as a shallow clone is returned, not a new instance.
     */
    public boolean buildsNewInstance() {
        return getMethodName() == null;
    }

    public String toString() {
        return Helper.getShortClassName(this) + "(" + this.getMethodName() + ")";
    }
}
