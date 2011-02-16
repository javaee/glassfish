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
package org.jvnet.hk2.component.classmodel;

import java.io.FileFilter;
import java.net.URI;
import java.util.Set;

import org.jvnet.hk2.component.classmodel.ClassPath;
import org.jvnet.hk2.junit.Hk2RunnerOptions;

/**
 * Assists in the creation of the habitat via class-model introspection.  Implementations of
 * this contract can fulfill two separate but related actions.
 * 
 * <p>
 * 1. It can prune the classpath used to construct the habitat based on some filter criteria.
 * 
 * <p>
 * 2. It can be provided feedback by the introspection machinery regarding the URIs in the
 * classpath that were significant during creation of the habitat that can be used by the
 * implementation to fine-tune future runs.
 * 
 * <p>
 * Both activities above are important for building a caching scheme for example, to
 * make class-model introspection more performant over repeated runs.
 * 
 * <p>
 * See {@link Hk2RunnerOptions#classpathFilter()} for usage.
 * 
 * @author Jeff Trent
 */
public interface ClassPathAdvisor extends FileFilter {

  /**
   * Called at the start of class-model habitat creation
   * 
   * @param inhabitantsClassPath the full classpath for locating class artifacts
   */
  void starting(ClassPath inhabitantsClassPath);
  
  /**
   * Called at the completion of class-model habitat creation
   * 
   * @param significant
   *  the set of code sources that were significant in that they contributed to logical habitat creation
   * @param insignificant
   *  the set of code sources that were not significant in creation of the logical habitat
   */
  void finishing(Set<URI> significant, Set<URI> insignificant);
  
}
