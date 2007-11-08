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
 * EJBQLASTImpl.java
 *
 * Created on November 12, 2001
 */


package com.sun.persistence.runtime.query.impl;

import com.sun.persistence.runtime.query.EJBQLAST;

import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;

/**
 * An instance of this class represents a node of the intermediate
 * representation (AST) used by the query compiler. It stores per node: <ul>
 * <li> token type info <li> token text <li> line info <li> column info <li>
 * type info the semantic analysis calculates the type of an expression and adds
 * this info to each node. </ul>
 * @author Michael Bouschen
 */
public class EJBQLASTImpl extends CommonAST implements Cloneable, EJBQLAST {
    /** */
    private static char SEPARATOR = '\n';

    /** */
    private static String INDENT = "  "; //NOI18N
    
    /*
     * If true, omit line number and character position information from toString.
     */
    private static boolean omitLineInfo = false;

    /**
     * The line info
     */
    protected int line = 0;

    /**
     * The column info
     */
    protected int column = 0;

    /**
     * The type info
     */
    protected transient Object typeInfo;
    
    static {
        setVerboseStringConversion(true, EJBQL3Parser._tokenNames);
    }

    /** XXX TBD Mitesh, what should I write here? */
    private String navigationId;

    /**
     * No args constructor.
     */
    public EJBQLASTImpl() {
        
    }

    /**
     * Constructor taking token type, text and type info.
     */
    public EJBQLASTImpl(int type, String text, Object typeInfo) {
        initialize(type, text, typeInfo);
    }

    /**
     * Copy constructor.
     */
    public EJBQLASTImpl(EJBQLASTImpl ast) {
        initialize(ast);
    }
    
    /** */
    public void initialize(Token t) {
        setType(t.getType());
        setText(t.getText());
        setLine(t.getLine());
        setColumn(t.getColumn());
    }

    /** */
    public void initialize(int type, String text, Object typeInfo) {
        setType(type);
        setText(text);
        setTypeInfo(typeInfo);
    }

    /** */
    public void initialize(AST _ast) {
        EJBQLASTImpl ast = (EJBQLASTImpl) _ast;
        setType(ast.getType());
        setText(ast.getText());
        setLine(ast.getLine());
        setColumn(ast.getColumn());
        setTypeInfo(ast.getTypeInfo());
    }

    static void setOmitLineInfo(boolean t) {
        omitLineInfo = t;
    }

    /** */
    public void setLine(int line) {
        this.line = line;
    }

    /** */
    public int getLine() {
        return line;
    }

    /** */
    public void setColumn(int column) {
        this.column = column;
    }

    /** */
    public int getColumn() {
        return column;
    }

    /** */
    public void setTypeInfo(Object typeInfo) {
        this.typeInfo = typeInfo;
    }

    /** */
    public Object getTypeInfo() {
        return typeInfo;
    }

    /**
     * If this node's navigationId is null, return its {@link #getText},
     * otherwise return its navigationId.
     * @see com.sun.persistence.runtime.query.EJBQLAST#getNavigationId()
     */
    public String getNavigationId() {
        return (navigationId == null) ? getText() : navigationId;
    }

    /**
     * @see com.sun.persistence.runtime.query.EJBQLAST#setNavigationId(java.lang.String)
     */
    public void setNavigationId(String navigationId) {
        this.navigationId = navigationId;
    }

    /**
     * Returns a string representation of this EJBQLASTImpl w/o child ast nodes.
     * @return a string representation of the object.
     */
    public String toString() {
        Object typeInfo = getTypeInfo();
        StringBuffer repr = new StringBuffer();
        repr.append("["); //NOI18N
        // token text and type information
        repr.append(super.toString());
        // line/column info
        if (!omitLineInfo) {
            repr.append(", ("); //NOI18N
            repr.append(getLine() + "/" + getColumn()); //NOI18N
            repr.append(")"); //NOI18N
        }
        // type info
        repr.append(", "); //NOI18N
        repr.append(typeInfo);
        repr.append("]"); //NOI18N
        return repr.toString();
    }

    /**
     * Returns a full string representation of this JQLAST. The returned string
     * starts with the specified title string, followed by the string
     * representation of this ast, followed by the string representation of the
     * child ast nodes of this ast. The method dumps each ast node on a separate
     * line. Child ast nodes are indented. The method calls toString to dump a
     * single node w/o children.
     * @return string representation of this ast including children.
     */
    public String getTreeRepr(String title) {
        return title + this.getTreeRepr(0);
    }

    /**
     * Helper method for getTreeRepr.
     */
    private String getTreeRepr(int level) {
        StringBuffer repr = new StringBuffer();
        // current node
        repr.append(SEPARATOR);
        repr.append(getIndent(level));
        repr.append(this.toString());
        // handle children
        for (EJBQLASTImpl node = (EJBQLASTImpl) this.getFirstChild();
             node != null; node = (EJBQLASTImpl) node.getNextSibling()) {
            repr.append(node.getTreeRepr(level + 1));
        }
        return repr.toString();
    }

    /**
     * Returns the indent specified by level.
     */
    private String getIndent(int level) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < level; i++) {
            buf.append(INDENT);
        }
        return buf.toString();
    }

    /**
     * Creates and returns a copy of this object. The returned EJBQLASTImpl shares
     * the same state as this object, meaning the fields type, text, line,
     * column, and typeInfo have the same values. But it is not bound to any
     * tree structure, thus the child is null and the sibling is null.
     * @return a clone of this instance.
     */
    protected Object clone() throws CloneNotSupportedException {
        EJBQLASTImpl clone = (EJBQLASTImpl) super.clone();
        clone.setFirstChild(null);
        clone.setNextSibling(null);
        return clone;
    }

    public String getText() {
        return super.getText();
    }
}

