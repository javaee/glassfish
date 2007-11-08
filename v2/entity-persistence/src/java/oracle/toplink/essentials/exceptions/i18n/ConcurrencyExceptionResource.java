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
 * English ResourceBundle for ConcurrencyException messages.
 *
 * Creation date: (12/6/00 9:47:38 AM)
 * @author: Xi Chen
 */
public class ConcurrencyExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "2001", "Wait was interrupted. {0}Message: [{1}]" },
                                           { "2002", "Wait failure on ServerSession." },
                                           { "2003", "Wait failure on ClientSession." },
                                           { "2004", "A signal was attempted before wait() on ConcurrencyManager. This normally means that an attempt was made to {0}commit or rollback a transaction before it was started, or to rollback a transaction twice." },
                                           { "2005", "Wait failure on Sequencing Connection Handler for DatabaseSession." },
                                           { "2006", "Attempt to acquire sequencing values through a single Connection({0}) simultaneously in multiple threads" },
                                           { "2007", "Max number of attempts to lock object: {0} exceded.  Failed to clone the object." },
                                           { "2008", "Max number of attempts to lock object: {0} exceded.  Failed to merge the transaction." },
                                           { "2009", "Max number of attempts to lock object exceded.  Failed to build the object. Thread: {0} has a lock on the object but thread: {1} is building the object"}																					 
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
