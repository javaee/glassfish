/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.osgijpa;

import org.osgi.framework.Bundle;

import java.util.jar.JarInputStream;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
interface JPAEnhancer
{
    /**
     * An enhancer is used to statically enhance the classes of a bundle and
     * produce a new JarInputStream which can then be used to update the
     * supplied bundle. It returns null if no enhancement is needed, e.g., the
     * bundle might have been enhanced already.
     * An enhancer may have to explode the bundle in a directory so that it
     * can scan the contents of the bundle using File or Jar APIs. If so, it
     * is the responsibility of the enhancer to delete such temporary
     * locations.
     *
     * @param b Bundle to be enhanced
     * @param puRoots Entries in the bundle containing META-INF/persistence.xml
     * @return a JarInputStream which contains the enhanced classes along with
     * changed manifest
     */
    InputStream enhance(Bundle b, List<String> puRoots) throws IOException;
}
