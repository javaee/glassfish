/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.startup;

import org.glassfish.web.util.IntrospectionUtils;
import org.apache.tomcat.util.digester.Rule;
import org.xml.sax.Attributes;

/**
 * Rule that uses the introspection utils to set properties of a context 
 * (everything except "path").
 * 
 * @author Remy Maucherat
 */
public class SetContextPropertiesRule extends Rule {


    // ----------------------------------------------------------- Constructors


    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------------- Public Methods


    /**
     * Handle the beginning of an XML element.
     *
     * @param attributes The attributes of this element
     *
     * @exception Exception if a processing error occurs
     */
    public void begin(String namespace, String nameX, Attributes attributes)
        throws Exception {

        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i);
            if ("".equals(name)) {
                name = attributes.getQName(i);
            }
            if ("path".equals(name)) { 
                continue;
            }
            String value = attributes.getValue(i);
            IntrospectionUtils.setProperty(digester.peek(), name, value);
        }

    }


}
