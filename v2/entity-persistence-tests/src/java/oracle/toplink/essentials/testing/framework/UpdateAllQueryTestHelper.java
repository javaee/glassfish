/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  


package oracle.toplink.essentials.testing.framework;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.mappings.OneToOneMapping;
import oracle.toplink.essentials.queryframework.UpdateAllQuery;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.queryframework.ReportQueryResult;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.expressions.DataExpression;
import oracle.toplink.essentials.exceptions.QueryException;
 
public class UpdateAllQueryTestHelper {
            
    public static String execute(Session mainSession, Class referenceClass, HashMap updateClauses, Expression selectionExpression) {
        return execute(mainSession, referenceClass, updateClauses, selectionExpression, true);
    }

    public static String execute(Session mainSession, Class referenceClass, HashMap updateClauses, Expression selectionExpression, boolean handleChildren) {
        return execute(mainSession, createUpdateAllQuery(referenceClass, updateClauses, selectionExpression), true);
    }

    public static String execute(Session mainSession, UpdateAllQuery uq) {
        return execute(mainSession, uq,  true);
    }
    
    // Compares results of updating multiple objects yusing traditionalTopLink update one-by-one approach
    // with updating using UpdateAllQuery. If results differ retuns a non-null error message.
    // To use this method:
    // 1. Populate db with the objects on which update should be performed;
    // 2. In case handleChildren == true the similar query will also be tested with all subclasses:
    //      example: for uq.referenceClass()==Project, the test will also run for SmallProject and LargeProject.
    // 3. After the test is completed it leaves the db in the original state (uses rollbacks).
    public static String execute(Session mainSession, UpdateAllQuery uq,  boolean handleChildren) {
        // Find the inheritance root class - the test will compare all instances of this class
        // after traditional TopLink update (one-by-one) with all instances of this class
        // after UpdateAllQuery. The test succeeds if the two collections are equal.
        Class rootClass = uq.getReferenceClass();
        ClassDescriptor descriptor = mainSession.getClassDescriptor(uq.getReferenceClass());
        if(descriptor.hasInheritance()) {
            ClassDescriptor parentDescriptor = descriptor;
            while(!parentDescriptor.getInheritancePolicy().isRootParentDescriptor()) {
                parentDescriptor = (ClassDescriptor) parentDescriptor.getInheritancePolicy().getParentDescriptor();
            }
            rootClass = parentDescriptor.getJavaClass();
        }
                
        String errorMsg = execute(mainSession, uq, handleChildren, rootClass);

        if(errorMsg.length() == 0) {
            return null;
        } else {
            return errorMsg;
        }
    }
    
