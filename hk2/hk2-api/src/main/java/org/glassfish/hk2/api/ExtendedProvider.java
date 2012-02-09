/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import javax.inject.Provider;

/**
 * This class extends the standard {@link Provider}.  Wherever a
 * {@link Provider} can be injected an ExtendedProvider can be injected.
 * <p>
 * This class provides extra information about the injected service, as
 * well as the ability to retrieve the service as another type (which
 * may be proxied)
 * 
 * @author Jerome Dochez, Jeff Trent, Mason Taube
 *
 */
public interface ExtendedProvider<T> extends Provider<T> {
  /**
   * Returns the descriptor associated with this Provider
   * 
   * @return The descriptor associated with this Provider
   */
  public Descriptor getDescriptor();

  /**
   * Obtain a reference to the associated component/service.
   * The given type may be used by the provider to obtain
   * and return a proxy rather than the actual component.
   *
   * @param type  the required type of the returned object
   *
   * @return the component or a suitable proxy of the given type
   *
   * @throws ClassCastException if the component is not an instance of
   *         the given type
   *
   * @throws ComponentException if this provider failed to get or
   *         create an instance of the component
   */
  public <U> U getByType(Class<U> type);

  /**
   * Returns true if the component has been instantiated.
   *
   * @return true if the component is active.
   */
  public boolean isActive();
}
