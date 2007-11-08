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
import java.util.List;

/*
 * Basic implementation of a document.
 */
public class HTMLDocument implements Document {
    
    /**
     * The root HTML element.
     */
    private Element html = new HTMLElement(HTMLReportConstants.HTML);
    
    
    /**
     * The head element.
     */
    private Element head = html.addElement(HTMLReportConstants.HEAD);
    
    
    /**
     * The body element.
     */
    private Element body = html.addElement(HTMLReportConstants.BODY);
    
    
    /**
     * The doctype.
     */
    private String doctype = "-//W3C//DTD HTML 4.01 Transitional//EN";
  
    /**
     */
    public Element getBody() {
        return body;
    }


    /**
     */
    public Element getHead() {
        return head;
    }


    /**
     */
    public String getDoctype() {
        return doctype;
    }


    /**
     */
    public void setDoctype(String raw) {
        if (raw == null) {
            throw new NullPointerException("Doctype string is null.");
        }
        doctype = raw;
    }
    

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<!DOCTYPE html PUBLIC \"")
        	.append(Escape.getInstance().encodeEntities(doctype, ""))
        	.append("\">\n")
        	.append(html.toString());
        return buf.toString();
    }


    /**
     */
    public void write(Writer output) throws IOException {
        output.append("<!DOCTYPE html PUBLIC \"")
    		.append(Escape.getInstance().encodeEntities(doctype, ""))
    		.append("\">\n")
    		.append(html.toString());
        output.flush();
    }


    /**
     */
    public void write(File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        write(fw);
        fw.close();
    }


    /**
     */
    public void set(Element head, Element body) {
        if (head == null) {
            throw new NullPointerException("Head element is null.");
        }
        if (body == null) {
            throw new NullPointerException("Body element is null.");
        }
        
        // Check the element names.
        if (!head.getName().equalsIgnoreCase(HTMLReportConstants.HEAD)) {
            new HTMLElement("HEAD").add(head);
        }
        if (!body.getName().equalsIgnoreCase(HTMLReportConstants.BODY)) {
            new HTMLElement("BODY").add(head);
        }
        
        // Discard old elements.
        List<Element> elements = html.getElements("BODY");
        for (Element element : elements) {
            html.delete(element);
        } // Loop discarding body elements.
        elements = html.getElements("HEAD");
        for (Element element : elements) {
            html.delete(element);
        } // Loop discarding head elements.
        
        // Add the new elements.
        html.add(head);
        html.add(body);
        this.head = head;
        this.body = body;
    }


  
}
