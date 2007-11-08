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

package com.sun.jbi.jsf.framework.common;

/**
 * JbiConstants.java
 *
 * @author ylee
 */
public interface JbiConstants {
    
    public static String JBI_TAG = "jbi";
    public static String VERSION_TAG = "version";
    
    /** Service Assembly */
    public static String SERVICE_ASSEMBLY_TAG = "service-assembly";
    public static String IDENTIFICATION_TAG = "identification";
    public static String SERVICE_UNIT_TAG = "service-unit";
    
    public static String NAME_TAG = "name";
    public static String DESCRIPTION_TAG = "description";
    
    public static String COMPONENT_NAME_TAG = "component-name";
    public static String ARTIFACTS_ZIP_TAG = "artifacts-zip";
    
    public static String CONNECTIONS_TAG = "connections";
    public static String CONNECTION_TAG = "connection";
    public static String CONSUMER_TAG = "consumer";
    public static String PROVIDER_TAG = "provider";
    public static String ENDPOINT_NAME = "endpoint-name";
    
    /** Service  Units */
    public static String BINDING_COMPONENT_TAG = "binding-component";
    public static String SERVICES_TAG = "services";
    public static String CONSUMES_TAG = "consumes";
    public static String PROVIDES_TAG = "provides";
    public static String SERVICE_NAME_TAG = "service-name";
    public static String INTERFACE_NAME_TAG = "interface-name";
    
    /** Namespaces */
    public static String NAMESPACE_PREFIX = "xmlns:";
    public static String VERSION = "version";
    public static String SCHEMA_PREFIX = "xsi:";
    public static String SCHEMA_LOCATION = "schemaLocation";
    public static String COLON_SEPARATOR = ":";
    public static String COMMA_SEPARATOR = ",";
    public static String SEMICOLON_SEPARATOR = ";";
    public static String HYPHEN_SEPARATOR = "-";
    
    

}
