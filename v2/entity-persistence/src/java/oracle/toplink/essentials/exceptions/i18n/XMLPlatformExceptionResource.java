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

/**
 * INTERNAL:
 */
public class XMLPlatformExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "27001", "The XML Platform class was not found: {0}" },
                                           { "27002", "The XML Platform could not be instantiated: {0}" },
                                           { "27003", "A new XML document could not be created." },
                                           { "27004", "The XPath was invalid." },
                                           { "27005", "An error occurred while validating the document." },
                                           { "27006", "Could not resolve XML Schema:  {0}" },
                                           { "27101", "An error occurred while parsing the document." },
                                           { "27102", "File not found: [{0}]" },
                                           { "27103", "** Parsing error, line [{0}], uri [{1}] [{2}]" },
                                           { "27201", "An error occurred while transforming the document." },
                                           { "27202", "Unknown type encountered: {0}" },
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
