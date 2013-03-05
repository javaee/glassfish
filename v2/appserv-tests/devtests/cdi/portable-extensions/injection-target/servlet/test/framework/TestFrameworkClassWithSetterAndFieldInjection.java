/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package test.framework;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import test.beans.Preferred;
import test.beans.TestBean;
import test.beans.TestNamedBean;


public class TestFrameworkClassWithSetterAndFieldInjection {
    
    @Inject TestNamedBean tnb;

    private TestBean tb;
    
    private String msg = "";

    private boolean postConstructCalled = false;
    private boolean preDestroyCalled = false;

    // must have default no-arg constructor or weld will puke
    public TestFrameworkClassWithSetterAndFieldInjection() {
    }

    public TestFrameworkClassWithSetterAndFieldInjection(String magicKey){
        if(!magicKey.equals("test")) throw new RuntimeException();
    }
    
    
    @Inject
    public void setTestBean(@Preferred TestBean tb){
        System.out.println("Setter based injection " +
                "into a framework class" + tb);
        this.tb = tb;
        if (tb == null) {
            msg += "Constructor injection in a test framework class failed";
        }
        
    }
    
    @PostConstruct
    private void beanPostConstruct() {
        this.postConstructCalled = true;
        if (tnb == null) {
            msg += "regular field injection in a test framework class failed";
        }
    }

    @PreDestroy
    private void beanPreDestroy() {
        this.preDestroyCalled = true;
    }

    public String getInitialTestResults() {
        if (!postConstructCalled)
            msg += "PostConstruct was not called in test framework class";
        String response = msg;
        msg = "";
        return response;
    }

    public String getFinalTestResults() {
        if (!preDestroyCalled)
            msg += "PreDestroy was not called " + "in test framework class";
        return msg;
    }
}
