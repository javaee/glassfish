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
package oracle.toplink.essentials.internal.sessions;

import java.io.*;
import java.util.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.sessions.Record;
import oracle.toplink.essentials.internal.helper.DatabaseField;

/**
 * <p>
 * <b>Purpose</b>: Define the abstract definition of a record for internal use.
 * Public API should reference the Record interface.
 * Subclasses are DatabaseRecord and XMLRecord.
 * <p>
 * <b>Responsibilities</b>: <ul>
 *        <li> Implement the Record and Map interfaces.
 * </ul>
 * @see DatabaseField
 */
public abstract class AbstractRecord implements Record, Cloneable, Serializable, Map {

    /** Use vector to store the fields/values for optimal performance.*/
    protected Vector fields;

    /** Use vector to store the fields/values for optimal performance.*/
    protected Vector values;

    /** Optimize field creation for field name lookup. */
    protected DatabaseField lookupField;

    /** INTERNAL: indicator showing that no entry exists for a given key. */
    public static final AbstractRecord.NoEntry noEntry = new AbstractRecord.NoEntry();

    /**
     * INTERNAL:
     * NoEntry: This is used to differentiate between the two kinds
     * of nulls: no entry exists, and the field is actually mapped
     * to null.
     */
    public static class NoEntry {
        private NoEntry() {
        }
    }

    /**
     * INTERNAL:
     * TopLink converts JDBC results to collections of rows.
     */
    public AbstractRecord() {
        this.fields = new Vector();
        this.values = new Vector();
    }

    /**
     * INTERNAL:
     * TopLink converts JDBC results to collections of rows.
     */
    public AbstractRecord(int initialCapacity) {
        this.fields = new Vector(initialCapacity);
        this.values = new Vector(initialCapacity);
    }

    /**
     * INTERNAL:
     * TopLink converts JDBC results to collections of rows.
     */
    public AbstractRecord(Vector fields, Vector values) {
        this.fields = fields;
        this.values = values;
    }

    /**
     * INTERNAL:
     * Add the field-value pair to the row.  Will not check,
     * will simply add to the end of the row
     */
    public void add(DatabaseField key, Object value) {
        getFields().addElement(key);
        getValues().addElement(value);
    }

    /**
     * PUBLIC:
     * Clear the contents of the row.
     */
    public void clear() {
        this.fields = new Vector();
        this.values = new Vector();
    }

    /**
     * INTERNAL:
     * Clone the row and its values.
     */
    public Object clone() {
        try {
            AbstractRecord clone = (AbstractRecord)super.clone();
            clone.setFields((Vector)getFields().clone());
            clone.setValues((Vector)getValues().clone());
            return clone;
        } catch (CloneNotSupportedException exception) {
        }

        return null;
    }

    /**
     * PUBLIC:
     * Check if the value is contained in the row.
     */
    public boolean contains(Object value) {
        return containsValue(value);
    }

