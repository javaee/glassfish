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
 * English ResourceBundle for DefaultMappingException messages.
 *
 * @author: King Wang
 * @since: OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class DefaultMappingExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "20001", "Could not find the parameter type: [{2}], defined in the ejb-jar.xml, of the finder: [{1}] in the entity bean: [{0}]." },
                                           { "20002", "The finder method: [{1}] with the parameters as: [{2}], defined in the ejb-jar.xml, is not found in the home of bean: [{0}]." },
                                           { "20003", "The ejbSelect method: [{1}] with the parameters as: [{2}], defined in the ejb-jar.xml, is not found in the bean class of bean: [{0}]." },
                                           { "20004", "The finder method: [{1}] of bean: [{0}] in ejb-jar.xml file is not well defined. It should start with either 'find' or 'ejbSelect'." },
                                           { "20005", "The abstract getter method: [{0}] is not defined in the bean: [{1}]." },
                                           { "20006", "The cmp field: [{0}] is not defined in the bean: [{1}]." }
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
