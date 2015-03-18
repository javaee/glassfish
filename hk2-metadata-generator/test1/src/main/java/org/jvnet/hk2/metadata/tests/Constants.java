/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.metadata.tests;

/**
 * @author jwells
 *
 */
public class Constants {
    public final static String GENERATE_METHOD_CREATE_IMPL = "com.acme.service.GenerateMethodImpl";
    public final static String GENERATE_METHOD_CREATE_CONTRACT = "com.acme.api.GenerateMethod";
    public final static String GENERATE_METHOD_CREATE_NAME1 = "name1";
    public final static String GENERATE_METHOD_CREATE_NAME2 = "name2";
    public final static String GENERATE_METHOD_CREATE_NAME3 = "name3";
    public final static String GENERATE_METHOD_CREATE_NAME4 = "name4";
    public final static String GENERATE_METHOD_CREATE_NAME5 = "name5";
    
    public final static String GENERATE_METHOD_DELETE_IMPL = "com.acme.service.DeleteImpl";
    public final static String GENERATE_METHOD_DELETE_CONTRACT = "com.acme.api.GenerateMethod";
    public final static String GENERATE_METHOD_DELETE_SCOPE = "javax.inject.Singleton";
    
    // metadata constants
    public final static String KEY1 = "key1";
    public final static String VALUE1 = "value1";
    public final static String KEY2 = "key2";
    public final static String VALUE2 = "value2";
    public final static String KEY3 = "key3";
    public final static String VALUE3 = "3";
    public final static String KEY4 = "key4";
    public final static String VALUE4 = Constants.class.getName();
    public final static String KEY5 = "key5";
    public final static String VALUE5_1 = "5_1";
    public final static String VALUE5_2 = "5_2";
    public final static String VALUE5_3 = "5_3";
    public final static String KEY6 = "key6";
    public final static long VALUE6_1 = 6001L;
    public final static long VALUE6_2 = 6002L;
    public final static long VALUE6_3 = 6003L;
    
    /** The name for non-defaulted things */
    public final static String NON_DEFAULT_NAME = "non-default-name";
    
    /** The rank to use when testing for rank */
    public final static int RANK = 13;
    
    /** The rank to use when testing for rank on factory method */
    public final static int FACTORY_METHOD_RANK = -1;
    
    /** A custom analyzer for a descriptor */
    public final static String CUSTOM_ANALYZER = "CustomAnalyzer";

}
