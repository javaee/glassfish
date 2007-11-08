/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
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
package oracle.toplink.essentials.exceptions.i18n;

import java.util.ListResourceBundle;

public class PersistenceUnitLoadingExceptionResource extends ListResourceBundle {
 
        static final Object[][] contents = {
                                           { "30001", "An exception was thrown while trying to load a persistence unit for directory: {0}"},
                                           { "30002", "An exception was thrown while trying to load a persistence unit for jar file: {0}"},
                                           { "30003", "An exception was thrown while processing persistence unit at URL: {0}"},
                                           { "30004", "An exception was thrown while processing persistence.xml from URL: {0}"},
                                           { "30005", "An exception was thrown while searching for persistence archives with ClassLoader: {0}"},
                                           { "30006", "An exception was thrown while searching for entities at URL: {0}"},
                                           { "30007", "An exception was thrown while loading class: {0} to check whether it implements @Entity, @Embeddable, or @MappedSuperclass."},
                                           { "30008", "File path returned was empty or null"},                                                                  
                                           { "30009", "An exception was thrown while trying to load persistence unit at url: {0}"},
                                           { "30010", "An exception was thrown while loading ORM XML file: {0}"},
                                           { "30011", "TopLink could not get classes from the URL: {0}.  TopLink attempted to read this URL as a jarFile and as a Directory and was unable to process it."},
                                           { "30012", "TopLink could not get persistence unit info from the URL:{0}"}
    };

    /**
      * Return the lookup table.
      */
    protected Object[][] getContents() {
        return contents;
    }
}
