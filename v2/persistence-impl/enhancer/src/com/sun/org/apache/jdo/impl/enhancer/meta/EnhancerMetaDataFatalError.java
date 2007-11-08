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


package com.sun.org.apache.jdo.impl.enhancer.meta;


/**
 * Thrown to indicate that an access to JDO meta-data failed due to a
 * serious error, which might have left the meta-data component in an
 * inconsistent state.
 */
public class EnhancerMetaDataFatalError
    //^olsen: provisional, convert to a checked exception
    extends RuntimeException
{
    /**
     * An optional nested exception.
     */
    public final Throwable nested;

    /**
     * Constructs an <code>EnhancerMetaDataFatalError</code> with no detail message.
     */
    public EnhancerMetaDataFatalError()
    {
        this.nested = null;
    }

    /**
     * Constructs an <code>EnhancerMetaDataFatalError</code> with the specified
     * detail message.
     */
    public EnhancerMetaDataFatalError(String msg)
    {
        super(msg);
        this.nested = null;
    }

    /**
     * Constructs an <code>EnhancerMetaDataFatalError</code> with an optional
     * nested exception.
     */
    public EnhancerMetaDataFatalError(Throwable nested)
    {
        super(nested.toString());
        this.nested = nested;
    }

    /**
     * Constructs an <code>EnhancerMetaDataFatalError</code> with the specified
     * detail message and an optional nested exception.
     */
    public EnhancerMetaDataFatalError(String msg, Throwable nested)
    {
        super(msg);
        this.nested = nested;
    }
}
