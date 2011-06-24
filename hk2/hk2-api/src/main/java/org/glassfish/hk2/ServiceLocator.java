/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2;

/**
 * A ServiceLocator provides runtime access to a particular
 * service in HK2.
 * 
 * <p/>
 * At any point in time, one can get the {@link Provider}s that qualify
 * to the locator query by calling the super class methods.
 * 
 * <p/>
 * There are two variations of ServiceLocators. The first type
 * is when a ServiceLocator is produced by methods like 
 * {@link Services#byType(Class)} and {@link Services#byType(String)}.
 * 
 * <p/>
 * In HK2, services "byType" represent a concrete class type lacking
 * any contract level abstraction of that concrete class as shown in
 * this example:
 * 
 * <pre>
 * &#064;Service
 * public class Example {
 *  ...
 * }
 * </pre>
 * 
 * Services "byType" are either present or they are not. This is because
 * a service "byType" is a concrete class declaration as shown in the
 * example above. <i>Example.class</i> will either be present in the
 * {@link HK2} service registry or it will not. In another words, there
 * is no further way to abstractly locate <i>Example.class</i> when it
 * is being sought by its type.
 * 
 * <p/>
 * The methods {@link #getComponentProvider() and {@link #get()}
 * represent access to the singleton if the service "byType" exists in
 * the HK2 service registry. If the service component does not exist then
 * these methods will return null. In this situation, {@link #all()} will
 * return an empty collection. Otherwise {@link #all} will return a
 * singleton collection.
 * 
 * <p/>
 * The second variation of ServiceLocator is that it forms the
 * super interface for {@link ContractLocator} and is produced by methods
 * like {@link Services#forContract(Class)} and 
 * {@link Services#forContract(String)}.
 * 
 * <p/>
 * Unlike "byType" where there can either be zero or one manifestation of
 * a qualifying service, a {@link ContractLocator} can represent many
 * service instances in the HK2 service registry.  In this case, the
 * methods {@link #getComponentProvider()} and {@link #get()} represent
 * the "best qualifying" service matching the locator criteria.
 * 
 * <p>
 * See {@link ContractLocator} for more detail.
 * 
 * @author Jerome Dochez 
 * @author Jeff Trent
 * @author Mason Taube
 * 
 * @see ContractLocator
 */
public interface ServiceLocator<T> extends Providers<T> {

}
