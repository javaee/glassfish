/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.hk2.tests.locator.negative.classanalysis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.MultiException;

/**
 * @author jwells
 *
 */
@Singleton @Named(ConfigurablyBadClassAnalyzer.BAD_ANALYZER_NAME)
public class ConfigurablyBadClassAnalyzer implements ClassAnalyzer {
    public static final String BAD_ANALYZER_NAME = "BadAnalyzer";
    
    private boolean throwFromConstructor = false;
    private boolean throwFromMethods = false;
    private boolean throwFromFields = false;
    private boolean throwFromPostConstruct = false;
    private boolean throwFromPreDestroy = false;
    
    private boolean nullFromConstructor = false;
    private boolean nullFromMethods = false;
    private boolean nullFromFields = false;
    
    @Inject @Named(ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME)
    private ClassAnalyzer delegate;
    
    public void resetToGood() {
        throwFromConstructor = false;
        throwFromMethods = false;
        throwFromFields = false;
        throwFromPostConstruct = false;
        throwFromPreDestroy = false;
        
        nullFromConstructor = false;
        nullFromMethods = false;
        nullFromFields = false;
    }
    
    public void setThrowFromConstructor(boolean throwFromConstructor) {
        this.throwFromConstructor = throwFromConstructor;
    }

    public void setThrowFromMethods(boolean throwFromMethods) {
        this.throwFromMethods = throwFromMethods;
    }

    public void setThrowFromFields(boolean throwFromFields) {
        this.throwFromFields = throwFromFields;
    }

    public void setThrowFromPostConstruct(boolean throwFromPostConstruct) {
        this.throwFromPostConstruct = throwFromPostConstruct;
    }

    public void setThrowFromPreDestroy(boolean throwFromPreDestroy) {
        this.throwFromPreDestroy = throwFromPreDestroy;
    }

    public void setNullFromConstructor(boolean nullFromConstructor) {
        this.nullFromConstructor = nullFromConstructor;
    }

    public void setNullFromMethods(boolean nullFromMethods) {
        this.nullFromMethods = nullFromMethods;
    }

    public void setNullFromFields(boolean nullFromFields) {
        this.nullFromFields = nullFromFields;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getConstructor(java.lang.Class)
     */
    @Override
    public <T> Constructor<T> getConstructor(Class<T> clazz)
            throws MultiException, NoSuchMethodException {
        if (throwFromConstructor) {
            throw new AssertionError(NegativeClassAnalysisTest.C_THROW);
        }
        if (nullFromConstructor) {
            return null;
        }
        return delegate.getConstructor(clazz);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getInitializerMethods(java.lang.Class)
     */
    @Override
    public <T> Set<Method> getInitializerMethods(Class<T> clazz)
            throws MultiException {
        if (throwFromMethods) {
            throw new AssertionError(NegativeClassAnalysisTest.M_THROW);
        }
        if (nullFromMethods) {
            return null;
        }
        return delegate.getInitializerMethods(clazz);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getFields(java.lang.Class)
     */
    @Override
    public <T> Set<Field> getFields(Class<T> clazz) throws MultiException {
        if (throwFromFields) {
            throw new AssertionError(NegativeClassAnalysisTest.F_THROW);
        }
        if (nullFromFields) {
            return null;
        }
        return delegate.getFields(clazz);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getPostConstructMethod(java.lang.Class)
     */
    @Override
    public <T> Method getPostConstructMethod(Class<T> clazz)
            throws MultiException {
        if (throwFromPostConstruct) {
            throw new AssertionError(NegativeClassAnalysisTest.PC_THROW);
        }
        
        return delegate.getPostConstructMethod(clazz);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getPreDestroyMethod(java.lang.Class)
     */
    @Override
    public <T> Method getPreDestroyMethod(Class<T> clazz) throws MultiException {
        if (throwFromPreDestroy) {
            throw new AssertionError(NegativeClassAnalysisTest.PD_THROW);
        }
        
        return delegate.getPostConstructMethod(clazz);
    }

}
