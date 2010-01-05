/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Writes command output to a json stream
 *
 * @author Ludovic Champenois
 */
@Service(name = "json")
@Scoped(PerLookup.class)
public class JsonActionReporter extends ActionReporter {

    /*
    top is true only for the first toplevel message to emit more data like
     * command name and exit_code of the command
     *
     */
    private boolean top = true;

    /** Creates a new instance of JsonActionReporter */
    public JsonActionReporter() {
    }

    public void writeReport(OutputStream os) throws IOException {
        PrintWriter writer = new PrintWriter(os);

        write(topMessage, writer);
        if (exception != null) {
            writer.println("Exception raised during operation : <br>");
            exception.printStackTrace(writer);
        }
        if (subActions.size() > 0) {
            writer.println(quote(", number_subactions") + ":" + quote("" + subActions.size()));
        }
        writer.flush();
    }

    private void write(MessagePart part, PrintWriter writer) {


        writer.println("{ " + quote("name") + ":" + quote(part.getMessage()));
        if (top) {
            writer.println(", " + quote("command") + ":" + quote(actionDescription));
            writer.println(", " + quote("exit_code") + ":" + quote("" + this.exitCode));
            top = false;
        }
        writeProperties(part.getProps(), writer);
        boolean first = true;
        for (MessagePart child : part.getChildren()) {
            if (first == true) {
                writer.println(", " + quote("result") + " : [");
            } else {
                writer.println(",");

            }
            first = false;
            write(child, writer);

        }
        if (first == false) { //close the array

            writer.println("]");
        }

        writer.println("}");

    }

    private void writeProperties(Properties props, PrintWriter writer) {
        if (props == null || props.size() == 0) {
            return;
        }
        writer.println("," + quote("properties") + " : ");
        boolean needComma = false;
        writer.println("{");
        for (Map.Entry entry : props.entrySet()) {
            if (needComma == true) {
                writer.println(",");

            }
            needComma = true;
            writer.println(quote("" + entry.getKey()) + " : " + quote("" + entry.getValue()));
        }
        writer.println("}");

    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. 
     */
    private String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
