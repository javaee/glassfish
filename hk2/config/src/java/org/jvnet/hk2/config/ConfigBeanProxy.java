/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.config;

/**
 * Marker interface that signifies that the interface
 * is meant to be used as a strongly-typed proxy to
 * {@link Dom}. 
 *
 * <p>
 * To obtain the Dom object, use {@link Dom#unwrap(ConfigBeanProxy)}.
 * This design allows the interfaces to be implemented by other code
 * outside DOM more easily.
 *
 * @author Kohsuke Kawaguchi
 * @see Dom#unwrap(ConfigBeanProxy)
 * @see DuckTyped
 * @see Element
 * @see Attribute
 */
public interface ConfigBeanProxy {

    /**
     * Returns the parent element of this configuration element.
     *
     * It is possible to return a not null parent while the parent knows nothing of this
     * child element. This could happen when the child element was removed
     * from the configuration tree, yet it's parent would not have been reset.
     *
     * @return the parent configuration node.
     */
    @DuckTyped
    public ConfigBeanProxy getParent();

    /**
     * Returns the typed parent element of this configuration element.
     *
     * It is possible to return a not null parent while the parent knows nothing of this
     * child element. This could happen when the child element was removed
     * from the configuration tree, yet it's parent would not have been reset.
     *
     * @param type parent's type
     * @return the parent configuration node.
     */
    @DuckTyped
    public <T extends ConfigBeanProxy> T getParent(Class<T> type);

    /**
     * Creates a child element of this configuration element
     *
     * @param type the child element type
     * @return the newly created child instance
     * @throws TransactionFailure when called outside the boundaries of a transaction 
     */
    @DuckTyped
    public <T extends ConfigBeanProxy> T createChild(Class<T> type) throws TransactionFailure;


    public class Duck {

        public static ConfigBeanProxy getParent(ConfigBeanProxy self) {
            Dom dom = Dom.unwrap(self);
            return dom.parent().createProxy();
        }

        public static <T extends ConfigBeanProxy> T getParent(ConfigBeanProxy self, Class<T> c) {
             Dom dom = Dom.unwrap(self);
            return dom.parent().createProxy(c);
        }

        public static <T extends ConfigBeanProxy> T createChild(ConfigBeanProxy self, Class<T> c)
            throws TransactionFailure {
            
            return ConfigSupport.createChildOf(self, c);
        }

    }

}
