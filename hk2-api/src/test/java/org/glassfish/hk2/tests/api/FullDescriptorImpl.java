/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.api;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * For testing purposes
 * 
 * @author jwells
 *
 */
@Blue @Green @NotQualifierAnnotation @Red
public class FullDescriptorImpl extends DescriptorImpl implements MarkerInterface, MarkerInterface2, MarkerInterface3 {
    private final static Set<String> FULL_CONTRACTS = new LinkedHashSet<String>();
    /** Given name */
    public final static String FULL_NAME = "Full";
    private final static Map<String, List<String>> FULL_METADATA =
            new LinkedHashMap<String, List<String>>();
    /** Given key1 */
    public final static String FULL_KEY1 = "key1";
    /** Given key2 */
    public final static String FULL_KEY2 = "key2";
    /** Given value1 */
    public final static String FULL_VALUE1 = "value1";
    /** Given value2 */
    public final static String FULL_VALUE2 = "value2";
    private final static Set<String> FULL_ANNOTATIONS = new LinkedHashSet<String>();
    /** Given initial rank */
    public final static int FULL_INITIAL_RANK = -1;
    /** Given initial proxiable */
    public final static Boolean FULL_INITIAL_PROXIABLE = Boolean.FALSE;
    /** Given initial proxyForSameScope */
    public final static Boolean FULL_INITIAL_PROXY_FOR_SAME_SCOPE = Boolean.TRUE;
    /** Given initial service id */
    public final static Long FULL_INITIAL_SID = new Long(-2);
    /** Given initial locator id */
    public final static Long FULL_INITIAL_LID = new Long(-3);
    /** The name of the class analysis service */
    public final static String FULL_ANALYSIS_SERVICE = "jax-rs";
    
    static {
        FULL_CONTRACTS.add(FullDescriptorImpl.class.getName());
        FULL_CONTRACTS.add(MarkerInterface.class.getName());
        
        List<String> key1_values = new LinkedList<String>();
        key1_values.add(FULL_VALUE1);
        
        List<String> key2_values = new LinkedList<String>();
        key2_values.add(FULL_VALUE1);
        key2_values.add(FULL_VALUE2);
        
        FULL_METADATA.put(FULL_KEY1, key1_values);
        FULL_METADATA.put(FULL_KEY2, key2_values);
        
        FULL_ANNOTATIONS.add(Green.class.getName());
        FULL_ANNOTATIONS.add(Blue.class.getName());
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 5270371169142849733L;
    
    /**
     * For testing
     */
    public FullDescriptorImpl() {
        super(FULL_CONTRACTS,
                FULL_NAME,
                Singleton.class.getName(),
                FullDescriptorImpl.class.getName(),
                FULL_METADATA,
                FULL_ANNOTATIONS,
                DescriptorType.PROVIDE_METHOD,
                DescriptorVisibility.LOCAL,
                new HK2LoaderImpl(),
                FULL_INITIAL_RANK,
                FULL_INITIAL_PROXIABLE,
                FULL_INITIAL_PROXY_FOR_SAME_SCOPE,
                FULL_ANALYSIS_SERVICE,
                FULL_INITIAL_SID,
                FULL_INITIAL_LID);
    }

}
