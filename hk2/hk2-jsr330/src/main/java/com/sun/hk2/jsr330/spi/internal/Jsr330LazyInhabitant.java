/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.hk2.jsr330.spi.internal;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Scope;
import org.jvnet.hk2.component.Womb;

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.LazyInhabitant;

/**
 * Jsr330 Inhabitants have special injection semantics.
 * 
 * @author Jeff Trent
 *
 * @since 3.1
 */
public class Jsr330LazyInhabitant<T> extends LazyInhabitant<T> {

  public Jsr330LazyInhabitant(
      Habitat habitat,
      Holder<ClassLoader> cl,
      String typeName,
      Scope scope,
      MultiMap<String, String> metadata) {
    super(habitat, cl, typeName, metadata);
  }

  @Override
  protected Womb<T> createWomb(Class<T> c) {
    return new Jsr330ConstructorWomb<T>(c, habitat, metadata());
  }
    
}