    protected static String execute(Session mainSession, UpdateAllQuery uq, boolean handleChildren,
                                  Class rootClass) {
        String errorMsg = "";
        ClassDescriptor descriptor = mainSession.getDescriptor(uq.getReferenceClass());
        
        clearCache(mainSession);
        
        // original objects
        Vector objects = mainSession.readAllObjects(rootClass);
        
        // first update using the original TopLink approach - one by one.
        // That will be done using report query - it will use the same selection criteria
        // as UpdateAllQuery and each attribute will correspond to an update item.
        ReportQuery rq = new ReportQuery(uq.getReferenceClass(), uq.getExpressionBuilder());    
        rq.setSelectionCriteria(uq.getSelectionCriteria());
        rq.setShouldRetrievePrimaryKeys(true);
        // some db platforms don't allow nulls in select clause - so add the fields with null values to the query result.
        Vector fieldsWithNullValues = new Vector();
        Iterator itEntrySets = uq.getUpdateClauses().entrySet().iterator();
        while(itEntrySets.hasNext()) {
            Map.Entry entry = (Map.Entry)itEntrySets.next();
            Expression valueExpression;
            String keyString = getQualifiedFieldNameFromKey(entry.getKey(), rq.getReferenceClass(), descriptor, mainSession);
            Object value = entry.getValue();
            DatabaseMapping mapping = descriptor.getObjectBuilder().getMappingForField(new DatabaseField(keyString));
            if(mapping != null && mapping.isOneToOneMapping() && value != null) {
                // Note that this only works in case the reference PK is not compound
                if(((OneToOneMapping)mapping).getSourceToTargetKeyFields().size() > 1) {
                    errorMsg = "Attribute "+ mapping.getAttributeName() + " mapped with 1to1 mapping that has more than one targetKeyField. UpdateAllQueryTestHelper currently doesn't support that.";
                }
                DatabaseField targetField = (DatabaseField)((OneToOneMapping)mapping).getSourceToTargetKeyFields().get(new DatabaseField(keyString));
                if(value instanceof Expression) {
                    valueExpression = ((Expression)(((Expression)value).clone())).getField(targetField);
                } else {
                    ClassDescriptor targetDescriptor = ((OneToOneMapping)mapping).getReferenceDescriptor();
                    Object fieldValue = targetDescriptor.getObjectBuilder().extractValueFromObjectForField(value, targetField, (oracle.toplink.essentials.internal.sessions.AbstractSession)mainSession);
                    valueExpression = rq.getExpressionBuilder().value(fieldValue);
                }
            } else {
                if(value instanceof Expression) {
                    valueExpression = (Expression)value;
                } else {
                    valueExpression = rq.getExpressionBuilder().value(value);
                }
            }
            if(value == null) {
                fieldsWithNullValues.add(keyString);
            } else {
                rq.addAttribute(keyString, valueExpression);
            }
        }
        
        UnitOfWork uow = mainSession.acquireUnitOfWork();
        // mainSession could be a ServerSession
        Session session = uow.getParent();
        
        // report query results contain the values to be assigned for each object to be updated.
        Vector result = (Vector)session.executeQuery(rq);
        Vector objectsAfterOneByOneUpdate = new Vector(objects.size());
        ((oracle.toplink.essentials.internal.sessions.AbstractSession)session).beginTransaction();
        try {
            for(int i=0; i < result.size(); i++) {
                // read through uow the object(clone) to be updated
                ReportQueryResult reportResult = (ReportQueryResult)result.elementAt(i);
                // hammer into the object the updated values
                Object obj = reportResult.readObject(rq.getReferenceClass(), uow);
                DatabaseRecord row = new DatabaseRecord();
                for(int j=0; j < reportResult.getNames().size(); j++) {
                    String name = (String)reportResult.getNames().elementAt(j);
                    DatabaseField field = new DatabaseField(name);
                    Object value = reportResult.getResults().elementAt(j);
                    row.add(field, value);            
                }
                // some db platforms don't allow nulls in select clause - so add the fields with null values to the query result
                for(int j=0; j < fieldsWithNullValues.size(); j++) {
                    String name = (String)fieldsWithNullValues.elementAt(j);
                    DatabaseField field = new DatabaseField(name);
                    row.add(field, null);
                }
                rq.getDescriptor().getObjectBuilder().assignReturnRow(obj, (AbstractSession)uow, row);
            }
            // uow committed - objects updated.
            uow.commit();
    
            // Because the transaction will be rolled back (to return to the original state to execute UpdateAllQuery)
            // objects are copied into another vector - later it will be compared with UpdateAllQuery result.
            for(int i=0; i < objects.size(); i++) {
                Object original = objects.elementAt(i);
                Object copy = buildCopy(descriptor, original, uow);
                objectsAfterOneByOneUpdate.add(copy);
            }
        } finally {
            // transaction rolled back - objects back to the original state in the db.
            ((oracle.toplink.essentials.internal.sessions.AbstractSession)session).rollbackTransaction();
        }
        clearCache(mainSession);

        // now use UpdateAllQuery
        uow = mainSession.acquireUnitOfWork();
        // mainSession could be a ServerSession
        session = uow.getParent();        
        Vector objectsAfterUpdateAll = new Vector(objects.size());
        ((oracle.toplink.essentials.internal.sessions.AbstractSession)session).beginTransaction();
        try {
            uow.executeQuery(uq);
            // uow committed - objects updated.
            uow.commit();
    
            // Because the transaction will be rolled back (to return to the original state)
            // objects are copied into another vector - it will be compared with update one-by-one result.
            for(int i=0; i < objects.size(); i++) {
                Object original = objects.elementAt(i);
                Object copy = buildCopy(descriptor, original, uow);
                objectsAfterUpdateAll.add(copy);
            }
        } finally {
            // transaction rolled back - objects back to the original state in the db.
            ((oracle.toplink.essentials.internal.sessions.AbstractSession)session).rollbackTransaction();
        }
        clearCache(mainSession);
        
        // verify
        String classErrorMsg = "";
        for(int i=0; i < objects.size(); i++) {
            Object obj = objects.elementAt(i);
            Object obj1 = objectsAfterOneByOneUpdate.elementAt(i);
            Object obj2 = objectsAfterUpdateAll.elementAt(i);
            boolean equal = rq.getDescriptor().getObjectBuilder().compareObjects(obj, obj2, (oracle.toplink.essentials.internal.sessions.AbstractSession)session);
            if(!equal) {
                classErrorMsg = classErrorMsg + "Difference: original = " + obj.toString() + "; afterOneByOneUpdate = " + obj1.toString() +"; afterUpdateAll = " + obj2.toString() + ";";
            }
        }
        if(classErrorMsg.length() > 0) {
            errorMsg = errorMsg + classErrorMsg;
        }

        if(handleChildren) {
            if(descriptor.hasInheritance() && descriptor.getInheritancePolicy().hasChildren()) {
                Iterator it = descriptor.getInheritancePolicy().getChildDescriptors().iterator();
                while(it.hasNext()) {
                    ClassDescriptor childDescriptor = (ClassDescriptor)it.next();
                    Class childReferenceClass = childDescriptor.getJavaClass();
                    UpdateAllQuery childUq = (UpdateAllQuery)uq.clone();
                    childUq.setReferenceClass(childReferenceClass);
                    childUq.setIsPrepared(false);
                    errorMsg += execute(mainSession, childUq, handleChildren, rootClass);
                }
            }
        }
        return errorMsg;
    }
    
