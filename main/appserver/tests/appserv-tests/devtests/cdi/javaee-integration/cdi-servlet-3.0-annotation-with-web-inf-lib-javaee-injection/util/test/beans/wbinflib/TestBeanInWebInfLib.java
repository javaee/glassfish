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

package test.beans.wbinflib;

import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class TestBeanInWebInfLib {
    @Inject
    BeanManager bm;
    
//    @Inject //@TestDatabase 
//    EntityManager emf_at_inj;

    @PersistenceContext(unitName="pu1")  
    EntityManager emf_at_pu;

    public String testInjection() {
        if (bm == null)
            return "Bean Manager not injected into the TestBean in WEB-INF/lib";
        System.out.println("BeanManager injected in WEB-INF/lib bean is " + bm);

        System.out.println("EMF injected in WEB-INF/lib bean is " + emf_at_pu);
        if (emf_at_pu == null)
            return "EMF injected via @PersistenceContext is not injected into " +
            		"the TestBean packaged in WEB-INF/lib";
        
        Set<Bean<?>> webinfLibBeans = bm.getBeans(TestBeanInWebInfLib.class, new AnnotationLiteral<Any>() {});
        if (webinfLibBeans.size() != 1)
            return "TestBean in WEB-INF/lib is not available via the WEB-INF/lib "
                    + "Bean's BeanManager";
        System.out.println("BeanManager.getBeans(TestBeanInWebInfLib, Any):" + webinfLibBeans);
        for (Bean b: webinfLibBeans) {
            debug(b);
        }

        // Get the proxy delegate
        org.jboss.weld.manager.BeanManagerImpl delegate =
                                     ((org.jboss.weld.bean.builtin.BeanManagerProxy) bm).delegate();

        Iterable<Bean<?>> accessibleBeans = delegate.getAccessibleBeans();
        System.out.println("BeanManagerImpl.getAccessibleBeans:" + accessibleBeans);
        for (Bean b : accessibleBeans) {
            debug(b);
        }

        Iterable<Bean<?>> beans = delegate.getBeans();
        System.out.println("BeanManagerImpl.getBeans:" + beans);
        for (Bean b : beans) {
            debug(b);
        }
        // success
        return "";
    }

    private void debug(Bean b) {
        String name = b.getBeanClass().getName();
        if (name.indexOf("Test") != -1) {
            System.out.println(name);
        }

    }
}
