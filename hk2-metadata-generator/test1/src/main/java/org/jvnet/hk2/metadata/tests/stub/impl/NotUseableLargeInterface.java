/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.metadata.tests.stub.impl;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.metadata.tests.stub.LargeInterface;

/**
 * @author jwells
 *
 */
@Service
public class NotUseableLargeInterface implements LargeInterface {

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#notOverridden(boolean)
     */
    @Override
    public boolean notOverridden(boolean param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodVoids()
     */
    @Override
    public void methodVoids() {
        throw new AssertionError("Must not be used");

    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodBoolean(boolean)
     */
    @Override
    public boolean methodBoolean(boolean param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodByte(byte)
     */
    @Override
    public byte methodByte(byte param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodChar(char)
     */
    @Override
    public char methodChar(char param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodDouble(double)
     */
    @Override
    public double methodDouble(double param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodFloat(float)
     */
    @Override
    public float methodFloat(float param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodInt(int)
     */
    @Override
    public int methodInt(int param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodInt(long)
     */
    @Override
    public long methodInt(long param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodShort(short)
     */
    @Override
    public short methodShort(short param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodDeclared(java.util.Map, java.lang.String, java.util.Random)
     */
    @Override
    public List<String> methodDeclared(Map<Object, String> param,
            String param1, Random param2) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodBooleanArray(boolean[])
     */
    @Override
    public boolean[] methodBooleanArray(boolean[] param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodByteArray(byte[])
     */
    @Override
    public byte[][][][] methodByteArray(byte[] param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodCharArray(char[][])
     */
    @Override
    public char[] methodCharArray(char[][] param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodDoubleArray(double[][][])
     */
    @Override
    public double[][] methodDoubleArray(double[][][] param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodFloatArray(float[][])
     */
    @Override
    public float[] methodFloatArray(float[][] param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodIntArray(int[])
     */
    @Override
    public int[][] methodIntArray(int[] param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodIntArray(long[][][][][])
     */
    @Override
    public long[] methodIntArray(long[][][][][] param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodShortArray(short)
     */
    @Override
    public short[] methodShortArray(short[] param) {
        throw new AssertionError("Must not be used");
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.metadata.tests.stub.LargeInterface#methodDeclaredArray(java.util.Map[], java.lang.String[], java.util.Random[])
     */
    @Override
    public List<String>[] methodDeclaredArray(Map<Object, String>[] param,
            String[] param1, Random... param2) {
        throw new AssertionError("Must not be used");
    }

}
