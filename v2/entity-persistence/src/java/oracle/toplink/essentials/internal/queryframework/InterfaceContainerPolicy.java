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
package oracle.toplink.essentials.internal.queryframework;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.lang.reflect.*;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;
import oracle.toplink.essentials.internal.security.PrivilegedGetMethod;

/**
 * <p><b>Purpose</b>: The abstract class for ContainerPolicy's whose container class implements
 * a container interface.
 * <p>
 *
 * @see CollectionContainerPolicy
 * @see MapContainerPolicy
 */
public abstract class InterfaceContainerPolicy extends ContainerPolicy {

    /** The concrete container class. */
    protected Class containerClass;
    protected String containerClassName;

    /** The method which will return a clone of an instance of the containerClass. */
    protected transient Method cloneMethod;

    /**
     * INTERNAL:
     * Construct a new policy.
     */
    public InterfaceContainerPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class.
     */
    public InterfaceContainerPolicy(Class containerClass) {
        setContainerClass(containerClass);
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class name.
     */
    public InterfaceContainerPolicy(String containerClassName) {
        setContainerClassName(containerClassName);
    }

    /**
     * INTERNAL:
     * Return a clone of the specified container.
     */
    public Object cloneFor(Object container) {
        if (container == null) {
            return null;
        }

        try {
            return invokeCloneMethodOn(getCloneMethod(), container);
        } catch (IllegalArgumentException ex) {
            // container may be a superclass of the concrete container class
            // so we have to use the right clone method...
            return invokeCloneMethodOn(getCloneMethod(container.getClass()), container);
        }
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this ContainerPolicy to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);
        if (containerClassName == null){
            return;
        }
        Class containerClass = null;
        try{
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    containerClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(containerClassName, true, classLoader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.classNotFoundWhileConvertingClassNames(containerClassName, exception.getException());
                }
            } else {
                containerClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(containerClassName, true, classLoader);
            }
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(containerClassName, exc);
        }
        setContainerClass(containerClass);
    };

    /**
     * INTERNAL:
     * Return the 'clone()' Method for the container class.
     * Lazy initialization is used, so we can serialize these things.
     */
    public Method getCloneMethod() {
        if (cloneMethod == null) {
            setCloneMethod(getCloneMethod(getContainerClass()));
        }
        return cloneMethod;
    }

    /**
     * INTERNAL:
     * Return the 'clone()' Method for the specified class.
     * Return null if the method does not exist anywhere in the hierarchy
     */
    protected Method getCloneMethod(Class javaClass) {
        try {
            // This must not be set "accessible" - clone() must be public, and some JVM's do not allow access to JDK classes.
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return (Method)AccessController.doPrivileged(new PrivilegedGetMethod(javaClass, "clone", (Class[])null, false));
                } catch (PrivilegedActionException exception) {
                    throw QueryException.methodDoesNotExistInContainerClass("clone", javaClass);
                }
            } else {
                return PrivilegedAccessHelper.getMethod(javaClass, "clone", (Class[])null, false);
            }
        } catch (NoSuchMethodException ex) {
            throw QueryException.methodDoesNotExistInContainerClass("clone", javaClass);
        }
    }

    /**
     * INTERNAL:
     * Returns the container class to be used with this policy.
     */
    public Class getContainerClass() {
        return containerClass;
    }

    public String getContainerClassName() {
        if ((containerClassName == null) && (containerClass != null)) {
            containerClassName = containerClass.getName();
        }
        return containerClassName;
    }

    public abstract Class getInterfaceType();

    /**
     * INTERNAL:
     * Return whether the iterator has more objects,
     */
    public boolean hasNext(Object iterator) {
        return ((Iterator)iterator).hasNext();
    }

    /**
     * INTERNAL:
     * Invoke the specified clone method on the container,
     * handling the necessary exceptions.
     */
    protected Object invokeCloneMethodOn(Method method, Object container) {
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return AccessController.doPrivileged(new PrivilegedMethodInvoker(method, container, (Object[])null));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw QueryException.cannotAccessMethodOnObject(method, container);
                    } else {
                        throw QueryException.methodInvocationFailed(method, container, throwableException);
                    }
                }
            } else {
                return PrivilegedAccessHelper.invokeMethod(method, container, (Object[])null);
            }
        } catch (IllegalAccessException ex1) {
            throw QueryException.cannotAccessMethodOnObject(method, container);
        } catch (InvocationTargetException ex2) {
            throw QueryException.methodInvocationFailed(method, container, ex2);
        }
    }

    /**
     * INTERNAL:
     * Validate the container type.
     */
    public boolean isValidContainerType(Class containerType) {
        return oracle.toplink.essentials.internal.helper.Helper.classImplementsInterface(containerType, getInterfaceType());
    }

    /**
     * INTERNAL:
     * Return the next object on the queue.
     * Valid for some subclasses only.
     */
    protected Object next(Object iterator) {
        return ((Iterator)iterator).next();
    }

    /**
     * INTERNAL:
     * Set the Method that will return a clone of an instance of the containerClass.
     */
    public void setCloneMethod(Method cloneMethod) {
        this.cloneMethod = cloneMethod;
    }

    /**
     * INTERNAL:
     * Set the class to use as the container.
     */
    public void setContainerClass(Class containerClass) {
        this.containerClass = containerClass;
        initializeConstructor();
    }

    public void setContainerClassName(String containerClassName) {
        this.containerClassName = containerClassName;
    }

    protected Object toStringInfo() {
        return this.getContainerClass();
    }
}
