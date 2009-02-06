/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina.util;


import org.apache.tomcat.util.digester.Digester;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

import com.sun.grizzly.util.IntrospectionUtils;

/**
 * This extended digester filters out ${...} tokens to replace them with
 * matching system properties.
 * 
 * @author Simon Kitching
 * @author Remy Maucherat
 */
public class CatalinaDigester extends Digester {


    // ---------------------------------------------------------- Static Fields


    private static class SystemPropertySource 
        implements IntrospectionUtils.PropertySource {
        public String getProperty( String key ) {
            return System.getProperty(key);
        }
    }

    protected static IntrospectionUtils.PropertySource source[] = 
        new IntrospectionUtils.PropertySource[] { new SystemPropertySource() };


    // ---------------------------------------------------------------- Methods


    /**
     * Invoke inherited implementation after applying variable
     * substitution to any attribute values containing variable
     * references. 
     */
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes list)
        throws SAXException {
        list = updateAttributes(list);
        super.startElement(namespaceURI, localName, qName, list);
    }


    /**
     * Invoke inherited implementation after applying variable substitution
     * to the character data contained in the current element.
     */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException  {
        bodyText = updateBodyText(bodyText);
        super.endElement(namespaceURI, localName, qName);
    }


    /**
     * Returns an attributes list which contains all the attributes
     * passed in, with any text of form "${xxx}" in an attribute value
     * replaced by the appropriate value from the system property.
     */
    private Attributes updateAttributes(Attributes list) {

        if (list.getLength() == 0) {
            return list;
        }
        
        AttributesImpl newAttrs = new AttributesImpl(list);
        int nAttributes = newAttrs.getLength();
        for (int i = 0; i < nAttributes; ++i) {
            String value = newAttrs.getValue(i);
            try {
                String newValue = 
                    IntrospectionUtils.replaceProperties(value, null, source);
                if (value != newValue) {
                    newAttrs.setValue(i, newValue);
                }
            }
            catch (Exception e) {
                // ignore - let the attribute have its original value
            }
        }

        return newAttrs;

    }


    /**
     * Return a new StringBuffer containing the same contents as the
     * input buffer, except that data of form ${varname} have been
     * replaced by the value of that var as defined in the system property.
     */
    private StringBuffer updateBodyText(StringBuffer bodyText) {
        String in = bodyText.toString();
        String out;
        try {
            out = IntrospectionUtils.replaceProperties(in, null, source);
        } catch(Exception e) {
            return bodyText; // return unchanged data
        }

        if (in.equals(out))  {
            // No substitutions required. Don't waste memory creating
            // a new buffer
            return bodyText;
        } else {
            return new StringBuffer(out);
        }
    }


}

