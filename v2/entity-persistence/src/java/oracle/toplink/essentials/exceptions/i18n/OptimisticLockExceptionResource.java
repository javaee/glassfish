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
 * English ResourceBundle for OptimisticLockException messages.
 *
 * Creation date: (12/6/00 9:47:38 AM)
 * @author: Xi Chen
 */
public class OptimisticLockExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "5001", "An attempt was made to delete the object [{0}], but it has no version number in the identity map. {3}It may not have been read before the delete was attempted. {3}Class> {1} Primary Key> {2}" },
                                           { "5003", "The object [{0}] cannot be deleted because it has changed or been deleted since it was last read. {3}Class> {1} Primary Key> {2}" },
                                           { "5004", "An attempt was made to update the object [{0}], but it has no version number in the identity map. {3}It may not have been read before the update was attempted. {3}Class> {1} Primary Key> {2}" },
                                           { "5006", "The object [{0}] cannot be updated because it has changed or been deleted since it was last read. {3}Class> {1} Primary Key> {2}" },
                                           { "5007", "The object [{0}] must have a non-read-only mapping to the version lock field." },
                                           { "5008", "Must map the version lock field to java.sql.Timestamp when using Timestamp Locking" },
                                           { "5009", "The object of class [{1}] of class [{0}] cannot be unwrapped because it was deleted since it was last read." },
                                           { "5010", "The object [{0}] cannot be merged because it has changed or been deleted since it was last read. {3}Class> {1}" }
                                           
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
