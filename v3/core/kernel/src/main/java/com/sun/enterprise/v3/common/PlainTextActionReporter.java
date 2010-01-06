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

import com.sun.enterprise.util.*;
import java.util.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import java.io.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

/**
 *
 * @author bnevins
 */
@Service(name="plain")
@Scoped(PerLookup.class)
public class PlainTextActionReporter extends ActionReporter {
    public static final String MAGIC = "PlainTextActionReporter";

    public void writeReport(OutputStream os) throws IOException {
        // The caller will read MAGIC and the next characters for success/failure
        // everything after the HEADER_END is good data
        writer = new PrintWriter(os);
        writer.print(MAGIC);
        if(isFailure()) {
            writer.print("FAILURE");
            Throwable t = getFailureCause();
            
            if(t != null)
                writer.print(t);
        }
        else {
            writer.print("SUCCESS");
        }

        if(superSimple(topMessage)) {
            writer.print(topMessage.getMessage());
        }

        else {
            writer.print("\n");
            
            if(StringUtils.ok(actionDescription))
                writer.println("Description: " + actionDescription);

            write("", topMessage);
        }

        if (!subActions.isEmpty())
            writer.println("There are " + subActions.size() + " sub operations");

        writer.flush();
    }
    @Override
    public String getContentType() {
        return "text/plain";
    }

    private boolean superSimple(MessagePart part) {
        // this is mainly here for backward compatability for when this Reporter
        // only wrote out the main message.
        List<MessagePart> list = part.getChildren();
        Properties props = part.getProps();
        boolean hasChildren =  ( list != null && !list.isEmpty() );
        boolean hasProps = ( props != null && props.size() > 0 );

        // return true if we are very very simple!
        return !hasProps && !hasChildren;
    }
    private void write(String indent, MessagePart part) {
        writer.printf("%s%s\n", indent, part.getMessage());
        write(indent + INDENT, part.getProps());

        for (MessagePart child : part.getChildren()) {
            write(indent + INDENT, child);
        }
    }

    private void write(String indent, Properties props) {
        if (props == null || props.size() <= 0)
            return;

        for (Map.Entry<Object,Object> entry : props.entrySet()) {
            String key = "" + entry.getKey();
            String val = "" + entry.getValue();
            writer.printf("%s[%s=%s]\n", indent, key, val);
        }
    }

    private PrintWriter writer;
    private static final String INDENT = "    ";
}
