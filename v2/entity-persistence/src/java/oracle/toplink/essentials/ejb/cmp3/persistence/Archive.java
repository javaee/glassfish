/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
 * 
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

package oracle.toplink.essentials.ejb.cmp3.persistence;

import java.util.Iterator;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Provides an abstraction to deal with various kinds of URLs that can
 * be returned by
 * {@link javax.persistence.spi.PersistenceUnitInfo#getPersistenceUnitRootUrl()}
 *
 * @see ArchiveFactoryImpl
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface Archive {
    /*
     * Implementation Note: This class does not have any dependency on TopLink
     * or GlassFish implementation classes. Please retain this searation.
     */

    /**
     * Returns an {@link java.util.Iterator} of the file entries. Each String represents
     * a file name relative to the root of the module.
     */
    Iterator<String> getEntries();

    /**
     * Returns the InputStream for the given entry name. Returns null if no such
     * entry exists. The entry name must be relative to the root of the module.
     *
     * @param entryPath the file name relative to the root of the module.
     * @return the InputStream for the given entry name or null if not found.
     */
    InputStream getEntry(String entryPath) throws IOException;

    /**
     * Returns the URL for the given entry name. Returns null if no such
     * entry exists. The entry name must be relative to the root of the module.
     *
     * @param entryPath the file name relative to the root of the module.
     * @return the URL for the given entry name or null if not found.
     */
    URL getEntryAsURL(String entryPath) throws IOException;

    /**
     * @return the URL that this archive represents.
     */
    URL getRootURL();
}

