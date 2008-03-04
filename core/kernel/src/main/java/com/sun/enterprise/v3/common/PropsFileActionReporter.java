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

import java.io.PrintWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Mar 2, 2007
 * Time: 9:14:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class PropsFileActionReporter extends ActionReporter {


    public void writeReport(OutputStream os) throws IOException {

        Manifest out = new Manifest();
        Attributes mainAttr = out.getMainAttributes();
        mainAttr.put(Attributes.Name.SIGNATURE_VERSION, "1.0");
        mainAttr.putValue("exit-code", exitCode.toString());
        writeReport(null, topMessage, out, mainAttr);
        out.write(os);
    }

    public void writeReport(String prefix, MessagePart part, Manifest m,  Attributes attr) {

        attr.putValue("message", part.getMessage());
        if (part.getProps().size()>0) {
            String keys=null;
            for (Map.Entry entry : part.getProps().entrySet()) {
                String key  = entry.getKey().toString().replaceAll(" ", "_");
                keys = (keys==null?key:keys + "," + key);
                attr.putValue(key+"_name", entry.getKey().toString());
                attr.putValue(key+"_value", entry.getValue().toString());
            }
            attr.putValue("keys", keys);
        }
        if (part.getChildren().size()>0) {
            attr.putValue("children-type", part.getChildrenType());
            String keys=null;
            for (MessagePart child : part.getChildren()) {
                String newPrefix = (prefix==null?child.getMessage():prefix+"."+child.getMessage()).replaceAll(" ", "_");
                keys = (keys==null?newPrefix:keys + "," + newPrefix);
                Attributes childAttr = new Attributes();
                m.getEntries().put(newPrefix, childAttr);
                writeReport(newPrefix, child, m, childAttr);
            }
            attr.putValue("children", keys);
        }
    }
}

