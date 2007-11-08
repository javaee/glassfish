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
 * EJBQLASTFactory.java
 *
 * Created on November 12, 2001
 */


package com.sun.persistence.runtime.query.impl;

import antlr.ASTFactory;
import antlr.collections.AST;
import com.sun.persistence.utility.I18NHelper;

import java.util.ResourceBundle;

/**
 * Factory to create and connect EJBQLASTImpl nodes.
 * @author Michael Bouschen
 */
public class EJBQLASTFactory extends ASTFactory {
    /**
     * The singleton EJBQLASTFactory instance.
     */
    private static EJBQLASTFactory factory = new EJBQLASTFactory();

    /**
     * I18N support.
     */
    private final static ResourceBundle msgs = I18NHelper.loadBundle(
            EJBQLASTFactory.class);

    /**
     * Get an instance of EJBQLASTFactory.
     * @return an instance of EJBQLASTFactory
     */
    public static EJBQLASTFactory getInstance() {
        return factory;
    }

    /**
     * Constructor. EJBQLASTFactory is a singleton, please use {@link
     * #getInstance} to get the factory instance.
     */
    protected EJBQLASTFactory() {
        this.theASTNodeTypeClass = EJBQLASTImpl.class;
        this.theASTNodeType = this.theASTNodeTypeClass.getName();
    }

    /**
     * Overwrites superclass method to create the correct AST instance.
     */
    public AST create() {
        return new EJBQLASTImpl();
    }

    /**
     * Overwrites superclass method to create the correct AST instance.
     */
    public AST create(AST tr) {
        return create((EJBQLASTImpl) tr);
    }

    /**
     * Creates a clone of the specified EJBQLASTImpl instance.
     */
    public EJBQLASTImpl create(EJBQLASTImpl tr) {
        try {
            return (tr == null) ? null : (EJBQLASTImpl) tr.clone();
        } catch (CloneNotSupportedException ex) {
            throw new EJBQLException(
                    I18NHelper.getMessage(
                            msgs, "ERR_UnexpectedExceptionClone"),
                    ex); //NOI18N
        }
    }
}

