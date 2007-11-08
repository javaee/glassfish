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

import java.util.*;
import oracle.toplink.essentials.mappings.converters.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;

/**
 * <p><b>Purpose</b>:
 * Concrete class to perform a direct read.
 * <p>
 * <p><b>Responsibilities</b>:
 * Used in conjunction with DirectCollectionMapping.
 * This can be used to read a single column of data (i.e. one field).
 * A container (implementing Collection) of the data values is returned.
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class DirectReadQuery extends DataReadQuery {

    /** Allows user defined conversion between the result value and the database value. */
    protected Converter valueConverter;

    /**
     * PUBLIC:
     * Initialize the state of the query.
     */
    public DirectReadQuery() {
        super();
    }

    /**
     * PUBLIC:
     * Initialize the query to use the specified SQL string.
     */
    public DirectReadQuery(String sqlString) {
        super(sqlString);
    }

    /**
     * PUBLIC:
     * Initialize the query to use the specified call.
     */
    public DirectReadQuery(Call call) {
        super(call);
    }

    /**
     * PUBLIC:
     * Return the converter on the query.
     * A converter can be used to convert between the result value and database value.
     */
    public Converter getValueConverter() {
        return valueConverter;
    }

    /**
     * PUBLIC:
     * Set the converter on the query.
     * A converter can be used to convert between the result value and database value.
     */
    public void setValueConverter(Converter valueConverter) {
        this.valueConverter = valueConverter;
    }

    /**
     * INTERNAL:
     * Used by cursored stream.
     * Return the first field in the row.
     */
    public Object buildObject(AbstractRecord row) {
        Object value = row.get(row.getFields().firstElement());
        if (getValueConverter() != null) {
            value = getValueConverter().convertDataValueToObjectValue(value, session);
        }
        return value;
    }

    /**
     * INTERNAL:
     * The results are *not* in a cursor, build the collection.
     */
    public Object executeNonCursor() throws DatabaseException, QueryException {
        ContainerPolicy cp = getContainerPolicy();

        Vector rows = getQueryMechanism().executeSelect();
        Object result = cp.containerInstance(rows.size());
        DatabaseField resultDirectField = null;

        for (Enumeration stream = rows.elements(); stream.hasMoreElements();) {
            AbstractRecord row = (AbstractRecord)stream.nextElement();
            if (resultDirectField == null) {
                resultDirectField = (DatabaseField)row.getFields().firstElement();
            }
            Object value = row.get(resultDirectField);
            if (getValueConverter() != null) {
                value = getValueConverter().convertDataValueToObjectValue(value, session);
            }
            cp.addInto(value, result, getSession());
        }
        return result;
    }
}
