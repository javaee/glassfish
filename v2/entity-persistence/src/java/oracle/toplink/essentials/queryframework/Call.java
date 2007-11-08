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
package oracle.toplink.essentials.queryframework;

import java.io.Serializable;
import oracle.toplink.essentials.internal.databaseaccess.Accessor;
import oracle.toplink.essentials.internal.queryframework.*;

/**
 * Call defines the interface used primarily by TopLink queries
 * and query mechanisms to perform the necessary actions
 * (read, insert, update, delete) on the data store.
 * A Call can collaborate with an Accessor to perform its
 * responsibilities. The only explicit requirement of a Call is that
 * it be able to supply the appropriate query mechanism for
 * performing its duties. Otherwise, the Call is pretty much
 * unrestricted as to how it should perform its responsibilities.
 *
 * @see DatabaseQuery
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 */
public interface Call extends Cloneable, Serializable {

    /**
     * INTERNAL:
     * Return the appropriate mechanism,
     * with the call set as necessary.
     */
    DatabaseQueryMechanism buildNewQueryMechanism(DatabaseQuery query);

    /**
     * INTERNAL:
     * Return the appropriate mechanism,
     * with the call added as necessary.
     */
    DatabaseQueryMechanism buildQueryMechanism(DatabaseQuery query, DatabaseQueryMechanism mechanism);

    /**
     * INTERNAL:
     * Return a clone of the call.
     */
    Object clone();

    /**
     * INTERNAL:
     * Return a string appropriate for the session log.
     */
    String getLogString(Accessor accessor);

    /**
     * INTERNAL:
     * Return whether the call is finished returning
     * all of its results (e.g. a call that returns a cursor
     * will answer false).
     */
    boolean isFinished();
}
