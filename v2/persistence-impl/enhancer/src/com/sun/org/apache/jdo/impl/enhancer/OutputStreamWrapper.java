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

package com.sun.org.apache.jdo.impl.enhancer;

import java.io.OutputStream;

/**
 *  This class serves as a wrapper for an output stream of a class file. The
 *  stream is passed as a parameter to the byte code enhancer, that can
 *  sets the classname of the written Java class to the wrapper.
 *  <br>
 *  This wrapper is necessary to determine the classname outside the enhancer,
 *  after the class has been enhanced, since do do not always know the
 *  classname of an opened input stream.
 *
 */
public class OutputStreamWrapper
{
    /**
     *  The wrapped output stream.
     */
    private OutputStream out;

    /**
     *  The classname of the written Java class. This parameter
     *  is set by the enhancer.
     */
    private String className = null;

    /**
     *  Constructs a new object.
     *
     *  @param  out  The output stream to wrap.
     */
    public OutputStreamWrapper(OutputStream out)
    {
        this.out = out;
    }

    /**
     *  Gets the wrapped output stream.
     *
     *  @return The wrapped output stream.
     */
    public final OutputStream getStream()
    {
        return out;
    }

    /**
     *  Gets the classname of the written Java class. This method should be
     *  called after the class has been enhanced.
     *
     *  @return  The name of the written Java class.
     */
    public final String getClassName()
    {
        return className;
    }

    /**
     *  Sets the name of the written Java class. This method should be called
     *  by the enhancer.
     *
     *  @param  classname  The name of the Java class.
     */
    public final void setClassName(String classname)
    {
        this.className = classname;
    }
}
