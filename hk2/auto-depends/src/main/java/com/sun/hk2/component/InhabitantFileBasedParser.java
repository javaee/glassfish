/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.hk2.component;

import org.jvnet.hk2.component.MultiMap;

import static com.sun.hk2.component.InhabitantsFile.CLASS_KEY;
import static com.sun.hk2.component.InhabitantsFile.INDEX_KEY;

/**
 * {@link com.sun.hk2.component.InhabitantParser} implementation based on the inhabitant
 * file format.
 *
 * Format of the inhabitant is subject to changes and compatibility across releases cannot
 * be guaranteed.
 *
 * file : line+
 * line : class=class-value, (key=value ,?)+
 * key : index | targetType | any
 * class-value : {@link Class#getName()} name of the service implementation
 * index : index-name[:name](,index)*
 * index-name : {@link Class#getName()} class name of the contract
 * name: string identifying the service name
 * targeType : class-type (, method-name)?
 * class-type : class name where {@link org.jvnet.hk2.annotations.InhabitantAnnotation} was declared
 * method-name : method name if the {@link org.jvnet.hk2.annotations.InhabitantAnnotation} was placed on a method
 * any : some-key=some-value
 * some-key : [a-z]+
 * some-value : [a-z]+
 *
 * "Any" above contributes to the metadata portion of the inhabitant.
 */
public class InhabitantFileBasedParser implements InhabitantParser {
    final MultiMap<String,String> metadata;
    final KeyValuePairParser parser;

    public InhabitantFileBasedParser(KeyValuePairParser parser) {
        this.parser = parser;
        metadata = buildMetadata(parser);
    }
    public Iterable<String> getIndexes() {
        return parser.findAll(INDEX_KEY);
    }

    public String getImplName() {
        return metadata.getOne(CLASS_KEY);
    }

    public String getLine() {
        return parser.getLine();
    }

    public void setImplName(String name) {
        metadata.set(CLASS_KEY,name);        
    }

    public void rewind() {
        parser.rewind();
    }

    public MultiMap<String, String> getMetaData() {
        return metadata;
    }

    public static MultiMap<String,String> buildMetadata(KeyValuePairParser kvpp) {
        MultiMap<String,String> metadata=new MultiMap<String, String>();

        while(kvpp.hasNext()) {
            kvpp.parseNext();

            if(kvpp.getKey().equals(INDEX_KEY)) {
                String v = kvpp.getValue();
                int idx = v.indexOf(':');
                if(idx!=-1) {
                    // v=contract:name
                    String contract = v.substring(0, idx);
                    String name = v.substring(idx + 1);
                    
                    if (null == name || name.indexOf('|') <= 0) {
                      metadata.add(contract,name);
                    } else {
                      String [] split = name.split("\\|");
                      for (String s : split) {
                        metadata.add(contract, s);
                      }
                    }
                }
            }
            metadata.add(kvpp.getKey(),kvpp.getValue());
        }

        return metadata;
    }

}
