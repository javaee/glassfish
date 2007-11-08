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

/**
 * Represents the environment in which compilation is happening.
 * There should be implementations for EJB 2.x and 3.x.
 *
 * @author Dave Bristor
 */
interface Environment {
    /**
     * Checks that the return type of the query is appropriate for the
     * environment in which the query is compiled.
     * @param selectClauseTypeInfo type of result calculated from EJBQL query
     * text.
     */
    public void checkQueryResultType(Object selectClauseTypeInfo);
    
    /**
     * @return true if the environment requires that the query return a
     * distinct result, false otherwise.
     */
    public boolean requiresDistinct();
    
    /**
     * Indicates to the environment that the query is an aggregate query.
     * @param aggregate
     */
    public void setAggregate(boolean aggregate);
	
    /**
     * Analyses whether paramAST can be associated to a typeName.
     * @param paramAST AST node corresponds to a PARAMETER
     * @param type type to be check against type of paramAST
     * @return the type info of the given <code>type</code> or queryContext.errorType
     */
	public Object analyseParameter(EJBQLASTImpl paramAST, Object type);
	
    /**
     * Returns type of the EJBQL parameter by input parameter declaration
     * string. The specified string denotes a parameter application in EJBQL. It
     * has the form "?<number>" where <number> is the parameter number starting
     * with 1.
     * @return class instance representing the parameter type.
     */
	public Object getParameterType(String ejbqlParamDecl);

    /**
     *
     */
    public void setSemanticHelper(SemanticHelper helper);
}
