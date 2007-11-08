/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.diagnostics.report.html;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * Provide a basic implementation of an attribute.
 */
public class HTMLAttribute implements Attribute, Text {
    
    /**
     * The name of this attribute.
     */
    private String name = null;
    
    /**
     * The value of this attribute.
     */
    private String value = null;

    private static final String DOUBLE_QUOTES ="\"";
    
    private static final char EQUALS = '=';
    /**
     * Create a new attribute.
     * @param name	The name of the attribute.
     * @param value	The value of the attribute.
     */
    public HTMLAttribute(String name, String value) {
        setName(name);
        setValue(value);
    }
    
    /**
     *
     */
    public String getName() {
        return name;
    }

    /**
     *
     */
    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException("Attribute name is null");
        }
        this.name = name;
    }


    /**
     *
     */
    public String getValue() {
        return value;
    }


    /**
     *
     */
    public void setValue(String value) {
        if (value == null) {
            throw new NullPointerException("Attribute value is null.");
        }
        this.value = value;
    }
    

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String nameStr = Escape.getInstance().encodeEntities(name, " \t\r\n");
        String valueStr = Escape.getInstance().encodeEntities(value, "");
        StringBuffer buf = new StringBuffer();
        buf.append(nameStr)
        	.append(EQUALS+DOUBLE_QUOTES)
        	.append(valueStr)
        	.append(DOUBLE_QUOTES);
        return buf.toString();
    }


    /**
     */
    public void write(Writer output) throws IOException {
        output.append(toString());
        output.flush();
    }


    /**
     */
    public void write(File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        write(fw);
        fw.close();
    }

   
}
