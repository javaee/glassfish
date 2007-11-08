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
 * Represents an EJBQL abstract syntax tree.
 * @author Dave Bristor
 */
public interface EJBQLAST {
    /** @return line number on which node's text resides. */
    public int getLine();

    /** @return column number at which node's text resides. */
    public int getColumn();

    /** @return an Object representing the type of this node. */
    public Object getTypeInfo();

    /**
     * Returns a full string representation of this JQLAST. The returned string
     * starts with the specified title string, followed by the string
     * representation of this ast, followed by the string representation of the
     * child ast nodes of this ast. The method dumps each ast node on a separate
     * line. Child ast nodes are indented. The method calls toString to dump a
     * single node w/o children.
     * @return string representation of this ast including children.
     */
    public String getTreeRepr(String title);
    
    /**
     * When this interface is implemented by a class that extends 
     * <code>antlr.CommonAST</code>, this method's implementation should
     * delegate to <code>antlr.CommonAST.getText</code>. 
     * @return String representation of this node.
     */
    public String getText();
    
    /**
     * @return the navigationId for this node.
     */
    public String getNavigationId();

    /**
     * Sets the node's navigationId
     * @param navigationId the navigationId for this node.
     */
    public void setNavigationId(String navigationId);

}
