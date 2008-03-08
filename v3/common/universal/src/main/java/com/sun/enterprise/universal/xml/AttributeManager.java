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
package com.sun.enterprise.universal.xml;

import java.util.*;

/**
 * Simple helper class for MiniXmlParser.
 * No publics
 * @author bnevins
 */
class AttributeManager {

    void add(String name, String value) {
        atts.add(new Attribute(name, value));
    }

    void dump() {
        for (Attribute att : atts) {
            System.out.println(att.name + " = " + att.value);
        }
    }

    String getValue(String name) {
        for (Attribute att : atts) {
            if (att.name.equals(name)) {
                return att.value;
            }
        }
        return null;
    }

    private static class Attribute {

        Attribute(String n, String v) {
            name = n;
            value = v;
        }
        String name;
        String value;
    }
    List<Attribute> atts = new ArrayList<Attribute>();
}

