/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.diagnostics.report.html;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;


/**
 * Basic implementation of an element.
 */
public class HTMLElement implements Element {
    
    /**
     * The name of this element.
     */
    private String name = null;
    
    /**
     * The children of this element which are not attributes.
     */
    private List<HTMLComponent> children = new LinkedList<HTMLComponent>();
    
    /**
     * The attributes for this element.
     */
    private List<Attribute> attributes = new LinkedList<Attribute>();
    
    private static final char TAG_MARKER_BEGIN = '<';
    private static final char TAG_MARKER_END = '>';
    private static final String SLASH_BEGIN="/>";
    private static final String SLASH_END="</";

    /**
     * Make a new element, specifying the name.
     * @param name	The name of the element.
     */
    public HTMLElement(String name) {
        setName(name);
    }

    /**
     * @param child	The node to add.
     * @return	This element.
     */
    public void add(HTMLComponent child) {
        if (child == null) {
            throw new NullPointerException("Child node is null.");
        }
        if (child == this) {
            throw new IllegalArgumentException("Attempt to add a node " +
            		"to itself.  Node is " + toString() + ".");
        }
        if (child instanceof Attribute) {
            attributes.add((Attribute) child);
        } else {
            children.add(child);
        }
    }
    

    /**
     */
    public Element addElement(String name) {
        if (name == null) {
            throw new NullPointerException("Element name is null.");
        }
        Element newElement = new HTMLElement(name);
        add(newElement);
        return newElement;
    }

    /**
     */
    public Text addComment(String content) {
        HTMLComment comment = new HTMLComment(content);
        add(comment);
        return comment;
       
    }

    /**
     */
    public Text addText(String text) {
        if(text != null) {
            Text textNode = new HTMLText(text);
            add(textNode);
            return textNode;
        }
        return null;
    }

    /**
     */
    public void addText(Iterator<String> textValues) {
        while(textValues.hasNext()) {
            Text textNode = new HTMLText(textValues.next());
            add(textNode);
        }
    }
    public Attribute addAttribute(String id, String value) {
        Attribute att = new HTMLAttribute(id, value);
        add(att);
        return att;
    }
    /**
     */
    public List<Element> getElements() {
        List<Element> retval = new LinkedList<Element>();
        for (HTMLComponent node : children) {
            if (node instanceof Element) {
                retval.add((Element) node);
            }
        } // Loop over children.
        return retval;
    }


    /**
     */
    public List<Element> getElements(String name) {
        List<Element> list = new LinkedList<Element>();
        for (HTMLComponent node : children) {
            if (node instanceof Element) {
                Element element = (Element) node;
                if (element.getName().equalsIgnoreCase(name)) {
                    list.add((Element) node);
                }
            }
        } // Loop over children.
        return list;
    }

    /**
     */
    public List<Text> getComments() {
        List<Text> list = new LinkedList<Text>();
        for (HTMLComponent node : children) {
            if (node instanceof HTMLComment) {
                list.add((HTMLComment) node);
            }
        } // Loop over children.
        return list;
    }
    
    /**
     */
    public List<Text> getTexts() {
        List<Text> retval = new LinkedList<Text>();
        for (HTMLComponent node : children) {
            if (node instanceof Text) {
                retval.add((Text) node);
            }
        } // Loop over children.
        return retval;
    }

    /**
     */
    public List<Attribute> getAttributes() {
        List<Attribute> retval = new LinkedList<Attribute>();
        retval.addAll(attributes);
        return retval;
    }


    /**
     */
    public List<Attribute> getAttributes(String name) {
        List<Attribute> retval = new LinkedList<Attribute>();
        for (HTMLComponent node : getAttributes()) {
            if (node instanceof Attribute) {
                Attribute att = (Attribute) node;
                if (att.getName().equalsIgnoreCase(name)) {
                    retval.add((Attribute) node);
                }
            }
        } // Loop over children.
        return retval;
    }

    /**
     */
    public List<HTMLComponent> children() {
        List<HTMLComponent> childrenList = new LinkedList<HTMLComponent>();
        childrenList.addAll(children);
        childrenList.addAll(attributes);
        return childrenList;
    }


    /**
     * @param child	The node to delete.
     */
    public void delete(HTMLComponent child) {
        if (child instanceof Attribute) {
            attributes.remove(child);
        } else {
            children.remove(child);
        }
    }
    

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
      
        String eName = Escape.getInstance().encodeEntities(name, " \t\r\n");
        buf.append(TAG_MARKER_BEGIN).append(eName);
        for (Attribute att : attributes) {
            buf.append(" ").append(att.toString());
        } // Loop over attributes.
        if (children.size() == 0) {
            buf.append(SLASH_BEGIN);
        } else {
	        buf.append(TAG_MARKER_END);
	        for (HTMLComponent node : children) {
	            buf.append(node.toString());
	        } // Loop over children.
	        buf.append(SLASH_END).append(eName).append(TAG_MARKER_END);
        }
        return buf.toString();
    }


    /**
     */
    public void write(Writer output) throws IOException {
        output.append(toString());
	output.flush();
    }


    /**
     */
    public void write(File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        write(fileWriter);
        fileWriter.close();
    }


    /**
     */
    public String getName() {
        return name;
    }


    /**
     */
    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException("Element name is null.");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("Element name is empty.");
        }
        this.name = name;
    }


    /**
     * Get all children of this element which are of a specified class.
     * @param type	The class of the children to obtain.
     * @return	All children of the specified class.
     */
    public <T extends HTMLComponent> List<T> get(Class<T> type) {
        List<T> list = new LinkedList<T>();
        for (HTMLComponent child : children()) {
            if (type.isInstance(child)) {
                list.add((T) child);
            }
        }
        return list;
    }
 }
