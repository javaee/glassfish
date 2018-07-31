/*
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
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

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

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
 * @version $Revision: 1.3 $ $Date: 2004/11/14 07:33:18 $
 */
public class EscapeHtmlTag extends BodyTagSupport {

    //*********************************************************************
    // Instance variables
    
    private String reader;
    private String writer;
    
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
    public void setReader(String reader) {
        this.reader = reader;
    }


    /**
     * Tag's 'writer' attribute
     */
    public void setWriter(String reader) {
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
            in = castToReader(eval("reader", reader, Object.class));
        }

        if (writer == null) {
            out = pageContext.getOut();
        } else {
            out = castToWriter(eval("writer", writer, Object.class));
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
    
    //*********************************************************************
    // Utility methods
    
    /**
     * Evaluate elexprvalue
     */
    private Object eval(String attName, String attValue, Class clazz)
        throws JspException {
        Object obj = ExpressionEvaluatorManager.evaluate(attName, attValue,
                                                         clazz, this,
                                                         pageContext);
        if (obj == null) {
            throw new JspException("escapeHtml");
        } else {
            return obj;
        }
    }


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
