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

import java.util.List;

/**
 * A single element in an HTML document.  Elements may contain
 * other elements and text, in a strict order.  They may also
 * have attributes assigned to them.
 */
public interface Element extends Container {

    /**
     * Add a element to  this element.  
     * @param name The name of the new element.
     */
    public Element addElement(String name);
    
    /**
     * Add a attribute to this element.  
     * @param id The name of the attribute.
     * @param value The value of the attribute.
     */
    public Attribute addAttribute(String id, String value);
    
    /**
     * Factory method to create a new comment and add it to this
     * element.  The content of the comment is escaped as appropriate.
     * @param content	The content of this comment.
     * @return	The new comment.
     * @see Text
     */
    public Text addComment(String content);
    
    /**
     * Factory method to create a new text instance and add it to this
     * element.  The content of the text is escaped as appropriate.
     * @param text	The text to add.
     * @return	The new text instance.
     */
    public Text addText(String text);
    
    /**
     * Get all child elements of this element.
     * @return	The list of child elements.
     */
    public List<Element> getElements();
    
    /**
     * Get all child elements of this element which have the specified
     * name.
     * @param name	The element name.
     * @return	The list of child elements.
     */
    public List<Element> getElements(String name);
    
    /**
     * Get all comments in this element.
     * @return	The list of all comments.
     */
    public List<Text> getComments();
    
    /**
     * Get all text contained in this element.
     * @return	The list of text nodes contained in this element.
     */
    public List<Text> getTexts();
    
    /**
     * Get all attributes of this element.
     * @return	The list of all attributes.
     */
    public List<Attribute> getAttributes();
    
    /**
     * Get all attributes of this element whose name matches the given
     * name.
     * @param name	The attribute name.
     * @return	The list of all matching attributes.
     */
    public List<Attribute> getAttributes(String name);
    
    /**
     * Get the name of this element.
     * @return	The name of this element.
     */
    public String getName();
    
    /**
     * Set the name of this element.  The previous name is discarded.
     * Note that element names are not case-sensitive; on output they
     * are all converted to upper-case.
     * @param name The new name of this element.
     */
    public void setName(String name);
}
