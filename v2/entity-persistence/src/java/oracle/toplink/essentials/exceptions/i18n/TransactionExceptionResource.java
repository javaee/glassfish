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
 * English ResourceBundle for TransactionException messages.
 *
 */
public class TransactionExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "23001", "Error looking up external Transaction resource under JNDI name [{0}]" },
                                           { "23002", "Error obtaining status of current externally managed transaction" },
                                           { "23003", "Error obtaining current externally managed transaction" },
                                           { "23004", "Error obtaining the Transaction Manager" },
                                           { "23005", "Error binding to externally managed transaction" },
                                           { "23006", "Error beginning new externally managed transaction" },
                                           { "23007", "Error committing externally managed transaction" },
                                           { "23008", "Error rolling back externally managed transaction" },
                                           { "23009", "Error marking externally managed transaction for rollback" },
                                           { "23010", "No externally managed transaction is currently active for this thread" },
                                           { "23011", "UnitOfWork [{0}] was rendered inactive before associated externally managed transaction was complete" },
                                           { "23012", "No transaction is currently active" },
                                           { "23013", "Transaction is currently active" },
                                           { "23014", "Cannot use an EntityTransaction while using JTA." },
                                           { "23015", "Cannot enlist multiple datasources in the transaction." },
                                           { "23016", "Exception in Proxy execution." }
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
