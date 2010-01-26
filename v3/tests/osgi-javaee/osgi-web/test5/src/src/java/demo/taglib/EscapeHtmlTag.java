/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2010 Sun Microsystems, Inc. All rights reserved.
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

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package demo.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * <p>Tag handler for &lt;escapeHtml&gt;
 *
 * @author Pierre Delisle
 * @version $Revision: 1.4 $ $Date: 2006/03/07 17:21:01 $
 */
public class EscapeHtmlTag extends BodyTagSupport {

    //*********************************************************************
    // Instance variables

    private Reader reader;
    private Writer writer;

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

    /** Tag's 'reader' attribute */
    public void setReader(Reader reader) {
        this.reader = reader;
    }


    /** Tag's 'writer' attribute */
    public void setWriter(Writer writer) {
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
            in = castToReader(bcs);
        } else {
            in = reader;
        }

        if (writer == null) {
            out = pageContext.getOut();
        } else {
            out = writer;
        }

        transform(in, out);
        return EVAL_PAGE;
    }


    /** Releases any resources we may have (or inherit) */
    public void release() {
        super.release();
        init();
    }

    //*********************************************************************
    // Tag's scific behavior methods

    /** Transform */
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

    //*********************************************************************
    // Utility methods   


    public static Reader castToReader(Object obj) throws JspException {
        if (obj instanceof InputStream) {
            return new InputStreamReader((InputStream) obj);
        } else if (obj instanceof Reader) {
            return (Reader) obj;
        } else if (obj instanceof String) {
            return new StringReader((String) obj);
        }
        throw new JspException("Invalid type '" + obj.getClass().getName() +
                               "' for castToReader()");
    }


    public static Writer castToWriter(Object obj) throws JspException {
        if (obj instanceof OutputStream) {
            return new OutputStreamWriter((OutputStream) obj);
        } else if (obj instanceof Writer) {
            return (Writer) obj;
            /*@@@
        } else if (obj instanceof String) {
            return new StringWriter();
             */
        }
        throw new JspException("Invalid type '" + obj.getClass().getName() +
                               "' for castToWriter()");
    }

}
