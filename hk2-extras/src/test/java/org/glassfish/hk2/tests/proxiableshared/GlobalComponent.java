
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.proxiableshared;

import javax.inject.Inject;

/**
 * Global component that is managed by its own "bean manager".
 * The component is made accessible in HK2 via {@link GlobalComponentFactory}
 * and is being injected by HK2 via a simple {@link ComponentInjector} SPI.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class GlobalComponent {

    /**
     * Our "singleton" instance.
     */
    static GlobalComponent instance = null;

    /**
     * This is to allow HK2 to inject a component managed here.
     */
    public interface ComponentInjector {

        /**
         * Just inject provided component.
         *
         * @param component injected component.
         */
        void inject(GlobalComponent component);
    }

    /**
     * Our "bean manager" implementation. All we do here
     * is we mimic CDI application scope.
     */
    public static class BeanManager {

        /**
         * Start from scratch.
         */
        public static void restart() {
            instance = null;
        }

        /**
         * Get me the actual global component, that will get
         * injected by provided injector after instantiation.
         * If there is an existing instance available,
         * no injection happens.
         *
         * @param injector use this to inject a new component.
         * @return injected component.
         */
        public static GlobalComponent provideComponent(ComponentInjector injector) {

            if (instance == null) {
                instance = new GlobalComponent();
                injector.inject(instance);
            }

            return instance;
        }
    }

    private GlobalComponent() {
        // disable instantiation
    }

    /**
     * HK2 injected field. A dynamic proxy will be injected here,
     * that will unfortunately keep reference to the first HK2 locator
     * used to inject this.
     */
    @Inject
    private ReqData request;

    /**
     * Get me actual request name, so that i can check you have the right guy.
     *
     * @return actual request name.
     */
    public String getRequestName() {
        return request.getRequestName();
    }
}
