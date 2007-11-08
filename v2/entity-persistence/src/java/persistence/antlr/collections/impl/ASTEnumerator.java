package persistence.antlr.collections.impl;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.jGuru.com
 * Software rights: http://www.antlr.org/license.html
 *
 */

import persistence.antlr.collections.impl.Vector;
import persistence.antlr.collections.ASTEnumeration;
import persistence.antlr.collections.AST;

import java.util.NoSuchElementException;

public class ASTEnumerator implements persistence.antlr.collections.ASTEnumeration {
    /** The list of root nodes for subtrees that match */
    VectorEnumerator nodes;
    int i = 0;


    public ASTEnumerator(Vector v) {
        nodes = new VectorEnumerator(v);
    }

    public boolean hasMoreNodes() {
        synchronized (nodes) {
            return i <= nodes.vector.lastElement;
        }
    }

    public persistence.antlr.collections.AST nextNode() {
        synchronized (nodes) {
            if (i <= nodes.vector.lastElement) {
                return (AST)nodes.vector.data[i++];
            }
            throw new NoSuchElementException("ASTEnumerator");
        }
    }
}
