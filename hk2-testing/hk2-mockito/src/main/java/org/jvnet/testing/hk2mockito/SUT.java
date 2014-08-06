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

/**
 * {@literal @}SUT (Service Under Test) is an annotation used on fields and
 * methods of a Test class to inject a spy of the real service. Note that
 * calling methods on the spy will call the methods of the real service unless
 * they are stubbed with when()/give(). You can disable spying by setting
 * {@link #value()} to false.
 * <p>
 * Example:
 * </p>
 * <pre>
 *<code>
 *&#64;Service
 *public class GreetingService {
 *
 *  public String greet() {
 *    return sayHello();
 *  }
 *
 *  public String sayHello() {
 *    return "Hello!";
 *  }
 *}
 *</code>
 * </pre>
 * <pre>
 *<code>
 *&#64;HK2
 *public class GreetingServiceTest {
 *
 *  &#64;SUT
 *  &#64;Inject
 *  GreetingService sut;
 *
 *  &#64;BeforeMethod
 *  public void init() {
 *    reset(sut);
 *  }
 *
 *  &#64;Test
 *  public void verifyInjection() {
 *    assertThat(sut)
 *      .isNotNull()
 *      .isInstanceOf(MockitoSpy.class);
 *  }
 *
 *  &#64;Test
 *  public void callToGreetShouldReturnHello() {
 *    String greeting = "Hello!";
 *
 *    String result = sut.greet();
 *
 *    assertThat(result).isEqualTo(greeting);
 *    verify(sut).greet();
 *    verify(sut).sayHello();
 *  }
 *
 *  &#64;Test
 *  public void callToGreetShouldReturnHola() {
 *    String greeting = "Hola!";
 *    when(sut.sayHello()).thenReturn(greeting);
 *
 *    String result = sut.greet();
 *
 *    assertThat(result).isEqualTo(greeting);
 *    verify(sut).greet();
 *    verify(sut).sayHello();
 *  }
 *}
 * </code>
 * </pre>
 *
 * @author Sharmarke Aden
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER})
public @interface SUT {

    /**
     * Indicates whether a spy should be created. By default a spy of the real
     * service is created. Note that the spy calls real methods unless they are
     * stubbed.
     *
     * @return true if a spy should be created.
     */
    public boolean value() default true;
}
