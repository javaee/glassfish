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
package oracle.toplink.essentials.descriptors;

import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.sessions.Record;
import oracle.toplink.essentials.internal.security.*;

/**
 * <p><b>Purpose</b>:
 * Used to allow complex inheritance support.  Typically class indicators are used to define inheritance in the database,
 * however in complex cases the class type may be determined through another mechanism.
 * The method calls a static method on the descriptor class to determine the class to use for the database row.
 *
 * @see oracle.toplink.essentials.descriptors.InheritancePolicy#setClassExtractor(ClassExtrator)
 */
public class MethodClassExtractor extends ClassExtractor {
    protected transient ClassDescriptor descriptor;
    protected String classExtractionMethodName;
    protected transient Method classExtractionMethod;

    /**
     * INTERNAL:
     * Return all the classExtractionMethod
     */
    public Method getClassExtractionMethod() {
        return classExtractionMethod;
    }

    /**
     * PUBLIC:
     * A class extraction method can be registered with the descriptor to override the default inheritance mechanism.
     * This allows for the class indicator field to not be used, and a user defined one instead.
     * The method registered must be a static method on the class that the descriptor is for,
     * the method must take DatabaseRow as argument, and must return the class to use for that row.
     * This method will be used to decide which class to instantiate when reading from the database.
     * It is the application's responsiblity to populate any typing information in the database required
     * to determine the class from the row.
     * If this method is used then the class indicator field and mapping cannot be used,
     * also the descriptor's withAllSubclasses and onlyInstances expressions must also be setup correctly.
     */
    public String getClassExtractionMethodName() {
        return classExtractionMethodName;
    }

    /**
     * INTERNAL:
     */
    protected void setClassExtractionMethod(Method classExtractionMethod) {
        this.classExtractionMethod = classExtractionMethod;
    }

    /**
     * PUBLIC:
     * A class extraction method can be registered with the descriptor to override the default inheritance mechanism.
     * This allows for the class indicator field to not be used, and a user defined one instead.
     * The method registered must be a static method on the class that the descriptor is for,
     * the method must take DatabaseRow as argument, and must return the class to use for that row.
     * This method will be used to decide which class to instantiate when reading from the database.
     * It is the application's responsiblity to populate any typing information in the database required
     * to determine the class from the row.
     * If this method is used then the class indicator field and mapping cannot be used,
     * also the descriptor's withAllSubclasses and onlyInstances expressions must also be setup correctly.
     */
    public void setClassExtractionMethodName(String staticClassClassExtractionMethod) {
        this.classExtractionMethodName = staticClassClassExtractionMethod;
    }

    /**
     * INTERNAL:
     * Setup the default classExtractionMethod, or if one was specified by the user make sure it is valid.
     */
    public void initialize(ClassDescriptor descriptor, Session session) throws DescriptorException {
        setDescriptor(descriptor);
        Class[] declarationParameters = new Class[1];
        declarationParameters[0] = ClassConstants.DatabaseRow_Class;

        try {
            setClassExtractionMethod(Helper.getDeclaredMethod(descriptor.getJavaClass(), getClassExtractionMethodName(), declarationParameters));
        } catch (NoSuchMethodException ignore) {
            declarationParameters[0] = ClassConstants.Record_Class;
            try {
                setClassExtractionMethod(Helper.getDeclaredMethod(descriptor.getJavaClass(), getClassExtractionMethodName(), declarationParameters));
            } catch (NoSuchMethodException exception) {
                throw DescriptorException.noSuchMethodWhileInitializingClassExtractionMethod(getClassExtractionMethodName(), descriptor, exception);
            }
        } catch (SecurityException exception) {
            throw DescriptorException.securityWhileInitializingClassExtractionMethod(getClassExtractionMethodName(), descriptor, exception);
        }

        // CR#2818667 Ensure the method is static.
        if (!Modifier.isStatic(getClassExtractionMethod().getModifiers())) {
            throw DescriptorException.classExtractionMethodMustBeStatic(getClassExtractionMethodName(), descriptor);
        }
    }

    /**
     * INTERNAL
     * Extract/compute the class from the database row and return the class.
     * Map is used as the public interface to database row, the key is the field name,
     * the value is the database value.
     */
    public Class extractClassFromRow(Record row, oracle.toplink.essentials.sessions.Session session) {
        Class classForRow;

        try {
            Object[] arguments = new Object[1];
            arguments[0] = row;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    classForRow = (Class)AccessController.doPrivileged(new PrivilegedMethodInvoker(getClassExtractionMethod(), null, arguments));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw DescriptorException.illegalAccessWhileInvokingRowExtractionMethod((AbstractRecord)row, getClassExtractionMethod(), getDescriptor(), throwableException);
                    } else {
                        throw DescriptorException.targetInvocationWhileInvokingRowExtractionMethod((AbstractRecord)row, getClassExtractionMethod(), getDescriptor(), throwableException);
                    }
                }
            } else {
                classForRow = (Class)PrivilegedAccessHelper.invokeMethod(getClassExtractionMethod(), null, arguments);
            }
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileInvokingRowExtractionMethod((AbstractRecord)row, getClassExtractionMethod(), getDescriptor(), exception);
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileInvokingRowExtractionMethod((AbstractRecord)row, getClassExtractionMethod(), getDescriptor(), exception);
        }

        return classForRow;
    }

    /**
     * INTERNAL:
     * Return the descriptor.
     */
    protected ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * INTERNAL:
     * Set the descriptor.
     */
    protected void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}
