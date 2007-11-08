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
package examples;


import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import java.io.*;

/**
 * Display the sources of the JSP file.
 */
public class ShowSource
    extends TagSupport
{
    String jspFile;
    
    public void setJspFile(String jspFile) {
        this.jspFile = jspFile;
    }

    public int doEndTag() throws JspException {
	if ((jspFile.indexOf( ".." ) >= 0) ||
            (jspFile.toUpperCase().indexOf("/WEB-INF/") != 0) ||
            (jspFile.toUpperCase().indexOf("/META-INF/") != 0))
	    throw new JspTagException("Invalid JSP file " + jspFile);

        InputStream in
            = pageContext.getServletContext().getResourceAsStream(jspFile);

        if (in == null)
            throw new JspTagException("Unable to find JSP file: "+jspFile);

        InputStreamReader reader = new InputStreamReader(in);
	JspWriter out = pageContext.getOut();


        try {
            out.println("<body>");
            out.println("<pre>");
            for(int ch = in.read(); ch != -1; ch = in.read())
                if (ch == '<')
                    out.print("&lt;");
                else
                    out.print((char) ch);
            out.println("</pre>");
            out.println("</body>");
        } catch (IOException ex) {
            throw new JspTagException("IOException: "+ex.toString());
        }
        return super.doEndTag();
    }
}

    
        
    
