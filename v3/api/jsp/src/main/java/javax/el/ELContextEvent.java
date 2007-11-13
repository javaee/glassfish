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
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 */ 

package javax.el;

/**
 * An event which indicates that an {@link ELContext} has been created.
 * The source object is the ELContext that was created.
 *
 * @see ELContext
 * @see ELContextListener
 * @since JSP 2.1
 */
public class ELContextEvent extends java.util.EventObject {

    /**
     * Constructs an ELContextEvent object to indicate that an 
     * <code>ELContext</code> has been created.
     *
     * @param source the <code>ELContext</code> that was created.
     */
    public ELContextEvent(ELContext source) {
        super(source);
    }

    /**
     * Returns the <code>ELContext</code> that was created.
     * This is a type-safe equivalent of the {@link #getSource} method.
     *
     * @return the ELContext that was created.
     */
    public ELContext getELContext() {
        return (ELContext) getSource();
    }
}
