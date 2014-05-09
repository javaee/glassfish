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
package org.glassfish.hk2.configuration.tests.simple;

import javax.annotation.PostConstruct;

import org.glassfish.hk2.configuration.api.Configured;
import org.glassfish.hk2.configuration.api.ConfiguredBy;
import org.junit.Assert;

/**
 * @author jwells
 */
@ConfiguredBy(type=BasicConfigurationTest.TEST_TYPE_ONE)
public class ConfiguredService {
    @Configured
    private String fieldOutput1;
    
    @Configured(key="fieldOutput2")
    private String anotherField;
    
    private final String constructorOutput;
    private String methodOutput1;
    private String methodOutput2;
    
    private ConfiguredService(@Configured(key="constructorOutput") String constructorOutput,
            SimpleService simpleService) {
        simpleService.hashCode();  //throws NPE if simpleService is null
        this.constructorOutput = constructorOutput;
    }
    
    @SuppressWarnings("unused")
    private void setMethodOutput1(@Configured(key="methodOutput1") String methodOutput1) {
        this.methodOutput1 = methodOutput1;
        
    }
    
    @SuppressWarnings("unused")
    private void anotherMethodInitializer(@Configured(key="methodOutput2") String methodOutput2,
            SimpleService simpleService) {
        simpleService.hashCode();  //throws NPE if simpleService is null
        
        this.methodOutput2 = methodOutput2;
    }
    
    @PostConstruct
    private void postConstruct() {
        Assert.assertNotNull(fieldOutput1);
        Assert.assertNotNull(anotherField);
        Assert.assertNotNull(constructorOutput);
        Assert.assertNotNull(methodOutput1);
        Assert.assertNotNull(methodOutput2);
    }
    
    public String getFieldOutput1() {
        return fieldOutput1;
    }
    
    public String getFieldOutput2() {
        return anotherField;
    }
    
    public String getConstructorOutput() {
        return constructorOutput;
    }
    
    public String getMethodOutput1() {
        return methodOutput1;
    }

    public String getMethodOutput2() {
        return methodOutput2;
    }
}
