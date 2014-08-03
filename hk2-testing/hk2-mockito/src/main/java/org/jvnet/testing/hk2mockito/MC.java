/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.testing.hk2mockito;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import org.mockito.Answers;
import org.mockito.MockSettings;

/**
 * {@literal @}MC (Mock Collaborator) annotation is used on fields and methods
 * of a Test class to inject a mock of the {@literal @}SUT's collaborating
 * services.
 *
 * @author Sharmarke Aden
 * @see MockSettings
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER})
public @interface MC {

    /**
     * If the collaborator service being injected is a constructor or method
     * parameter of the SUT, this value should indicate the index of the
     * parameter. By default the collaborator will be detected but in instances
     * (i.e. injecting two or more services with the same type) you may want to
     * explicitly specify the index of the service.
     *
     * @return the index of the parameter.
     */
    int value() default 0;

    /**
     * If the collaborator service being injected is a field of the SUT, this
     * value should indicate the name of the field. By default the
     * {@literal @}SC field name is used as the collaborator field name but in
     * instances (i.e. field injection of two or more services with the same
     * type) you may want to explicitly specify the name of the field.
     *
     * @return the name of the field.
     */
    String field() default "";

    /**
     * Specifies default answers to interactions.
     *
     * @return default answer to be used by mock when not stubbed.
     */
    Answers answer() default Answers.RETURNS_DEFAULTS;

    /**
     * Specifies mock name. Naming mocks can be helpful for debugging - the name
     * is used in all verification errors. By default the field name will be
     * used as mock name.
     *
     * @return the name of the mock.
     */
    String name() default "";

    /**
     * Specifies extra interfaces the mock should implement. Might be useful for
     * legacy code or some corner cases. For background, see issue 51 <a
     * href="http://code.google.com/p/mockito/issues/detail?id=51">here</a>
     *
     * @return extra interfaces that should be implemented.
     */
    Class<?>[] extraInterfaces() default {};
}
