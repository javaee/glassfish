/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2014 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.osgiresourcelocator;

import java.net.URL;
import java.util.List;

/**
 * This class provides helper methods to look up resources that are part of OSGi bundles,
 * but can't be exported. e.g., META-INF/mailcap file used by JavaMail.
 *
 * This class has been carefully coded to be loadable in non-OSGi environment.
 * When it is used in such an environment, various getENtry methods return null.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class ResourceFinder {
    private static ResourceFinder _me;

    public static void initialize(ResourceFinder singleton) {
        if (singleton == null) throw new NullPointerException("Did you intend to call reset()?");
        if (_me != null) throw new IllegalStateException("Already initialzed with [" + _me + "]");
        _me = singleton;
    }

    public static synchronized void reset() {
        if (_me == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        _me = null;
    }


    public static URL findEntry(String path) {
        if (_me == null) return null;
        return _me.findEntry1(path);
    }

    public static List<URL> findEntries(String path) {
        if (_me == null) return null;
        return _me.findEntries1(path);
    }

    /*package*/
    abstract URL findEntry1(String path);

    /*package*/
    abstract List<URL> findEntries1(String path);
}
