/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.server.core;
import com.sun.enterprise.config.serverbeans.Mbean;
import java.util.List;
import javax.management.ObjectName;

/** Defines the behavior to register MBeans in an MBeanServer.
 */
public interface CustomMBeanRegistration {

    /** Registers all the Mbeans in the given List of @link{Mbean} instances. Following is
     * the contract of this method: 
     * <ul>
     * <li> The given List must be non-null, an InvalidArgumentException is thrown otherwise </li>
     * <li> Only the MBeans that are enabled are registered in MBeanServer </li>
     * <li> Continues to load rest of the Mbeans if registration for one of them fails </li>
     * <li> Never throws an Exception if the given List is non-null
     * </ul>
     * @param mbeans a list of MBeans to register, the list will be traversed serially
     * @throws CustomMBeanException if the param is null
     *
     */
    public void registerMBeans(List<Mbean> mbeans) throws CustomMBeanException;

    /** Registers all the Mbeans in the given List of @link{Mbean} instances. Following is
     * the contract of this method: 
     * <ul>
     * <li> Degenerates to registerMBeans(mbeans) if param continueReg is true</li>
     * <li> If continueReg is false and there is any problem with registering an MBean in the given List, a
     * CustomMBeanException is thrown </li>
     * @param mbeans a List of MBeans to register
     * @param continueReg a boolean to control the behavior of registration
     * </ul>
     */
    public void registerMBeans(List<Mbean> mbeans, boolean continueReg) throws CustomMBeanException;

    /** Registers an MBean per the given MBean definition in an MBeanServer of
     * implementation choice. Registration process involves loading of MBean Class
     * by a class-loader of implementation choice, instantiating it, registering it in the
     * MBeanServer and initializing it if the configuration (MBeanDefinition) has it.
     * If the initialization of the MBean fails, a RuntimeException is thrown and the 
     * MBean is deregistered if it was registered. Appropriate exceptions are available in
     * the stack of the exceptions at every stage during registration, as follows: <ul>
     * <li> ClassNotFoundException, NoClassDefFoundError if the class could not be loaded </li>
     * <li> ExceptionInInitializerError, IllegalAccessException, InstantiationException if the MBean instance
     *  could not be created </li>
     * <li> Standard attribute setting exceptions from MBeanServer's setAttribute(Attribute, Object) contract </li>
     * </ul>
     * The initialization is done using several calls to setAttribute(Attribute, Object) on the MBeanServer so as
     * to know setting what attribute causes a problem, if any.
     * @return ObjectName with which the MBean is going to be actually registered. If the Mbean definition given is not enabled,
     * a null is returned
     */
    public ObjectName registerMBean(Mbean mbd) throws CustomMBeanException;
    
    /** Sets the ClassLoader instance that would be used in loading of MBean Class from this 
     * point onwards. This will affect the behavior of the implementing class once invoked because
     * all the subsequent registration calls will use the given instance of ClassLoader. When set, this
     * class-loader is used as the <i> initiating class loader </i> of the MBean Classes.
     * @param cl ClassLoader instance to use, must be non-null
     * @throws IllegalArgumentException if the given class-loader is null
     */
    public void setClassLoader(ClassLoader cl) throws CustomMBeanException;
}