    /**
     * PUBLIC:
     * Check if the field is contained in the row.
     * Conform to hashtable interface.
     */
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            return containsKey((String)key);
        }
        if (key instanceof DatabaseField) {
            return containsKey((DatabaseField)key);
        }

        return false;
    }

    /**
     * PUBLIC:
     * Check if the field is contained in the row.
     */
    public boolean containsKey(String fieldName) {
        // Optimized the field creation.
        if (this.lookupField == null) {
            this.lookupField = new DatabaseField(fieldName);
        } else {
            this.lookupField.resetQualifiedName(fieldName);
        }
        return containsKey(this.lookupField);
    }

    /**
     * INTERNAL:
     * Check if the field is contained in the row.
     */
    public boolean containsKey(DatabaseField key) {
        // Optimize check.
        int index = key.getIndex();
        if ((index >= 0) && (index < getFields().size())) {
            DatabaseField field = (DatabaseField)getFields().elementAt(index);
            if ((field == key) || field.equals(key)) {
                return true;
            }
        }
        return getFields().contains(key);
    }

    /**
     * PUBLIC:
     * Check if the value is contained in the row.
     */
    public boolean containsValue(Object value) {
        return getValues().contains(value);
    }

    /**
     * PUBLIC:
     * Returns an Enumeration of the values.
     */
    public Enumeration elements() {
        return getValues().elements();
    }

    /**
     * PUBLIC:
     * Returns a set of the keys.
     */
    public Set entrySet() {
        int size = this.size();
        Map tempMap = new HashMap(size);
        for (int i = 0; i < size; i++) {
            tempMap.put(this.getFields().elementAt(i), this.getValues().elementAt(i));
        }
        return tempMap.entrySet();
    }

    /**
     * PUBLIC:
     * Retrieve the value for the field name.
     * A field is constructed on the name to check the hash table.
     * If missing null is returned.
     */
    public Object get(Object key) {
        if (key instanceof String) {
            return get((String)key);
        } else if (key instanceof DatabaseField) {
            return get((DatabaseField)key);
        }
        return null;
    }

    /**
     * PUBLIC:
     * Retrieve the value for the field name.
     * A field is constructed on the name to check the hash table.
     * If missing null is returned.
     */
    public Object get(String fieldName) {
        Object value = getIndicatingNoEntry(fieldName);
        if (value == oracle.toplink.essentials.internal.sessions.AbstractRecord.noEntry) {
            return null;
        }
        return value;
    }

    /**
     * PUBLIC:
     * Retrieve the value for the field name.
     * A field is constructed on the name to check the hash table.
     * If missing DatabaseRow.noEntry is returned.
     */
    public Object getIndicatingNoEntry(String fieldName) {
        // Optimized the field creation.
        if (this.lookupField == null) {
            this.lookupField = new DatabaseField(fieldName);
        } else {
            this.lookupField.resetQualifiedName(fieldName);
        }
        return getIndicatingNoEntry(this.lookupField);
    }

    /**
     * INTERNAL:
     * Retrieve the value for the field. If missing null is returned.
     */
    public Object get(DatabaseField key) {
        Object value = getIndicatingNoEntry(key);
        if (value == oracle.toplink.essentials.internal.sessions.AbstractRecord.noEntry) {
            return null;
        }
        return value;
    }

    //----------------------------------------------------------------------------//
    public Object getValues(DatabaseField key) {
        return get(key);
    }

    public Object getValues(String key) {
        return get(key);
    }

    //----------------------------------------------------------------------------//

    /**
     * INTERNAL:
     * Retrieve the value for the field. If missing DatabaseRow.noEntry is returned.
     */
    public Object getIndicatingNoEntry(DatabaseField key) {
        // PERF: Direct variable access.
        // Optimize check.
        int index = key.getIndex();
        if ((index >= 0) && (index < this.fields.size())) {
            DatabaseField field = (DatabaseField)this.fields.elementAt(index);
            if ((field == key) || field.equals(key)) {
                return this.values.elementAt(index);
            }
        }
        index = this.fields.indexOf(key);
        if (index >= 0) {
            // PERF: If the fields index was not set, then set it.
            if (key.getIndex() == -1) {
                key.setIndex(index);
            }
            return this.values.elementAt(index);
        } else {
            return oracle.toplink.essentials.internal.sessions.AbstractRecord.noEntry;
        }
    }

    /**
     * INTERNAL:
     * Returns the row's field with the same name.
     */
    public DatabaseField getField(DatabaseField key) {
        // Optimize check.
        int index = key.getIndex();
        if ((index >= 0) && (index < getFields().size())) {
            DatabaseField field = (DatabaseField)getFields().elementAt(index);
            if ((field == key) || field.equals(key)) {
                return field;
            }
        }
        for (index = 0; index < getFields().size(); index++) {
            DatabaseField field = (DatabaseField)getFields().elementAt(index);
            if ((field == key) || field.equals(key)) {
                return field;
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     */
    public Vector getFields() {
        return fields;
    }

    /**
     * INTERNAL:
     */
    public Vector getValues() {
        return values;
    }

    /**
     * PUBLIC:
     * Return if the row is empty.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * PUBLIC:
     * Returns an Enumeration of the DatabaseField objects.
     */
    public Enumeration keys() {
        return getFields().elements();
    }

    /**
     * PUBLIC:
     * Returns a set of the keys.
     */
    public Set keySet() {
        return new HashSet(getFields());
    }

    /**
     * INTERNAL:
     * Merge the provided row into this row.  Existing field values in this row will
     * be replaced with values from the provided row. Fields not in this row will be 
     * added from provided row.  Values not in provided row will remain in this row.
     */
    
    public void mergeFrom(AbstractRecord row){
        for (int index = 0; index < row.size(); ++index){
            this.put(row.getFields().get(index), row.getValues().get(index));
        }
    }
    
    /**
     * PUBLIC:
     * Add the field-value pair to the row.
     */
    public Object put(Object key, Object value) throws ValidationException {
        if (key instanceof String) {
            return put((String)key, value);
        } else if (key instanceof DatabaseField) {
            return put((DatabaseField)key, value);
        } else {
            throw ValidationException.onlyFieldsAreValidKeysForDatabaseRows();
        }
    }

    /**
     * PUBLIC:
     * Add the field-value pair to the row.
     */
    public Object put(String key, Object value) {
        return put(new DatabaseField(key), value);
    }

    /**
     * INTERNAL:
     * Add the field-value pair to the row.
     */
    public Object put(DatabaseField key, Object value) {
        int index = getFields().indexOf(key);
        if (index >= 0) {
            Object oldValue = getValues().elementAt(index);
            replaceAt(value, index);
            return oldValue;
        } else {
            add(key, value);
        }

        return null;
    }

    /**
     * PUBLIC:
     * Add all of the elements.
     */
    public void putAll(Map map) {
        Iterator entriesIterator = map.entrySet().iterator();
        while (entriesIterator.hasNext()) {
            Map.Entry entry = (Map.Entry)entriesIterator.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * INTERNAL:
     * Remove the field key from the row.
     */
    public Object remove(Object key) {
        if (key instanceof String) {
            return remove((String)key);
        } else if (key instanceof DatabaseField) {
            return remove((DatabaseField)key);
        }
        return null;
    }

    /**
     * INTERNAL:
     * Remove the field key from the row.
     */
    public Object remove(String fieldName) {
        return remove(new DatabaseField(fieldName));
    }

    /**
     * INTERNAL:
     * Remove the field key from the row.
     */
    public Object remove(DatabaseField key) {
        int index = getFields().indexOf(key);
        if (index >= 0) {
            getFields().removeElementAt(index);
            Object value = getValues().elementAt(index);
            getValues().removeElementAt(index);
            return value;
        }
        return null;
    }

    /**
     * INTERNAL:
     * replaces the value at index with value
     */
    public void replaceAt(Object value, int index) {
        getValues().setElementAt(value, index);
    }

    protected void setFields(Vector fields) {
        this.fields = fields;
    }

    protected void setValues(Vector values) {
        this.values = values;
    }

    /**
     * PUBLIC:
     * Return the number of field/value pairs in the row.
     */
    public int size() {
        return getFields().size();
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        StringWriter writer = new StringWriter();
        writer.write(Helper.getShortClassName(getClass()));
        writer.write("(");

        for (int index = 0; index < getFields().size(); index++) {
            writer.write(Helper.cr());
            writer.write("\t");
            writer.write(String.valueOf((getFields().elementAt(index))));
            writer.write(" => ");
            writer.write(String.valueOf((getValues().elementAt(index))));
        }
        writer.write(")");

        return writer.toString();
    }

    /**
     * PUBLIC:
     * Returns an collection of the values.
     */
    public Collection values() {
        return getValues();
    }
}
