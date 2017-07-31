/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

import java.util.*;

/** 
 * Holds namespace-to-package mapping information from a 
 * "non-exhaustive" jaxrpc mapping file.
 *
 * @author Kenneth Saks
 */
 
public class JaxrpcMappingDescriptor extends Descriptor {

    private Map packageToNamespaceUriMap = new HashMap();
    private Map namespaceUriToPackageMap = new HashMap();

    private boolean simpleMapping = true;

    public JaxrpcMappingDescriptor() {
    }

    public void setSpecVersion(String version) {
        // ignore
    }

    public void setIsSimpleMapping(boolean flag) {
        simpleMapping = flag;
    }

    /**
     * @return true if only mapping info only contains package->namespace
     * mapping.
     */
    public boolean isSimpleMapping() {
        return simpleMapping;
    }

    public void addMapping(String javaPackage, String namespaceUri) {
        packageToNamespaceUriMap.put(javaPackage, namespaceUri);
        namespaceUriToPackageMap.put(namespaceUri, javaPackage);
    }

    /**
     * @return Collection of Mapping elements
     */
    public Collection getMappings() {
        Collection mappings = new HashSet();
        Iterator nIter = namespaceUriToPackageMap.entrySet().iterator();
        while(nIter.hasNext()){
            Map.Entry entry = (Map.Entry) nIter.next();
            String namespaceUri = (String) entry.getKey();
            String javaPackage = (String) entry.getValue();
            Mapping mapping = new Mapping(namespaceUri, javaPackage);
            mappings.add(mapping);
        }
        return mappings;
    }

    public static class Mapping {
        private String namespaceUri;
        private String javaPackage;
        
        public Mapping(String namespace, String thePackage) {
            namespaceUri = namespace;
            javaPackage  = thePackage;
        }

        public String getNamespaceUri() {
            return namespaceUri;
        }

        public String getPackage() {
            return javaPackage;
        }
    }
    
}
