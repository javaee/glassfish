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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.examples.util.IOBean;
import org.apache.taglibs.standard.examples.util.ServletResponseWrapperForWriter;

/**
 * <p>Tag handler for &lt;resource&gt;
 *
 * @author Pierre Delisle
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:20:41 $
 */
public class ResourceTag extends TagSupport {
    
    //*********************************************************************
    // Instance variables
    
    private String id;
    private String resource;
    
    private Reader reader;
    
    //*********************************************************************
    // Constructors
    
    public ResourceTag() {
        super();
        init();
    }
    
    private void init() {
        id = null;
        resource = null;
    }
    
    //*********************************************************************
    // Tag's properties
    
    /**
     * Tag's 'id' attribute
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Tag's 'resource' attribute
     */
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    //*********************************************************************
    // TagSupport methods
    
    public int doStartTag() throws JspException {
        reader = getReaderFromResource(resource);
        exposeVariable(reader);
        return EVAL_BODY_INCLUDE;
    }
    
    public int doEndTag() throws JspException {
        try {
            reader.close();
        } catch (IOException ex) {}
        reader = null;
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
    
    private Reader getReaderFromResource(String name) throws JspException {
        HttpServletRequest request =
        (HttpServletRequest)pageContext.getRequest();
        HttpServletResponse response =
        (HttpServletResponse)pageContext.getResponse();
        RequestDispatcher rd = null;
        
        // The response of the local URL becomes the reader that
        // we export. Need temporary storage.
        IOBean ioBean = new IOBean();
        Writer writer = ioBean.getWriter();
        ServletResponseWrapper responseWrapper =
        new ServletResponseWrapperForWriter(
        response, new PrintWriter(writer));
        rd = pageContext.getServletContext().getRequestDispatcher(name);
        try {
            rd.include(request, responseWrapper);
            return ioBean.getReader();
        } catch (Exception ex) {
            throw new JspException(ex);
        }
    }
    
    //*********************************************************************
    // Utility methods
        
    private void exposeVariable(Reader reader) {
        if (id != null) {
            pageContext.setAttribute(id, reader);
        }
    }
}
