/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.deployment.impl;

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.api.deployment.JavaModel;
import com.sun.persistence.deployment.impl.reflection.JavaModelImpl;
import com.sun.persistence.deployment.impl.reflection.PersistentPropertyIntrospectorImpl;

/**
 * This is a factory for {@link PersistentPropertyIntrospector}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistentPropertyIntrospectorFactoryImpl
        implements PersistentPropertyIntrospectorFactory {
    private static I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    private PersistentPropertyIntrospectorFactoryImpl() {
    }

    public static PersistentPropertyIntrospectorFactoryImpl getInstance() {
        return new PersistentPropertyIntrospectorFactoryImpl();
    }

    public PersistentPropertyIntrospector getIntrospector(JavaModel javaModel) {
        if (javaModel instanceof JavaModelImpl) {
            return new PersistentPropertyIntrospectorImpl(
                    JavaModelImpl.class.cast(javaModel));
        }
        throw new RuntimeException(i18NHelper.msg("EXC_UnrecognizedJavaModel")); // NOI18N
    }

}
