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
 * English ResourceBundle for DatabaseException messages.
 *
 * Creation date: (12/6/00 9:47:38 AM)
 * @author: Xi Chen
 */
public class DatabaseExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "4003", "Configuration error.  Class [{0}] not found." },
                                           { "4005", "DatabaseAccessor not connected." },
                                           { "4006", "Error reading BLOB data from stream in getObject()." },
                                           { "4007", "Could not convert object type due to an internal error. {0}java.sql.TYPES: [{1}]" },
                                           { "4008", "You cannot logout while a transaction is in progress." },
                                           { "4009", "The sequence table information is not complete." },
                                           { "4011", "Error preallocating sequence numbers.  The sequence table information is not complete." },
                                           { "4014", "Cannot register SynchronizationListener." },
                                           { "4015", "Synchronized UnitOfWork does not support the commitAndResume() operation." },
                                           { "4016", "Configuration error.  Could not instantiate driver [{0}]." },
                                           { "4017", "Configuration error.  Could not access driver [{0}]." },
                                           { "4018", "The TransactionManager has not been set for the JTS driver." },
                                           { "4019", "Error while obtaining information about the database. Please look at the nested exception for more details." }
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
