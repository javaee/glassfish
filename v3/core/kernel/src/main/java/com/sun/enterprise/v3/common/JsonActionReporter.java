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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
