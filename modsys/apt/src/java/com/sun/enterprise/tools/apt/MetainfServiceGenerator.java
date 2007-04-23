/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.tools.apt;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * {@link AnnotationProcessorFactory} implementation to generate <tt>META-INF/services</tt>
 * by scanning {@link org.jvnet.hk2.annotations.Service}.
 *
 * @author Jerome Dochez
 */
public class MetainfServiceGenerator implements AnnotationProcessorFactory {

    /**
     * Although I should theorically only return @Service annotation, I
     * use this trick of returning '*' so I have my annoation processor called
     * even if the @Service class is not present on the source file.
     * This is very useful when a class that used to be annotated with @Service
     * is not anymore so that I can remove it from the existing generated
     * META-INF/services file.
     */
    private static final Collection<String> supportedAnnotations
        = Collections.unmodifiableCollection(Arrays.asList("*"));

    // We only support debugging option
    private static final Collection<String> supportedOptions = Arrays.asList("-Adebug");

    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }

    public Collection<String> supportedOptions() {
        return supportedOptions;
    }

    public AnnotationProcessor getProcessorFor(
            Set<AnnotationTypeDeclaration> atds,
            AnnotationProcessorEnvironment env) {
        
            return new ServiceAnnotationProcessor(env);
        
    }    
}
