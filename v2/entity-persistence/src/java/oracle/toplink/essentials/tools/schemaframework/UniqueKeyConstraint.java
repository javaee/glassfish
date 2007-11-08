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
package oracle.toplink.essentials.tools.schemaframework;

import java.io.*;
import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Define a unique key constraint for a table.
 */
public class UniqueKeyConstraint implements Serializable {
    protected String name;
    protected Vector<String> sourceFields; // field names

    public UniqueKeyConstraint() {
        this.name = "";
        this.sourceFields = new Vector<String>();
    }

    public UniqueKeyConstraint(String name, String sourceField) {
        this();
        this.name = name;
        sourceFields.addElement(sourceField);
    }

    public UniqueKeyConstraint(String name, String[] sourceFields) {
        this();
        this.name = name;
        for(String sourceField : sourceFields) {
            this.sourceFields.addElement(sourceField);
        }
    }
    
    /**
     * PUBLIC:
     */
    public void addSourceField(String sourceField) {
        getSourceFields().addElement(sourceField);
    }

    /**
     * INTERNAL:
     * Append the database field definition string to the table creation statement.
     */
    public void appendDBString(Writer writer, AbstractSession session) {
        try {
            writer.write("UNIQUE (");
            for (Enumeration sourceEnum = getSourceFields().elements();
                     sourceEnum.hasMoreElements();) {
                writer.write((String)sourceEnum.nextElement());
                if (sourceEnum.hasMoreElements()) {
                    writer.write(", ");
                }
            }
            writer.write(")");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * PUBLIC:
     */
    public String getName() {
        return name;
    }

    /**
     * PUBLIC:
     */
    public Vector<String> getSourceFields() {
        return sourceFields;
    }

    /**
     * PUBLIC:
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * PUBLIC:
     */
    public void setSourceFields(Vector<String> sourceFields) {
        this.sourceFields = sourceFields;
    }
}

