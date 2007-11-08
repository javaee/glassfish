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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oracle.toplink.essentials.exceptions.QueryException;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.mappings.OneToOneMapping;
import oracle.toplink.essentials.sessions.DatabaseRecord;

/**
 * <p><b>Purpose</b>:
 * Concrete class to represent the EntityResult structure as defined by
 * the EJB 3.0 Persistence specification.  This class is a subcompent of the 
 * SQLResultSetMapping
 * 
 * @see SQLResultSetMapping
 * @author Gordon Yorke
 * @since TopLink Java Essentials
 */

public class EntityResult extends SQLResult {
    /** Stores the class name of result  */
    protected String entityClassName;
    protected Class entityClass;
    
    /** Stores the list of FieldResult */
    protected Map fieldResults;
    
    /** Stores the column that will contain the value to determine the correct subclass
     * to create if applicable.
     */
    protected String discriminatorColumn;
    
    public EntityResult(Class entityClass){
        this.entityClass = entityClass;
        if (this.entityClass == null){
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_value_for_entity_result"));
        }
    }
    
    public EntityResult(String entityClassName){
        this.entityClassName = entityClassName;
        if (this.entityClassName == null){
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_value_for_entity_result"));
        }
    }
    
    public void addFieldResult(FieldResult fieldResult){
        if (fieldResult == null || fieldResult.getAttributeName() == null){
            return;
        }
        FieldResult existingFieldResult = (FieldResult)getFieldResults().get(fieldResult.getAttributeName());
        if (existingFieldResult==null){
            getFieldResults().put(fieldResult.getAttributeName(), fieldResult);
        }else{
            existingFieldResult.add(fieldResult);
        }
    }
    
    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this query to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);
        Class entityClass = null;
        try{
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    entityClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(entityClassName, true, classLoader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.classNotFoundWhileConvertingClassNames(entityClassName, exception.getException());
                }
            } else {
                entityClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(entityClassName, true, classLoader);
            }
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(entityClassName, exc);
        }
        this.entityClass = entityClass;
    };   

    /**
     * Accessor for the internally stored list of FieldResult.  Calling this
     * method will result in a collection being created to store the FieldResult
     */
    public Map getFieldResults(){
        if (this.fieldResults == null){
            this.fieldResults = new HashMap();
        }
        return this.fieldResults;
    }
    
    /**
     * Returns the column name for the column that will store the value used to
     * determine the subclass type if applicable.
     */
    public String getDiscriminatorColumn(){
        return this.discriminatorColumn;
    }

    /**
     * Sets the column name for the column that will store the value used to
     * determine the subclass type if applicable.
     */
    public void setDiscriminatorColumn(String column){
        if (column == null){
            return;
        }
        this.discriminatorColumn = column;
    }

    /**
     * INTERNAL:
     * This method is a convience method for extracting values from Results
     */
    public Object getValueFromRecord(DatabaseRecord record, ResultSetMappingQuery query){
        //from the row data build result entity.
        // To do this let's collect the column based data for this entity from
        // the results and call build object with this new row.
        ClassDescriptor descriptor = query.getSession().getDescriptor(this.entityClass);
        DatabaseRecord entityRecord = new DatabaseRecord(descriptor.getFields().size());
        if (descriptor.hasInheritance()){
            if (this.discriminatorColumn != null){
                Object value = record.get(this.discriminatorColumn);
                if (value == null){
                    throw QueryException.discriminatorColumnNotSelected(this.discriminatorColumn, query.getSQLResultSetMapping().getName());
                }
                entityRecord.put(descriptor.getInheritancePolicy().getClassIndicatorField(), record.get(this.discriminatorColumn));
            }else{
                entityRecord.put(descriptor.getInheritancePolicy().getClassIndicatorField(), record.get(descriptor.getInheritancePolicy().getClassIndicatorField()));
            }
            // if the descriptor uses inheritance and multiple types may have been read
            //get the correct descriptor.
            if (descriptor.hasInheritance() && descriptor.getInheritancePolicy().shouldReadSubclasses()) {
                Class classValue = descriptor.getInheritancePolicy().classFromRow(entityRecord, query.getSession());
                descriptor = query.getSession().getDescriptor(classValue);
            }
        }
        for (Iterator mappings = descriptor.getMappings().iterator(); mappings.hasNext();){
            DatabaseMapping mapping = (DatabaseMapping)mappings.next();
            FieldResult fieldResult = (FieldResult)this.getFieldResults().get(mapping.getAttributeName());
            if (fieldResult != null){
                if (mapping.getFields().size() == 1 ){
                    entityRecord.put(mapping.getFields().firstElement(), record.get(fieldResult.getColumnName()));
                }else if (mapping.getFields().size() >1){
                    getValueFromRecordForMapping(entityRecord,mapping,fieldResult,record);
                }
            }else{
                for (Iterator fields = mapping.getFields().iterator(); fields.hasNext();){
                    DatabaseField field = (DatabaseField)fields.next();
                    entityRecord.put(field, record.get(field));
                }
            }
        }
        query.setReferenceClass(this.entityClass);
        query.setDescriptor(descriptor);
        return descriptor.getObjectBuilder().buildObject(query, entityRecord, new JoinedAttributeManager(descriptor, (ExpressionBuilder)null, query));
    }

    public boolean isEntityResult(){
        return true;
    }
    
    /**
     * INTERNAL:
     *   This method is for processing all FieldResults for a mapping.  Adds DatabaseFields to the passed in entityRecord
     */
    public void getValueFromRecordForMapping(DatabaseRecord entityRecord,DatabaseMapping mapping, FieldResult fieldResult, DatabaseRecord databaseRecord){
        ClassDescriptor currentDescriptor = mapping.getReferenceDescriptor();
        /** check if this FieldResult contains any other FieldResults, process it if it doesn't */
        if (fieldResult.getFieldResults()==null){
            DatabaseField dbfield = processValueFromRecordForMapping(currentDescriptor,fieldResult.getMultipleFieldIdentifiers(),1);
            /** If it is a 1:1 mapping we need to do the target to source field conversion.  If it is an aggregate, it is fine as it is*/
            if (mapping.isOneToOneMapping()){
                dbfield = (DatabaseField)(((OneToOneMapping)mapping).getTargetToSourceKeyFields().get(dbfield));
            }
            entityRecord.put(dbfield, databaseRecord.get(fieldResult.getColumnName()));
            return;
        }
        /** This processes each FieldResult stored in the collection of FieldResults individually */
        Iterator fieldResults = fieldResult.getFieldResults().iterator();
        while (fieldResults.hasNext()){
            FieldResult tempFieldResult = ((FieldResult)fieldResults.next());
            DatabaseField dbfield = processValueFromRecordForMapping(currentDescriptor,tempFieldResult.getMultipleFieldIdentifiers(),1);
             if (mapping.isOneToOneMapping()){
                dbfield = (DatabaseField)(((OneToOneMapping)mapping).getTargetToSourceKeyFields().get(dbfield));
            }
            entityRecord.put(dbfield, databaseRecord.get(tempFieldResult.getColumnName()));
        }
    }
    
    /**
     * INTERNAL:
     *   This method is for processing a single FieldResult, returning the DatabaseField it refers to.
     */
    public DatabaseField processValueFromRecordForMapping(ClassDescriptor descriptor, String[] attributeNames, int currentLoc){
        DatabaseMapping mapping = descriptor.getMappingForAttributeName(attributeNames[currentLoc]);
        if (mapping==null){throw QueryException.mappingForFieldResultNotFound(attributeNames,currentLoc);}
        currentLoc++;
        if (attributeNames.length!=currentLoc){
            ClassDescriptor currentDescriptor = mapping.getReferenceDescriptor();
            DatabaseField df= processValueFromRecordForMapping(currentDescriptor, attributeNames, currentLoc);
            if (mapping.isOneToOneMapping()){
                return (DatabaseField)(((OneToOneMapping)mapping).getTargetToSourceKeyFields().get(df));
            }
            return df;
        }else{
            //this is it.. return this mapping's field
            return (DatabaseField) mapping.getFields().firstElement();
        }
    }
    
}
