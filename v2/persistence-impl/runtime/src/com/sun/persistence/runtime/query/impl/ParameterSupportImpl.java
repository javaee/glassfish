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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


import com.sun.persistence.runtime.query.ParameterSupport;
import com.sun.persistence.utility.I18NHelper;

/**
 * Handles the typing of names in an EJBQL query.  Subclasses must provide a mean
 * to set parameter types.
 * 
 * @author Dave Bristor
 */
abstract class ParameterSupportImpl implements ParameterSupport {
    /**
     * If true, then the EJB QL of the QueryImpl that owns this instance
     * has one or more named parameters.  Default is false.
     */
    private boolean hasNamedParameters = false;
    
    /**
     * The types of the parameters.  Subclass methods will access this.
     */
    protected Map<String, Object> parameterTypes =
        new HashMap<String, Object>();

    /**
     * I18N support.
     */
    protected final static ResourceBundle msgs =
        I18NHelper.loadBundle(CMPParameterSupport.class);

    ParameterSupportImpl() {
        // empty
    }
    
    /* Implement ParameterSupport */
    
    /**
     * Returns null if parameter does not have type.
     * @see com.sun.persistence.runtime.query.ParameterSupport#getParameterType(String)
     */
    public Object getParameterType(String ejbqlParamDecl) {
        return parameterTypes.get(ejbqlParamDecl);
    }
    
    /**
     * @see com.sun.persistence.runtime.query.ParameterSupport#setParameterType(String, Object)
     */
    public void setParameterType(String ejbqlParamDecl, Object type) {
        // Darned IDEs don't seem to have good supoprt for enabling
        // assertions, and the test for this class expects to see the
        // assertion.  So throw it instead of using Java's "assert".        
        if (!isSuitableParameter(ejbqlParamDecl)) {
            throw new AssertionError(
                "Internal error in PersistenceParameterSupport.setParameterType: " // NOI18N
                + "attempt to mix named and positional parameters; given param is " // NOI18N
                + ejbqlParamDecl);
        }

        parameterTypes.put(ejbqlParamDecl, type);
        if (ejbqlParamDecl.startsWith(":")) {
            hasNamedParameters = true;
        }
    }
    
    
    /**
     * @see com.sun.persistence.runtime.query.ParameterSupport#isSuitableParameter(String)
     */
     public boolean isSuitableParameter(String ptext) {
         assert ptext.length() > 0
             : "Internal error: in PersistenceParameterSupport.isSuitableParameter: "
             + "given parameter has length=0";

         return isNamedParameter(ptext)
             ? isOKToHaveNamedParameters()
             : isOKToHavePositionalParameters();
    }

     /**
      * @see com.sun.persistence.runtime.query.ParameterSupport#isNamedParameter(String)
      */
     public boolean isNamedParameter(String ptext) {
         return ptext.startsWith(":");
     }
     
     /** @return true if no named parameters are set. */
     private boolean isOKToHavePositionalParameters() {
         // This is true
         return parameterTypes.isEmpty() || !hasNamedParameters;
     }
     
     /** @return true if any named parameters are set. */
     private boolean isOKToHaveNamedParameters() {
         return parameterTypes.isEmpty() || hasNamedParameters;
     }
}
