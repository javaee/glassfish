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



/*
 * SymbolTable.java
 *
 * Created on November 19, 2001
 */


package com.sun.persistence.runtime.query.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * The symbol table handling declared identifies.
 * @author Michael Bouschen
 */
public class SymbolTable {
    /**
     * The table of declared identifier (symbols).
     */
    protected Map symbols = new HashMap();

    /**
     * This method adds the specified identifier to this SymbolTable. The
     * specified decl object provides details anbout the declaration. If this
     * SymbolTable already defines an identifier with the same name, the
     * SymbolTable is not changed and the existing declaration is returned.
     * Otherwise <code>null</code> is returned.
     * @param ident identifier to be declared
     * @param definition new definition of identifier
     * @return the old definition if the identifier was already declared;
     *         <code>null</code> otherwise
     */
    public Object declare(String ident, Object decl) {
        Object old = symbols.get(ident);
        if (old == null) {
            symbols.put(ident.toUpperCase(), decl);
        }
        return old;
    }

    /**
     * Checks whether the specified identifier is declared.
     * @param ident the name of identifier to be tested
     * @return <code>true</code> if the identifier is declared;
     *         <code>false</code> otherwise.
     */
    public boolean isDeclared(String ident) {
        return (getDeclaration(ident) != null);
    }

    /**
     * Checks the symbol table for the actual declaration of the specified
     * identifier. The method returns the declaration object if available or
     * <code>null</code> for an undeclared identifier.
     * @param ident the name of identifier
     * @return the declaration object if ident is declared; <code>null</code>
     *         otherise.
     */
    public Object getDeclaration(String ident) {
        return symbols.get(ident.toUpperCase());
    }

}
