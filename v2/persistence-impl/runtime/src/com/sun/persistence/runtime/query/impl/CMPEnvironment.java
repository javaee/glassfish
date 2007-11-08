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


package com.sun.persistence.runtime.query.impl;

import java.lang.reflect.Method;
import java.util.ResourceBundle;

import com.sun.jdo.spi.persistence.support.ejb.ejbc.MethodHelper;
import com.sun.persistence.utility.I18NHelper;

import com.sun.persistence.runtime.query.ParameterSupport;
import com.sun.persistence.runtime.query.QueryContext;

/**
 * An implementation of Environment for compilation of ejbql in an
 * EJB 2.x environment.
 *
 * @author Dave Bristor
 */
class CMPEnvironment extends EnvironmentImpl {
    /* QueryContext in which compilation takes place. */
    private final QueryContext queryContext;
        
    /** The Method instance of the finder/selector method. */
    private final Method method;
        
    /** result-type-mapping element from the DD. */
    private final int resultTypeMapping;
        
    /** Flag indicating finder or selector. */
    private final boolean finderNotSelector;
        
    /** The ejb name of the entity bean */
    private final String ejbName;
        
    private final CMPParameterSupport paramSupport;
        
    /** true if a select clause is for an aggregate function. */
    private boolean aggregate = false;
    
    /**
     * I18N support.
     */
    protected final static ResourceBundle msgs = I18NHelper.loadBundle(
            CMPEnvironment.class);
    
    CMPEnvironment(QueryContext queryContext, CMPParameterSupport paramSupport,
            Method method, int resultTypeMapping, boolean finderNotSelector,
            String ejbName) {
        this.queryContext = queryContext;
        this.method = method;
        this.paramSupport = paramSupport;
        this.resultTypeMapping = resultTypeMapping;
        this.finderNotSelector = finderNotSelector;
        this.ejbName = ejbName;
    }
        
    /* Implement Environment. */
    
    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#checkQueryResultType(java.lang.Object)
     */
    public void checkQueryResultType(Object selectClauseTypeInfo) {
        Class returnType = method.getReturnType();
        if (finderNotSelector) {
            checkFinderReturnType(returnType, selectClauseTypeInfo);
            checkFinderResultTypeMapping();
        } else {
            checkSelectorReturnType(returnType, selectClauseTypeInfo);
            checkSelectorResultTypeMapping(returnType,
                                           selectClauseTypeInfo);
        }
    }
        
    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#requiresDistinct()
     */
    public boolean requiresDistinct() {
        return !finderNotSelector
            && (method.getReturnType() == java.util.Set.class);
    }
        
    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#setAggregate(boolean)
     */
    public void setAggregate(boolean aggregate) {
        this.aggregate = aggregate;
    }

    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#analyseParameter(EJBQLASTImpl, Object)
     */
    public Object analyseParameter(EJBQLASTImpl paramAST, Object type) {
        Object rc = queryContext.getBooleanType();
        String ejbName = (String) type;
        
        if (semanticHelper.isInputParameter(paramAST)) {
            String paramName = paramAST.getText();
            String paramEjbName = paramSupport.getParameterKind(paramName);
            if (paramEjbName != null && !paramEjbName.equals(ejbName)) {
                ErrorMsg.error(paramAST.getLine(), paramAST.getColumn(),
                        I18NHelper.getMessage(msgs,
                                "EXC_MultipleEJBNameParameter", // NOI18N
                                paramName, ejbName, paramEjbName));
                rc = queryContext.getErrorType();
            } else {
                paramSupport.setParameterKind(paramName, ejbName);
            }
        }
        return rc;
    }

    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#getParameterType(String)
     */
    public Object getParameterType(String ejbqlParamDecl) {
        return paramSupport.getParameterType(ejbqlParamDecl);
    }
    
    /* Private implementation. */
    
    /**
     * Checks the return type and the type of the select clause expression
     * of a finder method.
     * <p>
     * The return type of a finder must be one of the following:
     * <ul>
     * <li>java.util.Collection (multi-object finder)
     * <li>java.util.Enumeration (EJB 1.1 multi-object finder)
     * <li>the entity bean's remote interface (single-object finder)
     * <li>the entity bean's local interface (single-object finder)
     * </ul>
     * The type of the select clause expression of a finder must be
     * the entity bean's local or remote interface.
     * @param returnType the return type of the finder/selector method object
     * @param selectClauseTypeInfo the type info of the select clause
     * expression.
     */
    private void checkFinderReturnType(
            Class returnType, Object selectClauseTypeInfo) {
        String selectClauseTypeName = queryContext.getTypeName(selectClauseTypeInfo);
        Object returnTypeInfo = queryContext.getTypeInfo(returnType);
        // The return type of a finder must be Collection or Enumeration or
        // the entity bean's remote or local interface
        if ((returnType != java.util.Collection.class) &&
            (returnType != java.util.Enumeration.class) &&
            (!queryContext.isRemoteInterfaceOfEjb(returnTypeInfo, ejbName)) &&
            (!queryContext.isLocalInterfaceOfEjb(returnTypeInfo, ejbName))) {
            ErrorMsg.error(I18NHelper.getMessage(msgs,
                                                 "EXC_InvalidFinderReturnType", returnType.getName())); //NOI18N
                
        }
            
        // The type of the select clause expression must be the ejb name
        // of this bean.
        if (!selectClauseTypeName.equals(this.ejbName)) {
            ErrorMsg.error(I18NHelper.getMessage(msgs,
                                                 "EXC_InvalidFinderSelectClauseType", selectClauseTypeName)); //NOI18N
        }
    }
        
