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


package org.apache.commons.modeler.modules;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import org.apache.commons.digester.Digester;
import org.apache.commons.modeler.Registry;

public class MbeansDescriptorsDigesterSource extends ModelerSource
{
    private static Logger log = Logger.getLogger(MbeansDescriptorsDigesterSource.class.getName());

    Registry registry;
    String location;
    String type;
    Object source;
    List mbeans=new ArrayList();

    public void setRegistry(Registry reg) {
        this.registry=reg;
    }

    public void setLocation( String loc ) {
        this.location=loc;
    }

    /** Used if a single component is loaded
     *
     * @param type
     */
    public void setType( String type ) {
       this.type=type;
    }

    public void setSource( Object source ) {
        this.source=source;
    }

    public List loadDescriptors( Registry registry, String location,
                                 String type, Object source)
            throws Exception
    {
        setRegistry(registry);
        setLocation(location);
        setType(type);
        setSource(source);
        execute();
        return mbeans;
    }

    public void execute() throws Exception {
        if( registry==null ) registry=Registry.getRegistry();

        InputStream stream=(InputStream)source;

        long t1=System.currentTimeMillis();

        Digester digester = new Digester();
        digester.setNamespaceAware(false);
        digester.setValidating(false);
        URL url = registry.getClass().getResource
                ("/org/apache/commons/modeler/mbeans-descriptors.dtd");
        digester.register
                ("-//Apache Software Foundation//DTD Model MBeans Configuration File",
                        url.toString());

        // Push our registry object onto the stack
        digester.push(mbeans);

        // Configure the parsing rules
        digester.addObjectCreate
                ("mbeans-descriptors/mbean",
                        "org.apache.commons.modeler.ManagedBean");
        digester.addSetProperties
                ("mbeans-descriptors/mbean");
        digester.addSetNext
                ("mbeans-descriptors/mbean",
                        "add",
                        "java.lang.Object");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/attribute",
                        "org.apache.commons.modeler.AttributeInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/attribute");
        digester.addSetNext
                ("mbeans-descriptors/mbean/attribute",
                        "addAttribute",
                        "org.apache.commons.modeler.AttributeInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/attribute/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/attribute/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/attribute/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/constructor",
                        "org.apache.commons.modeler.ConstructorInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/constructor");
        digester.addSetNext
                ("mbeans-descriptors/mbean/constructor",
                        "addConstructor",
                        "org.apache.commons.modeler.ConstructorInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/constructor/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/constructor/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/constructor/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/constructor/parameter",
                        "org.apache.commons.modeler.ParameterInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/constructor/parameter");
        digester.addSetNext
                ("mbeans-descriptors/mbean/constructor/parameter",
                        "addParameter",
                        "org.apache.commons.modeler.ParameterInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/notification",
                        "org.apache.commons.modeler.NotificationInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/notification");
        digester.addSetNext
                ("mbeans-descriptors/mbean/notification",
                        "addNotification",
                        "org.apache.commons.modeler.NotificationInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/notification/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/notification/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/notification/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addCallMethod
                ("mbeans-descriptors/mbean/notification/notification-type",
                        "addNotifType", 0);

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/operation",
                        "org.apache.commons.modeler.OperationInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/operation");
        digester.addSetNext
                ("mbeans-descriptors/mbean/operation",
                        "addOperation",
                        "org.apache.commons.modeler.OperationInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/operation/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/operation/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/operation/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/operation/parameter",
                        "org.apache.commons.modeler.ParameterInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/operation/parameter");
        digester.addSetNext
                ("mbeans-descriptors/mbean/operation/parameter",
                        "addParameter",
                        "org.apache.commons.modeler.ParameterInfo");

        // Process the input file to configure our registry
        try {
            digester.parse(stream);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error digesting Registry data", e);
            throw e;
        }
        long t2=System.currentTimeMillis();
//        if( t2-t1 > 500 )
        log.log(Level.INFO, "Loaded registry information (digester) " + ( t2 - t1 ) + " ms");
    }
}
