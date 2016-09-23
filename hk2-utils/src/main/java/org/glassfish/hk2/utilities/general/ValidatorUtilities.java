/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities.general;

import java.lang.annotation.ElementType;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

import org.glassfish.hk2.utilities.general.internal.MessageInterpolatorImpl;
import org.hibernate.validator.HibernateValidator;


/**
 * @author jwells
 *
 */
public class ValidatorUtilities {
    private static final TraversableResolver TRAVERSABLE_RESOLVER = new TraversableResolver() {
        public boolean isReachable(Object traversableObject,
                Path.Node traversableProperty, Class<?> rootBeanType,
                Path pathToTraversableObject, ElementType elementType) {
                    return true;
        }

        public boolean isCascadable(Object traversableObject,
                Path.Node traversableProperty, Class<?> rootBeanType,
                Path pathToTraversableObject, ElementType elementType) {
                    return true;
        }
        
    };
    
    private static Validator validator;
    
    private static Validator initializeValidator() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        try {      
            Thread.currentThread().setContextClassLoader(HibernateValidator.class.getClassLoader());
       
            ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
            ValidatorContext validatorContext = validatorFactory.usingContext();
            validatorContext.messageInterpolator(new MessageInterpolatorImpl());                
            return validatorContext.traversableResolver(
                       TRAVERSABLE_RESOLVER).getValidator();
        }
        finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
    
    /**
     * Gets a validator that can be used to validate that is initialized with HK2
     * specific utilities such as the message interpolator
     * 
     * @return A javax bean validator for validating constraints
     */
    public synchronized static Validator getValidator() {
        if (validator == null) {
            validator = AccessController.doPrivileged(new PrivilegedAction<Validator>() {

                @Override
                public Validator run() {
                    return initializeValidator();
                }
                
            });
        }
        
        if (validator == null) {
            throw new IllegalStateException("Could not find a javax.validator");
        }
        
        return validator;
    }

}
