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

import com.sun.persistence.utility.I18NHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to handle EJBQL query parameters.
 * 
 * @author Dave Bristor
 */
public class CMPParameterSupport extends ParameterSupportImpl {
    /**
     * The EJB names corresponding to types of parameters of the finder/selector
     * method.
     */
    private Map<String, String> parameterKinds = new HashMap<String, String>();
    
    /**
     * Constructor.
     * @param method the Method instance of the finder/selector method.
     */
    public CMPParameterSupport(Method method) {
        if (method != null) {
            Class paramTypes[] = method.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                super.setParameterType("?" + new Integer(i + 1).toString(), paramTypes[i]);
            }
        }
    }
    
    /**
     * @see com.sun.persistence.runtime.query.ParameterSupport#getParameterType(String)
     * @throws EJBQLException if parameter name is out of range.
     */
    public Object getParameterType(String ejbqlParamDecl) {
        Object rc = super.getParameterType(ejbqlParamDecl);
        if (rc == null) {
            ErrorMsg.error(
                I18NHelper.getMessage(
                    msgs, "EXC_InvalidParameterIndex", //NOI18N
                    ejbqlParamDecl,
                    String.valueOf(parameterTypes.size())));
        }       
        return rc;
    }

    /**
     * Does nothing; all CMP parameter types are known at construction
     * @see com.sun.persistence.runtime.query.ParameterSupport#setParameterType(String, Object)
     */
    public void setParameterType(String ejbqlParamDecl, Object type) {
        // empty
    }
    
    /**
     * Get EJB name corresponding to the EJBQL parameter by input parameter
     * declaration string.
     * @param ejbqlParamDecl denotes a parameter application in EJBQL. It has
     * the form "?<number>" where <number> is the parameter number starting with
     * 1.
     * @return class instance representing the parameter type.
     */
    public String getParameterKind(String ejbqlParamDecl) {
        String rc = parameterKinds.get(ejbqlParamDecl);
        if (rc == null) {
            ErrorMsg.error(
                I18NHelper.getMessage(
                    msgs, "EXC_InvalidParameterIndex", //NOI18N
                    ejbqlParamDecl,
                    String.valueOf(parameterTypes.size())));
        }
        return rc;
    }
    
    /**
     * Set EJB name corresponding to the EJBQL parameter by input parameter
     * declaration string.
     * @param ejbqlParamDecl denotes a parameter application in EJBQL. It has
     * the form "?<number>" where <number> is the parameter number starting with
     * 1.
     * @param ejbqlParamDecl parameter whose kind is being declared
     * @param kind kind of the parameter
     */
    public void setParameterKind(String ejbqlParamDecl, String kind) {
        parameterKinds.put(ejbqlParamDecl, kind);
    }
}