    protected static void clearCache(Session mainSession) {
        mainSession.getIdentityMapAccessor().initializeAllIdentityMaps();
    }
    
    public static UpdateAllQuery createUpdateAllQuery(Class referenceClass, HashMap updateClauses, Expression selectionExpression) {
        // Construct UpdateAllQuery
        UpdateAllQuery uq = new UpdateAllQuery(referenceClass, selectionExpression);
        Iterator itEntrySets = updateClauses.entrySet().iterator();
        while(itEntrySets.hasNext()) {
            Map.Entry entry = (Map.Entry)itEntrySets.next();
            uq.addUpdate((String)entry.getKey(), entry.getValue());
        }
        return uq;
    }
    
    static protected Object buildCopy(ClassDescriptor descriptor, Object original, UnitOfWork uow) {
        Object copy = descriptor.getCopyPolicy().buildClone(original, (oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl)uow);
        descriptor.getObjectBuilder().copyInto(original, copy, true);
        return copy;
    }

    static protected String getQualifiedFieldNameFromKey(Object key, Class referenceClass, ClassDescriptor descriptor, Session session) {
        DatabaseField field = null;
        if(key instanceof String) {
            // attribute name
            String name = (String)key;
            DatabaseMapping mapping = descriptor.getObjectBuilder().getMappingForAttributeName(name);
            if(mapping != null) {
                field = (DatabaseField)mapping.getFields().firstElement();
            }
        } else if(key instanceof DataExpression) {
            DataExpression fieldExpression = (DataExpression)key;
            field = descriptor.getObjectBuilder().getFieldForQueryKeyName(fieldExpression.getName());
            if(field == null) {
                DataExpression fieldExpressionClone = (DataExpression)fieldExpression.clone();
                fieldExpressionClone.getBuilder().setQueryClass(referenceClass);
                fieldExpressionClone.getBuilder().setSession((oracle.toplink.essentials.internal.sessions.AbstractSession)session);
                field = fieldExpressionClone.getField();
            }
        }
        
        if(field != null) {
            return field.getQualifiedName();
        }
        
        // should never happen
        return null;
    }
}
