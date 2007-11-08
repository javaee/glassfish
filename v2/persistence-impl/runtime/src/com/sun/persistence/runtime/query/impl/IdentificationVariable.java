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
 * An instance of this class denotes an identification variable as declared in
 * the from clause of an EJBQL query string. The compiler creates such an
 * instance when analysing the from clause and stores it in the symbol table.
 * @author Michael Bouschen
 */
public class IdentificationVariable {
    /**
     * The name of the identification variable.
     */
    private String name;

    /**
     * The type of the identification variable.
     */
    private Object typeInfo;

    /**
     * Creates an identification variable declaration for use during semantic
     * analysis.
     * @param name the name of the identification variable.
     * @param typeInfo the type of the identification variable.
     */
    public IdentificationVariable(String name, Object typeInfo) {
        this.name = name;
        this.typeInfo = typeInfo;
    }

    /**
     * Returns the name of the IdentificationVariable.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the IdentificationVariable.
     * @return the type
     */
    public Object getTypeInfo() {
        return typeInfo;
    }

}
