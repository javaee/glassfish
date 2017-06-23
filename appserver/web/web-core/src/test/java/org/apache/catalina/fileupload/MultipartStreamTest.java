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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.fileupload;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;


/**
 * Unit tests {@link org.apache.commons.fileupload.MultipartStream}.
 * 
 * @version $Id$
 */
public class MultipartStreamTest {
    
    static private final String BOUNDARY_TEXT = "myboundary";
    
    /**
     * The Carriage Return ASCII character value.
     */
    public static final byte CR = 0x0D;


    /**
     * The Line Feed ASCII character value.
     */
    public static final byte LF = 0x0A;


    /**
     * The dash (-) ASCII character value.
     */
    public static final byte DASH = 0x2D;
    
    @Test
    public void testThreeParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        byte[] BOUNDARY_PREFIX = {CR, LF, DASH, DASH};
        int iBufSize =
                boundary.length + BOUNDARY_PREFIX.length + 1;
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSmallBuffer() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        int iBufSize = 1;
        @SuppressWarnings("unused")
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
    }

    @Test
    public void testTwoParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }
    
}
