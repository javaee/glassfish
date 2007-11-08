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

/**
 * Encapsulate a single HTML document.  This holds a single HTML
 * element with head and body elements, and allows specifying the
 * document type.
 */
public interface Document extends HTMLComponent {

    /**
     * Get the body element of this HTML document.
     * @return	The body element.
     */
    public Element getBody();
    
    /**
     * Get the head element of this HTML document.
     * @return	The head element.
     */
    public Element getHead();
    
    /**
     * Replace the content of this document with the specified
     * head and body.
     * 
     * If the head element does not have the name HEAD, or the body
     * element does not have the name BODY, then an additional, new
     * element is created with the appropriate name, and the input
     * element is added to this element.
     * @param head A new head element.
     * @param body A new body element.
     * @return	This document.
     */
    public void set(Element head, Element body);
    
    /**
     * Get the current doctype string.
     * @return	The current doctype string.
     * @see #setDoctype(String)
     */
    public String getDoctype();
    
    /**
     * Explicitly set the doctype of this document.  Right now the
     * raw doctype string is given.  It will be converted into a line
     * of the form:<br/>
     * <code>&lt;!DOCTYPE html PUBLIC "<em>raw</em>"&gt;</code>
     * <p>
     * This is not very flexible; perhaps this should be re-visited in
     * the future.
     * @param raw	The raw doctype string.
     * @return	This document.
     */
    public void setDoctype(String raw);
}