    /**
     * Checks the result-type-mapping element setting in the case of a finder
     * method. Finder must not specify result-type-mapping.
     */
    private void checkFinderResultTypeMapping() {
        if (resultTypeMapping != MethodHelper.NO_RETURN) {
            ErrorMsg.error(I18NHelper.getMessage(msgs,
                                                 "EXC_InvalidResultTypeMappingForFinder")); //NOI18N
        }
    }
        
    /**
     * Checks the return type and the type of the select clause expression
     * of a selector method.
     * <p>
     * The return type of a selector must be one of the following:
     * <ul>
     * <li>java.util.Collection (multi-object selector)
     * <li>java.util.Set (multi-object selector)
     * <li>assignable from the type of the select clause expression
     * (single-object selector)
     * </ul>
     * @param returnType the return type of the finder/selector method object
     * @param selectClauseTypeInfo the type info of the select clause
     * expression.
     */
    private void checkSelectorReturnType(
            Class returnType, Object selectClauseTypeInfo) {
        String selectClauseTypeName = queryContext.getTypeName(selectClauseTypeInfo);
        Object returnTypeInfo = queryContext.getTypeInfo(returnType);
        // The return type of a selector must be Collection or Set or
        // assingable from the type of the select clause expression
        if ((returnType != java.util.Collection.class) &&
            (returnType != java.util.Set.class) &&
            !isCompatibleSelectorSelectorReturnType(returnTypeInfo,
                                                    selectClauseTypeInfo)) {
            ErrorMsg.error(I18NHelper.getMessage(msgs,
                                                 "EXC_InvalidSelectorReturnType", //NOI18N
                                                 queryContext.getTypeName(returnTypeInfo), selectClauseTypeName));
        }
    }
        
    /**
     * Checks the setting of the result-type-mapping element for a
     * selector. Only selectors returning a entity object may
     * specify this.
     * <p>
     * The method checks the following error cases:
     * <ul>
     * <li>result-type-mapping is specified as Remote,
     * but bean does not have remote interface
     * <li>result-type-mapping is specified as Local,
     * but bean does not have local interface
     * <li>single-object selector returns remote interface,
     * but result-type-mapping is not specified as Remote
     * <li>single-object selector returns local interface,
     * but result-type-mapping is specified as Remote
     * <li>result-type-mapping is specified for a selector returning
     * non-entity objects.
     * </ul>
     * @param returnType the return type of the finder/selector method object
     * @param selectClauseTypeInfo the type info of the select clause.
     */
    private void checkSelectorResultTypeMapping(
            Class returnType, Object selectClauseTypeInfo) {
        Object returnTypeInfo = queryContext.getTypeInfo(returnType);
            
        // case: multi-object selector returning entity objects
        if (queryContext.isCollectionType(returnTypeInfo) &&
            queryContext.isEjbName(selectClauseTypeInfo)) {
            if (resultTypeMapping == MethodHelper.REMOTE_RETURN) {
                // result-type-mapping is Remote =>
                // bean must have remote interface
                if (!queryContext.hasRemoteInterface(selectClauseTypeInfo)) {
                    ErrorMsg.error(I18NHelper.getMessage(msgs,
                                                         "EXC_InvalidRemoteResultTypeMappingForMultiSelector", //NOI18N
                                                         selectClauseTypeInfo));
                }
            } else {
                // result-type-mapping is Local or not specified =>
                // bean must have local interface
                if (!queryContext.hasLocalInterface(selectClauseTypeInfo)) {
                    ErrorMsg.error(I18NHelper.getMessage(msgs,
                                                         "EXC_InvalidLocalResultTypeMappingForMultiSelector", //NOI18N
                                                         selectClauseTypeInfo));
                }
            }
        }
        // case: single-object selector returning remote interface
        else if (queryContext.isRemoteInterface(returnTypeInfo)) {
            // result-type-mapping must be Remote
            if (resultTypeMapping != MethodHelper.REMOTE_RETURN) {
                ErrorMsg.error(I18NHelper.getMessage(msgs,
                                                     "EXC_InvalidLocalResultTypeMappingForSingleSelector")); //NOI18N
            }
        }
        // case: single-object selector returning local interface
        else if (queryContext.isLocalInterface(returnTypeInfo)) {
            // result-type-mapping must be Local or not specified
            if (resultTypeMapping == MethodHelper.REMOTE_RETURN) {
                ErrorMsg.error(I18NHelper.getMessage(msgs,
                                                     "EXC_InvalidRemoteResultTypeMappingForSingleSelector")); //NOI18N
            }
        }
        // cases: single-object and multi-object selector
        // returning non-enity object(s)
        else if (resultTypeMapping != MethodHelper.NO_RETURN) {
            // result-type-mapping must not be specified
            ErrorMsg.error(I18NHelper.getMessage(msgs,
                                                 "EXC_InvalidResultTypeMappingForSelector", //NOI18N
                                                 selectClauseTypeInfo));
        }
    }
        
    /**
     * Implements type compatibility for selector. The method returns
     * <code>true</code> if returnTypeInfo is compatible with
     * selectClauseTypeInfo.
     */
    private boolean isCompatibleSelectorSelectorReturnType(
            Object returnTypeInfo, Object selectClauseTypeInfo) {
        if (aggregate) {
            return queryContext.getCommonOperandType(
                    selectClauseTypeInfo, returnTypeInfo) != queryContext.getErrorType();
        } else {
            return queryContext.isCompatibleWith(selectClauseTypeInfo, returnTypeInfo);
        }
    }
}
