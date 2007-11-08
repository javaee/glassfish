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
 * English ResourceBundle for ConversionException messages.
 *
 * Creation date: (12/6/00 9:47:38 AM)
 * @author: Rick Barkhouse
 */
public class ConversionExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "3001", "The object [{0}], of class [{1}], could not be converted to [{2}]." },
                                           { "3002", "The object [{0}], of class [{1}], from mapping [{2}] with descriptor [{3}], could not be converted to [{4}]." },
                                           { "3003", "Incorrect date format: [{0}] (expected [YYYY-MM-DD])" },
                                           { "3004", "Incorrect time format: [{0}] (expected [HH:MM:SS])" },
                                           { "3005", "Incorrect timestamp format: [{0}] (expected [YYYY-MM-DD HH:MM:SS.NNNNNNNNN])" },
                                           { "3006", "[{0}] must be of even length to be converted to a byte array." },
                                           { "3007", "The object [{0}], of class [{1}], could not be converted to [{2}].  Please ensure that the class [{0}] is on the CLASSPATH.  You may need to use alternate API passing in the appropriate class loader as required, or setting it on the default ConversionManager" },
                                           { "3008", "Incorrect date-time format: [{0}] (expected [YYYY-MM-DD'T'HH:MM:SS])" }
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
