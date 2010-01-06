/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2010 Sun Microsystems, Inc. All rights reserved.
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
