/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
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

package com.sun.org.apache.jdo.impl.enhancer.meta.prop;


/**
 * Some utility methods for classname conversion.
 */
final class NameHelper
{
    /**
     *  Converts a classname given in a given VM-similar notation(with slashes)
     *  into a canonical notation (with dots).
     *
     *  @param  classname The VM-similar notation of the classname.
     *  @return  The canonical classname.
     *  @see  #fromCanonicalClassName
     */
    static String toCanonicalClassName(String classname)
    {
        return classname.replace('/', '.');
    }

    /**
     *  Converts a classname given in a canonical form(with dots) into
     *  a VM-similar notation (with slashes)
     *
     *  @param  classname  The canonical classname.
     *  @return  The VM-similar classname notation.
     *  @see  #toCanonicalClassName
     */
    static String fromCanonicalClassName(String classname)
    {
        return classname.replace('.', '/');
    }
}
