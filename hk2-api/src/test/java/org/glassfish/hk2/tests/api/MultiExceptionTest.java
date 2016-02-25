/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2016 Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.glassfish.hk2.api.MultiException;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class MultiExceptionTest {
    private final static String EXPECTED = "expected";
    
    private final static String E1 = "E1";
    private final static String E2 = "E2";
    
    /**
     * Tests that I can create a no-args multi exception
     */
    @Test
    public void testNoArgsMultiException() {
        MultiException me = new MultiException();
        
        Assert.assertTrue(me.getErrors().isEmpty());
        
        Assert.assertTrue(me.toString().contains("MultiException"));
    }
    
    /**
     * Tests that I can create a single throwable multi exception
     */
    @Test
    public void testSingleThrowableMultiException() {
        IllegalArgumentException iae = new IllegalArgumentException(EXPECTED);
        
        MultiException me = new MultiException(iae);
        
        List<Throwable> ths = me.getErrors();
        Assert.assertEquals(1, ths.size());
        Assert.assertEquals(iae, ths.get(0));
        
        Assert.assertTrue(me.toString().contains(EXPECTED));
    }
    
    /**
     * Tests that I can create a multi throwable multi exception
     */
    @Test
    public void testMultiThrowableMultiException() {
        List<Throwable> putMeIn = new LinkedList<Throwable>();
        
        IllegalArgumentException iae = new IllegalArgumentException(E1);
        IllegalStateException ise = new IllegalStateException(E2);
        
        putMeIn.add(iae);
        putMeIn.add(ise);
        
        MultiException me = new MultiException(putMeIn);
        
        List<Throwable> ths = me.getErrors();
        Assert.assertEquals(2, ths.size());
        Assert.assertEquals(iae, ths.get(0));
        Assert.assertEquals(ise, ths.get(1));
        
        Assert.assertTrue(me.toString().contains(E1));
        Assert.assertTrue(me.toString().contains(E2));
    }
    
    /**
     * Tests that I can create a multi throwable multi exception
     */
    @Test
    public void testPrintException() {
        List<Throwable> putMeIn = new LinkedList<Throwable>();
        
        IllegalArgumentException iae = new IllegalArgumentException(E1);
        IllegalStateException ise = new IllegalStateException(E2);
        
        putMeIn.add(iae);
        putMeIn.add(ise);
        
        MultiException me = new MultiException(putMeIn);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream writer = new PrintStream(baos);
        
        me.printStackTrace(writer);
        
        writer.close();
        
        String asString = baos.toString();
        
        Assert.assertTrue(asString.contains(E1));
        Assert.assertTrue(asString.contains(E2));
    }
    
    /**
     * Tests that I can create a multi throwable multi exception
     */
    @Test
    public void testPrintExceptionPrintWriter() {
        List<Throwable> putMeIn = new LinkedList<Throwable>();
        
        IllegalArgumentException iae = new IllegalArgumentException(E1);
        IllegalStateException ise = new IllegalStateException(E2);
        
        putMeIn.add(iae);
        putMeIn.add(ise);
        
        MultiException me = new MultiException(putMeIn);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);
        
        me.printStackTrace(writer);
        
        writer.close();
        
        String asString = baos.toString();
        
        Assert.assertTrue(asString.contains(E1));
        Assert.assertTrue(asString.contains(E2));
    }
    
    /**
     * Tests that I can create a multi throwable multi exception
     */
    @Test
    public void testGetMessage() {
        List<Throwable> putMeIn = new LinkedList<Throwable>();
        
        IllegalArgumentException iae = new IllegalArgumentException(E1);
        IllegalStateException ise = new IllegalStateException(E2);
        
        putMeIn.add(iae);
        putMeIn.add(ise);
        
        MultiException me = new MultiException(putMeIn);
        
        String asString = me.getMessage();
        
        Assert.assertTrue(asString.contains(E1));
        Assert.assertTrue(asString.contains(E2));
    }
    
    /**
     * Tests that I can create a multi throwable multi exception
     */
    @Test
    public void testToString() {
        List<Throwable> putMeIn = new LinkedList<Throwable>();
        
        IllegalArgumentException iae = new IllegalArgumentException(E1);
        IllegalStateException ise = new IllegalStateException(E2);
        
        putMeIn.add(iae);
        putMeIn.add(ise);
        
        MultiException me = new MultiException(putMeIn);
        
        String asString = me.toString();
        
        Assert.assertTrue(asString.contains(E1));
        Assert.assertTrue(asString.contains(E2));
    }

    /**
     * Tests that I can serialize a multi exception
     */
    @Test
    public void testSerializeMultiException() throws ClassNotFoundException, IOException {
        final MultiException me = new MultiException();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(me);
        final byte[] bits = baos.toByteArray();
        Assert.assertNotNull(bits);
        Assert.assertTrue(bits.length > 0);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bits);
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final Object lazarus = ois.readObject();
        Assert.assertTrue(lazarus instanceof MultiException);
    }
    
    /**
     * Tests that we can concurrently access the list of errors.  This
     * test will fail out with a ConcurrentModificationException
     * if the implementation is bad
     * 
     * @throws InterruptedException
     */
    @Test
    public void testConcurrentAccessOfErrors() throws InterruptedException {
        MultiException me = new MultiException(new IllegalStateException("Initial Exception"));
        
        Thread t = new Thread(new ExceptionChangerRunner(20, me));
        
        t.start();
        
        for (Throwable th : me.getErrors()) {
            Thread.sleep(10);
            
            Assert.assertNotNull(th);
        }
    }
    
    private static class ExceptionChangerRunner implements Runnable {
        private final int numToAdd;
        private final MultiException me;
        
        private ExceptionChangerRunner(int numToAdd, MultiException me) {
            this.numToAdd = numToAdd;
            this.me = me;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            for (int lcv = 0; lcv < numToAdd; lcv++) {
                me.addError(new IllegalStateException("Adding exception " + lcv));
                
                try {
                    Thread.sleep(5);
                }
                catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
                
            }
            
        }
        
    }

}
