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


package com.sun.persistence.runtime.query;

/**
 * Handles the typing of names given in an EJBQL query string.
 * 
 * @author Dave Bristor
 */
public interface ParameterSupport {
    /**
     * Returns type of the EJBQL parameter by input parameter declaration
     * string. The specified string denotes a parameter application in EJBQL. It
     * has the form "?<number>" where <number> is the parameter number starting
     * with 1, or in the ":<name>" form, where <name> is a valid parameter name.
     * @return class instance representing the parameter type.
     */
    public Object getParameterType(String ejbqlParamDecl);
    
    /**
     * Associates a type of the given parameter name
     * @param paramName name of a parameter 
     * @param type type of the parameter
     */
    public void setParameterType(String ejbqlParamDecl, Object type);
    
    /**
     * Indicates whether the given parameter is the same kind (named or
     * positional) as other parameters set on this
     * <code>ParameterSupport</code> instance.
     * @param ejbqlParamDecl EJB QL parameter such as <code>?1</code>
     * or <code>:name</code>
     * @return true if the given <code>ejbqlParamDecl</code> is suitable
     * with others set so far on this <code>ParameterSupport</code>, false
     * otherwise.  Here "suitable" means that either positional or named
     * parameters can be used, but not both.
     */
    boolean isSuitableParameter(String ejbqlParamDecl);
    
    /**
     * @return true if given string is the name of a named parameter, i.e.,
     * it starts with ':'.
     * @param ptext parameter name
     * @return true if <code>ptext</code> is a named parameter, false otherwise.
     */
    public boolean isNamedParameter(String ptext);
}
