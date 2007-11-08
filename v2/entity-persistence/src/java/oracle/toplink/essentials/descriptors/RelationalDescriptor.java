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
package oracle.toplink.essentials.descriptors;

import java.util.*;
import oracle.toplink.essentials.exceptions.*;

/**
 * <p><b>Purpose</b>: TopLink has been designed to take advantage of the similarities between
 * relational databases and objects while accommodating for their differences, providing an object
 * oriented wrapper for relational databases. This is accomplished through the use of Descriptors.
 * A descriptor is a pure specification class with all its behaviour deputized to DescriptorEventManager,
 * DescriptorQueryManager and ObjectBuilder. Look at the following variables for the list
 * of specification on the descriptor.
 * <p>
 * A Descriptor is a set of mappings that describe how an objects's data is to be represented in a
 * relational database. It contains mappings from the class instance variables to the table's fields,
 * as well as the transformation routines necessary for storing and retrieving attributes. As such
 * the descriptor acts as the link between the Java object and its database representaiton.
 * <p>
 * Every descripor is initialized with the following information:
 * <ul>
 * <li> The Java class its describes, and the corresponding table(s) for storing instances of the class.
 * <li> The primary key of the table.
 * <li> A list of query keys for field names.
 * <li> A description of the objects's attributes and relationships. This information is stored in mappings.
 * <li> A set of user selectable properties for tailoring the behaviour of the descriptor.
 * </ul>
 *
 * <p> This descriptor subclass should be used for object-relational mapping,
 * and allows for other datatype mappings to be done in the XML, EIS and OR sibling classes.
 *
 * @see DescriptorEventManager
 * @see DescriptorQueryManager
 * @see InheritancePolicy
 * @see InterfacePolicy
 */
public class RelationalDescriptor extends ClassDescriptor {

    /**
     * PUBLIC:
     * Return a new descriptor.
     */
    public RelationalDescriptor() {
        super();
    }

    /**
     * PUBLIC:
     * Specify the table name for the class of objects the receiver describes.
     * If the table has a qualifier it should be specified using the dot notation,
     * (i.e. "userid.employee"). This method is used if there is more than one table.
     */
    public void addTableName(String tableName) {
        super.addTableName(tableName);
    }

    /**
     * PUBLIC:
     * Return the name of the descriptor's first table.
     * This method must only be called on single table descriptors.
     */
    public String getTableName() {
        return super.getTableName();
    }

    /**
     * PUBLIC:
     * Return the table names.
     */
    public Vector getTableNames() {
        return super.getTableNames();
    }

    /**
     * PUBLIC:
     * The descriptors default table can be configured if the first table is not desired.
     */
    public void setDefaultTableName(String defaultTableName) {
        super.setDefaultTableName(defaultTableName);
    }

    /**
     * PUBLIC:
     * Specify the table name for the class of objects the receiver describes.
     * If the table has a qualifier it should be specified using the dot notation,
     * (i.e. "userid.employee"). This method is used for single table.
     */
    public void setTableName(String tableName) throws DescriptorException {
        super.setTableName(tableName);
    }

    /**
     * PUBLIC:
     * Specify the all table names for the class of objects the receiver describes.
     * If the table has a qualifier it should be specified using the dot notation,
     * (i.e. "userid.employee"). This method is used for multiple tables
     */
    public void setTableNames(Vector tableNames) {
        super.setTableNames(tableNames);
    }

    /**
     * PUBLIC: Set the table Qualifier for this descriptor.  This table creator will be used for
     * all tables in this descriptor
     */
    public void setTableQualifier(String tableQualifier) {
        super.setTableQualifier(tableQualifier);
    }
}
