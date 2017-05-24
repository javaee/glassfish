/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;
import java.lang.reflect.*;


public class BaseBean {

    // InterceptorA
    boolean ac = false;

    // InterceptorB
    boolean ac1 = false;

    // InterceptorC
    boolean ac2 = false;

    Method method = null;

    void verifyMethod(String name) {
        if (method == null) {
            if (name != null) throw new RuntimeException("In " + getClass().getName() + " expected method name: " + name + " got null");
        } else {
            if (!method.getName().equals(name)) 
                throw new RuntimeException("In " + getClass().getName() + " expected method name: " + name + " got: " + method.getName());
        }
    }

    void verify(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was called");
    }

    void verifyA(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was called");
    }

    void verifyA_AC(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
    }

    void verifyB_AC(String name) {
        if (ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
    }

    void verifyAB_AC(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was called");
    }

    void verifyAC_AC(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
        if (!ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was not called");
    }
}
