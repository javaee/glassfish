/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.taglibs.standard.examples.taglib;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.taglibs.standard.examples.util.Util;

/**
 * <p>Tag handler for &lt;escapeHtml&gt;
 *
 * @author Pierre Delisle
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:20:40 $
 */
public class EscapeHtmlTag extends BodyTagSupport {
    
    //*********************************************************************
    // Instance variables
    
    private Object reader;
    private Object writer;
    
    //*********************************************************************
    // Constructors
    
    public EscapeHtmlTag() {
        super();
        init();
    }
    
    private void init() {
        reader = null;
        writer = null;
    }
    
    
    //*********************************************************************
    // Tag's properties
    
    /**
     * Tag's 'reader' attribute
     */
    public void setReader(Object reader) {
        this.reader = reader;
    }
    
    /**
     * Tag's 'writer' attribute
     */
    public void setWriter(Object writer) {
        this.writer = writer;
    }
    
    //*********************************************************************
    // TagSupport methods
    
    public int doEndTag() throws JspException {
        Reader in;
        Writer out;
        
        if (reader == null) {
            String bcs = getBodyContent().getString().trim();
            if (bcs == null || bcs.equals("")) {
                throw new JspTagException("In &lt;escapeHtml&gt;, 'reader' " +
                "not specified and no non-whitespace content inside the tag.");
            }
            in = Util.castToReader(bcs);
        } else {
            in = Util.castToReader(reader);
        }
        
        if (writer == null) {
            out = pageContext.getOut();
        } else {
            out = Util.castToWriter(writer);
        }
        
        transform(in, out);
        return EVAL_PAGE;
    }
    
    /**
     * Releases any resources we may have (or inherit)
     */
    public void release() {
        super.release();
        init();
    }
    
    //*********************************************************************
    // Tag's scific behavior methods
    
    /**
     * Transform
     */
    public void transform(Reader reader, Writer writer)
    throws JspException {
        int c;
        try {
            writer.write("<pre>");
            while ((c = reader.read()) != -1) {
                if (c == '<') {
                    writer.write("&lt;");
                } else if (c == '>') {
                    writer.write("&gt;");
                } else {
                    writer.write(c);
                }
            }
            writer.write("</pre>");
        } catch (IOException ex) {
            throw new JspException("EscapeHtml: " +
            "error copying chars", ex);
        }
    }
}
