/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.common;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author dochez
 */
@Service(name = "html")
@Scoped(PerLookup.class)
public class HTMLActionReporter extends ActionReporter {
    
    /** Creates a new instance of HTMLActionReporter */
    public HTMLActionReporter() {
    }
    
    public void writeReport(OutputStream os) throws IOException {
        PrintWriter writer = new PrintWriter(os);
        writer.print("<html>");
        writer.println("<body>" +
                "<h1>GlassFish " + actionDescription + " command report</h1>" +
                "<br><br>");
        writer.println("Exit Code : " + this.exitCode);
        writer.println("<hr>");
        write(2, topMessage, writer);
        writer.println("<hr>");
        if (exception!=null) {
            writer.println("Exception raised during operation : <br>");
            exception.printStackTrace(writer);
        }
        if (subActions.size()>0) {
            writer.println("There are " + subActions.size() + " sub operations");
        }
        writer.print("</body></html>");
        writer.flush();        
    }

    private void write(int level, MessagePart part, PrintWriter writer) {
        if (level>6) {
            writer.println(part.getMessage());
        } else {
            writer.println("<h" + level + ">" + part.getMessage() + "</h" + level + ">");
        }
        write(part.getProps(), writer);

        for (MessagePart child : part.getChildren()) {
            write(level+1, child, writer);
        }
    }
    
    private void write(Properties props, PrintWriter writer) {
        if (props==null || props.size()==0) {
            return;
        }
        writer.println("<table border=\"1\">");
        for (Map.Entry entry : props.entrySet()) {
            writer.println("<tr>");
            writer.println("<td>" + entry.getKey() + "</td>");
            writer.println("<td>" + entry.getValue() + "</td>");
            writer.println("</tr>");
        }
        writer.println("</table>");
        
    }
}
